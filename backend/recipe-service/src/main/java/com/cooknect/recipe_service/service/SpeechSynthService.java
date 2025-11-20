package com.cooknect.recipe_service.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import com.cooknect.recipe_service.model.RecipeAudio;
import com.cooknect.recipe_service.repository.RecipeAudioRepository;
import com.cooknect.recipe_service.model.Recipe;
import com.cooknect.recipe_service.repository.RecipeRepository;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;


@Service
public class SpeechSynthService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private RecipeRepository recipeRepository;

    @Value("${GL_API_KEY}")
    private String apiKey;

    private static final Logger log = LoggerFactory.getLogger(SpeechSynthService.class);

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key=";

    private static final String translate_api = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    /**
     * Returns existing audio for recipeId if present. Otherwise synthesizes,
     * persists the result (if recipeId provided) and returns the bytes.
     */
    @Transactional
    public byte[] getOrCreateAudio(String text, String voiceName, Long recipeId) {
        try {
            if (recipeId != null) {
                Optional<RecipeAudio> opt = recipeAudioRepository.findByRecipeId(recipeId);
                if (opt.isPresent()) {
                    log.info("Returning cached audio for recipeId={}", recipeId);
                    return opt.get().getAudioData();
                }
            }

            // synthesizeAudio(...) is existing method that produces the audio bytes
            byte[] wav = synthesizeAudio(text, voiceName, recipeId);

            // Save only if recipeId provided and no existing row (avoid duplicates)
            if (recipeId != null) {
                try {
                    if (!recipeAudioRepository.findByRecipeId(recipeId).isPresent()) {
                        RecipeAudio audioEntity = new RecipeAudio(recipeId, wav, "audio/wav");
                        RecipeAudio saved = recipeAudioRepository.save(audioEntity);
                        log.info("Saved TTS WAV to database for recipeId={} audioId={}", recipeId, saved.getId());
                    } else {
                        log.info("Audio already saved concurrently for recipeId={}", recipeId);
                    }
                } catch (Exception dbEx) {
                    log.warn("Failed to save TTS WAV to DB for recipeId={}: {}", recipeId, dbEx.getMessage());
                }
            }

            return wav;
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            log.error("Unexpected error in getOrCreateAudio: {}", e.getMessage(), e);
            throw new RuntimeException("TTS failed", e);
        }
    }

    @Autowired
    private RecipeAudioRepository recipeAudioRepository;

    public byte[] synthesizeAudio(String text, String voiceName, Long recipeId) {
        try {
            ObjectNode payload = mapper.createObjectNode();

            // ---- contents ----
            ArrayNode contents = mapper.createArrayNode();
            ObjectNode contentObj = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();
            ObjectNode textObj = mapper.createObjectNode();
            textObj.put("text", text);
            parts.add(textObj);
            contentObj.set("parts", parts);
            contents.add(contentObj);
            payload.set("contents", contents);

            // ---- generationConfig ----
            ObjectNode prebuilt = mapper.createObjectNode();
            prebuilt.put("voiceName", voiceName == null ? "Kore" : voiceName);

            ObjectNode voiceConfig = mapper.createObjectNode();
            voiceConfig.set("prebuiltVoiceConfig", prebuilt);

            ObjectNode speechConfig = mapper.createObjectNode();
            speechConfig.set("voiceConfig", voiceConfig);

            ObjectNode genConfig = mapper.createObjectNode();
            genConfig.putArray("responseModalities").add("AUDIO");
            genConfig.set("speechConfig", speechConfig);

            payload.set("generationConfig", genConfig);

            // ---- Prepare POST Request ----
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request =
                    new HttpEntity<>(payload.toString(), headers);

            String url = GEMINI_URL + apiKey;

            // ---- Call Gemini ----
            ResponseEntity<JsonNode> response =
                    restTemplate.postForEntity(url, request, JsonNode.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Gemini HTTP error: " + response.getStatusCode());
            }

            JsonNode body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Empty response from Gemini");
            }

            // ---- Extract Base64 Audio ----
            String base64 = body
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("inlineData")
                    .path("data")
                    .asText();

            byte[] pcmBytes = Base64.getDecoder().decode(base64);
            System.out.println("Gemini TTS synthesis complete.");
            byte[] wav = convertPcmToWav(pcmBytes);

            // Save WAV to /tmp for inspection (server-side)
            try {
                String fname = "gemini-tts-" + Instant.now().toEpochMilli() + ".wav";
                Path out = Paths.get("/tmp").resolve(fname);
                Files.write(out, wav);
                System.out.println("Saved TTS WAV to: " + out.toString());
            } catch (IOException ioe) {
                System.out.println("Failed to save TTS WAV to /tmp: " + ioe.getMessage());
            }

            // Persist to Postgres
            try {
                if (recipeId != null) {
                    RecipeAudio audio = new RecipeAudio(recipeId, wav, "audio/wav");
                    recipeAudioRepository.save(audio);
                    System.out.println("Saved TTS WAV to database for recipeId=" + recipeId);
                }
            } catch (Exception dbEx) {
                System.out.println("Failed to save TTS WAV to DB: " + dbEx.getMessage());
            }

          return wav;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Gemini error: " + e.getMessage(), e);
        }
    }

    @Transactional
    public void deleteAudioForRecipe(Long recipeId) {
        if(recipeId == null) return;
        try {
            Optional<RecipeAudio> opt = recipeAudioRepository.findByRecipeId(recipeId);
            if (opt.isPresent()) {
                recipeAudioRepository.delete(opt.get());
                log.info("Deleted audio for recipeId={}", recipeId);
            }
            else {
                log.info("No cached audio found to delete for recipeId={}", recipeId);
            }
        } catch (Exception e) {
            log.error("Failed to delete audio for recipeId={}: {}", recipeId, e.getMessage(), e);
        }
    }


    // ---------------------------------------------------------
    // Convert raw PCM LINEAR16 → WAV wrapper
    // ---------------------------------------------------------
    private byte[] convertPcmToWav(byte[] pcmData) throws IOException {

        int sampleRate = 24000;
        int channels = 1;
        int bitsPerSample = 16;

        int byteRate = sampleRate * channels * bitsPerSample / 8;
        int blockAlign = channels * bitsPerSample / 8;
        int subchunk2Size = pcmData.length;

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // WAV Header
        writeString(out, "RIFF");
        writeInt(out, 36 + subchunk2Size);
        writeString(out, "WAVE");

        writeString(out, "fmt ");
        writeInt(out, 16); // Subchunk1Size
        writeShort(out, (short) 1); // PCM
        writeShort(out, (short) channels);
        writeInt(out, sampleRate);
        writeInt(out, byteRate);
        writeShort(out, (short) blockAlign);
        writeShort(out, (short) bitsPerSample);

        writeString(out, "data");
        writeInt(out, subchunk2Size);
        out.write(pcmData);

        return out.toByteArray();
    }

    private void writeString(ByteArrayOutputStream out, String value) throws IOException {
        out.write(value.getBytes(StandardCharsets.US_ASCII));
    }

    private void writeInt(ByteArrayOutputStream out, int value) throws IOException {
        out.write(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array());
    }

    private void writeShort(ByteArrayOutputStream out, short value) throws IOException {
        out.write(ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array());
    }


    // Translate the recipe into chosen language
    @Transactional
    public String translateText(Long id, String targetLanguage) {
        try {
            if(id == null) {
                throw new IllegalArgumentException("Recipe ID cannot be null for translation.");
            }

            Recipe recipe = recipeRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Recipe not found for ID: " + id));
            String text = "Title: " + recipe.getTitle() + "\n\nDescription: " + recipe.getDescription() + "\n\nIngredients: " + recipe.getIngredients().toString() + "\n\nCuisine: " + recipe.getCuisine().toString();

            ObjectNode payload = mapper.createObjectNode();

            String instruction = "Translate the following text into " + targetLanguage + ":" + text;
            System.out.println("Translation instruction: " + instruction);
            // ---- contents ----
            ArrayNode contents = mapper.createArrayNode();
            ObjectNode contentObj = mapper.createObjectNode();
            ArrayNode parts = mapper.createArrayNode();
            ObjectNode textObj = mapper.createObjectNode();
            textObj.put("text", instruction);
            parts.add(textObj);
            contentObj.set("parts", parts);
            contents.add(contentObj);
            payload.set("contents", contents);

            // ---- Prepare POST Request ----
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> request =
                    new HttpEntity<>(payload.toString(), headers);

            String url = translate_api + "?key=" + apiKey;

            // ---- Call Gemini ----
            ResponseEntity<JsonNode> response;
            try {
                response =
                        restTemplate.postForEntity(url, request, JsonNode.class);
            } catch (org.springframework.web.client.HttpClientErrorException httpEx) {
                String bodyText = httpEx.getResponseBodyAsString();
                log.error("Gemini HTTP error: status={}, body={}", httpEx.getStatusCode(), bodyText);
                throw new RuntimeException("Gemini error: " + httpEx.getStatusCode() + ": " + (bodyText == null ? "[no body]" : bodyText), httpEx);
            }

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Gemini HTTP error: " + response.getStatusCode());
            }

            JsonNode body = response.getBody();
            if (body == null) {
                throw new RuntimeException("Empty response from Gemini");
            }

            // ---- Extract Translated Text ----
            String translatedText = body
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text")
                    .asText();

            return translatedText;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Gemini error: " + e.getMessage(), e);
        }
    }
}
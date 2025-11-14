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

@Service
public class SpeechSynthService {

    private final ObjectMapper mapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${GL_API_KEY}")
    private String apiKey;

    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-preview-tts:generateContent?key=";

    public byte[] synthesizeAudio(String text, String voiceName) {
        try {
            System.out.println("Synthesizing speech with Gemini TTS...");
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

          return wav;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Gemini error: " + e.getMessage(), e);
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
}
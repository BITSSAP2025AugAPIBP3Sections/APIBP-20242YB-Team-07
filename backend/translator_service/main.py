from fastapi import FastAPI
from fastapi.responses import FileResponse
from pydantic import BaseModel
from transformers import MarianMTModel, MarianTokenizer
from gtts import gTTS
import uuid
import os

app = FastAPI()

# Translation model (English → Hindi)
model_name = "Helsinki-NLP/opus-mt-en-hi"
tokenizer = MarianTokenizer.from_pretrained(model_name)
translator = MarianMTModel.from_pretrained(model_name)

AUDIO_DIR = "output_audio"
os.makedirs(AUDIO_DIR, exist_ok=True)

# Language mapping for gTTS
LANG_MAP = {
    "hi": "hi",  # Hindi
    "fr": "fr",  # French
    "es": "es",  # Spanish
    "de": "de",  # German
    "it": "it",  # Italian
    "ja": "ja",  # Japanese
    "ko": "ko",  # Korean
    "zh": "zh-cn",  # Chinese
    "ar": "ar",  # Arabic
    "pt": "pt",  # Portuguese
}

class TranslateAudioRequest(BaseModel):
    text: str
    target_lang: str   # "hi", "fr", "es", etc

@app.post("/translate-audio")
def translate_and_convert(req: TranslateAudioRequest):
    try:
        # Step 1: Translate text
        encoded = tokenizer(req.text, return_tensors="pt", padding=True)
        translated_tokens = translator.generate(**encoded)
        translated_text = tokenizer.decode(translated_tokens[0], skip_special_tokens=True)

        # Step 2: Generate audio file
        file_name = f"{uuid.uuid4()}.mp3"
        out_path = os.path.join(AUDIO_DIR, file_name)

        # Use gTTS for text-to-speech
        lang_code = LANG_MAP.get(req.target_lang, "hi")
        tts = gTTS(text=translated_text, lang=lang_code, slow=False)
        tts.save(out_path)

        return {
            "translated_text": translated_text,
            "audio_url": f"/audio/{file_name}"
        }
    except Exception as e:
        return {"error": str(e)}, 500

@app.get("/audio/{file}")
def get_audio(file: str):
    file_path = os.path.join(AUDIO_DIR, file)
    if os.path.exists(file_path):
        return FileResponse(file_path, media_type="audio/mpeg")
    return {"error": "File not found"}, 404

@app.get("/health")
def health_check():
    return {"status": "healthy", "service": "translator"}

@app.get("/")
def root():
    return {
        "service": "Translation & TTS Service",
        "version": "1.0",
        "endpoints": {
            "/translate-audio": "POST - Translate text and generate audio",
            "/audio/{file}": "GET - Download audio file",
            "/health": "GET - Health check"
        }
    }
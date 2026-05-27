"""CLI plant image analysis via local Ollama (same idea as Java OllamaService). No API keys."""
import sys
import os
import base64
import json
import urllib.request

OLLAMA = os.environ.get("OLLAMA_HOST", "http://localhost:11434").rstrip("/")
MODEL = os.environ.get("OLLAMA_VISION_MODEL", "llava")


def analyze_plant(image_path: str) -> str:
    try:
        with open(image_path, "rb") as f:
            b64 = base64.standard_b64encode(f.read()).decode("ascii")
    except OSError as e:
        return f"Error reading image: {e}"

    prompt = (
        "You are an expert plant pathologist. Analyze this plant image. "
        "Identify the plant if possible, any disease/pests/deficiencies, "
        "and give actionable treatment steps. Use Markdown."
    )
    body = json.dumps(
        {"model": MODEL, "prompt": prompt, "images": [b64], "stream": False}
    ).encode("utf-8")

    req = urllib.request.Request(
        f"{OLLAMA}/api/generate",
        data=body,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=600) as resp:
            data = json.loads(resp.read().decode("utf-8"))
        if data.get("error"):
            return str(data["error"])
        return (data.get("response") or "").strip() or "Empty response from Ollama."
    except urllib.error.HTTPError as e:
        return f"Ollama HTTP {e.code}: {e.read().decode('utf-8', errors='replace')[:500]}"
    except Exception as e:
        return f"Ollama error: {e}. Is Ollama running? Try: ollama pull {MODEL}"


if __name__ == "__main__":
    if len(sys.argv) > 1:
        print(analyze_plant(sys.argv[1]))
    else:
        print("Usage: python plant_vision.py <image path>")

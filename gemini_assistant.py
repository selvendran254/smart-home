# Renamed purpose: local Ollama text assistant (legacy filename kept for old scripts).
import os
import json
import urllib.request

OLLAMA = os.environ.get("OLLAMA_HOST", "http://localhost:11434").rstrip("/")
MODEL = os.environ.get("OLLAMA_MODEL", "llama3")


def get_ollama_response(prompt: str) -> str:
    if not prompt or not str(prompt).strip():
        return "Error: empty prompt."
    body = json.dumps(
        {"model": MODEL, "prompt": str(prompt).strip(), "stream": False}
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
        return (data.get("response") or "").strip() or "Empty response."
    except Exception as e:
        return f"Ollama error: {e}. Run: ollama pull {MODEL}"


# Backward-compatible name
def get_gemini_response(prompt):
    return get_ollama_response(prompt)


if __name__ == "__main__":
    import sys

    if len(sys.argv) > 1:
        print(get_ollama_response(" ".join(sys.argv[1:])))
    else:
        print("Usage: python gemini_assistant.py <your question>")

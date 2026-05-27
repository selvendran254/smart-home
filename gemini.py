"""Tiny CLI: ask local Ollama (no Google API)."""
import sys
from gemini_assistant import get_ollama_response

if __name__ == "__main__":
    if len(sys.argv) > 1:
        print(get_ollama_response(" ".join(sys.argv[1:])))
    else:
        print("Example: python gemini.py What is photosynthesis?")

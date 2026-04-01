import urllib.request
import urllib.error
import json
import sys
# We use PYTHONUTF8=1 in ProcessBuilder to handle unicode

import ssl

def get_gemini_response(prompt):
    api_key = "AIzaSyDwDCfv1cVVoUipZgngMAeMePMrqYSXMrI"
    
    # 1. Fetch available models
    models_url = f"https://generativelanguage.googleapis.com/v1beta/models?key={api_key}"
    model_name = "models/gemini-1.5-flash-latest" # safe fallback
    
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE
    
    try:
        req = urllib.request.Request(models_url)
        with urllib.request.urlopen(req, context=ctx) as response:
            data = json.loads(response.read().decode('utf-8'))
            for m in data.get('models', []):
                if 'generateContent' in m.get('supportedGenerationMethods', []):
                    model_name = m.get('name')
                    if "flash" in model_name: # Try to prefer flash because it's fast
                        break
    except Exception:
        pass # silently ignore and use fallback

    # 2. Make the API request with the discovered model
    url = f"https://generativelanguage.googleapis.com/v1beta/{model_name}:generateContent?key={api_key}"
    
    headers = {'Content-Type': 'application/json'}
    data = {
        "contents": [{"parts": [{"text": prompt}]}]
    }
    
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers, method='POST')
    
    try:
        with urllib.request.urlopen(req, context=ctx) as response:
            result = json.loads(response.read().decode('utf-8'))
            text = result.get("candidates", [{}])[0].get("content", {}).get("parts", [{}])[0].get("text", "")
            return text
    except urllib.error.HTTPError as he:
        err_msg = he.read().decode('utf-8', errors='ignore')
        return f"Error: HTTP {he.code}: {err_msg}"
    except Exception as e:
        return f"Error: {e}"

if __name__ == "__main__":
    if len(sys.argv) > 1:
        # Prompt passed as argument
        prompt = sys.argv[1]
        print(get_gemini_response(prompt))
    else:
        print("No AI response")

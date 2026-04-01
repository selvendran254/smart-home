import sys
import base64
import json
import urllib.request
import urllib.error
import ssl

def analyze_plant(image_path):
    api_key = "AIzaSyDwDCfv1cVVoUipZgngMAeMePMrqYSXMrI"
    
    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE

    # Fetch available models to get correct name
    model_name = "models/gemini-1.5-flash" # fallback
    try:
        models_url = f"https://generativelanguage.googleapis.com/v1beta/models?key={api_key}"
        req_models = urllib.request.Request(models_url)
        with urllib.request.urlopen(req_models, context=ctx) as response:
            data = json.loads(response.read().decode('utf-8'))
            for m in data.get('models', []):
                if 'generateContent' in m.get('supportedGenerationMethods', []):
                    # We prefer a flash model for speed, but will take anything that supports generateContent
                    model_name = m.get('name')
                    if "flash" in model_name and "latest" not in model_name:
                        break
    except Exception:
        pass
        
    url = f"https://generativelanguage.googleapis.com/v1beta/{model_name}:generateContent?key={api_key}"
    
    # Read and encode image
    try:
        with open(image_path, "rb") as image_file:
            encoded_string = base64.b64encode(image_file.read()).decode('utf-8')
    except Exception as e:
        return f"Error reading image: {e}"

    # Determine mimeType based on extension
    mime_type = "image/jpeg"
    if image_path.lower().endswith(".png"):
        mime_type = "image/png"
    elif image_path.lower().endswith(".webp"):
        mime_type = "image/webp"

    # Context prompt 
    prompt = (
        "You are an expert plant pathologist and AI Plant Doctor. "
        "Analyze this image of a plant. "
        "1. Identify the plant if possible. "
        "2. Identify any visible diseases, pests, or nutrient deficiencies. "
        "3. Provide step-by-step actionable solutions or treatments to cure it. "
        "Reply in a clear, formatted style using Markdown (use headers, bullets, and bold text)."
    )
    
    data = {
        "contents": [
            {
                "parts": [
                    {"text": prompt},
                    {
                        "inlineData": {
                            "mimeType": mime_type,
                            "data": encoded_string
                        }
                    }
                ]
            }
        ]
    }

    ctx = ssl.create_default_context()
    ctx.check_hostname = False
    ctx.verify_mode = ssl.CERT_NONE

    headers = {'Content-Type': 'application/json'}
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers, method='POST')

    try:
        with urllib.request.urlopen(req, context=ctx) as response:
            result = json.loads(response.read().decode('utf-8'))
            text = result.get("candidates", [{}])[0].get("content", {}).get("parts", [{}])[0].get("text", "")
            return text
    except urllib.error.HTTPError as he:
        err_msg = he.read().decode('utf-8', errors='ignore')
        return f"API Error HTTP {he.code}: {err_msg}"
    except Exception as e:
        return f"API Connection Error: {e}"

if __name__ == "__main__":
    if len(sys.argv) > 1:
        image_path = sys.argv[1]
        print(analyze_plant(image_path))
    else:
        print("Error: No image path provided")

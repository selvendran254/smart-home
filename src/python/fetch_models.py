import urllib.request
import json
import ssl

api_key = "AIzaSyDwDCfv1cVVoUipZgngMAeMePMrqYSXMrI"
models_url = f"https://generativelanguage.googleapis.com/v1beta/models?key={api_key}"

ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

req = urllib.request.Request(models_url)
with urllib.request.urlopen(req, context=ctx) as response:
    data = json.loads(response.read().decode('utf-8'))
    for m in data.get('models', []):
        if 'generateContent' in m.get('supportedGenerationMethods', []):
            print(m.get('name'))

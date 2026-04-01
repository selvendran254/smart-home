import urllib.request
import json
import sys

API_KEY = "AIzaSyDwDCfv1cVVoUipZgngMAeMePMrqYSXMrI"
URL = f"https://generativelanguage.googleapis.com/v1beta/models?key={API_KEY}"

try:
    req = urllib.request.Request(URL)
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode())
        valid_models = []
        for model in data.get('models', []):
            if 'generateContent' in model.get('supportedGenerationMethods', []):
                valid_models.append(model['name'])
        print("\n".join(valid_models))
except Exception as e:
    print(f"Error: {e}")

import urllib.request
import json
import sys

def get_gemini_response(prompt):
    api_key = "AIzaSyDwDCfv1cVVoUipZgngMAeMePMrqYSXMrI"
    url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key={api_key}"
    
    headers = {'Content-Type': 'application/json'}
    data = {
        "contents": [{"parts": [{"text": prompt}]}]
    }
    
    req = urllib.request.Request(url, data=json.dumps(data).encode('utf-8'), headers=headers, method='POST')
    
    try:
        with urllib.request.urlopen(req) as response:
            result = json.loads(response.read().decode())
            text = result.get("candidates", [{}])[0].get("content", {}).get("parts", [{}])[0].get("text", "")
            return text
    except Exception as e:
        return f"Error: {e}"

if __name__ == "__main__":
    if len(sys.argv) > 1:
        # Get prompt from the command line argument
        prompt = sys.argv[1]
        print(get_gemini_response(prompt))
    else:
        print("Please provide a prompt. Example: python gemini.py 'Hello'")

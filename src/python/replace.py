import os

filepath = r"c:\Users\Selvendran\Downloads\smarthome\smarthome\src\main\resources\static\index.html"
with open(filepath, "r", encoding="utf-8") as f:
    content = f.read()

new_content = content.replace("http://10.227.61.228", "http://10.44.229.228/")

with open(filepath, "w", encoding="utf-8") as f:
    f.write(new_content)

print("Replacement complete.")

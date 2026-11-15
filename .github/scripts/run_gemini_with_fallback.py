import json
import os
import subprocess
import sys
import urllib.request

api_key = os.environ.get("GEMINI_API_KEY", "").strip()
prompt = sys.argv[1]

priority_models = [
    "gemini-3.5-flash-lite",
    "gemini-3.8-flash",
    "gemini-2.5-flash-lite",
    "gemini-2.0-flash",
]

discovered = []
if api_key:
    url = f"https://googleapis.com{api_key}"
    try:
        with urllib.request.urlopen(url, timeout=15) as resp:
            body = resp.read().decode("utf-8", errors="ignore")
            data = json.loads(body) if body.strip() else {}
            for m in data.get("models", []):
                name = m.get("name", "").replace("models/", "")
                methods = m.get("supportedGenerationMethods", [])
                if (
                    "generateContent" in methods
                    and "gemini" in name
                    and not any(x in name for x in ["image", "tts", "embedding", "aqa"])
                ):
                    discovered.append(name)
    except Exception as e:
        print(f"Warning: Could not fetch model list ({e}); using fallback list.")

models = []
for m in priority_models + discovered:
    if m and m not in models:
        models.append(m)

print("Candidate models to cycle:", ", ".join(models))

for model in models:
    print(f"\n=== Attempting Gemini CLI with model: {model} ===")
    res = subprocess.run(["gemini", "--yolo", "--model", model, "-p", prompt])
    if res.returncode == 0:
        print(f"=== Succeeded with {model} ===")
        sys.exit(0)
    print(f"=== Model {model} failed (exit {res.returncode}), rotating to next... ===")

print("All candidate models failed or exceeded quota.")
sys.exit(1)

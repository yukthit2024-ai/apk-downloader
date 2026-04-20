import os
import glob

workspace = r"x:\Projects_X\0_Active\8_Android_APK\APK_Downloader_APK_GITHUB"

replacements = [
    ("friendtracker", "apkdownloader"),
    ("FriendLocationTracker", "VypeenApkDownloader"),
    ("Friend Location Tracker", "Vypeen APK Downloader"),
    ("friends\\' locations", "Vypeen APK Downloader"),
    ("track friends\\' locations in real-time using the Matrix protocol.", "download APKs safely.")
]

files_to_check = []
for root, _, files in os.walk(workspace):
    if ".git" in root or "build" in root or "gradle" in root:
        continue
    for f in files:
        if f.endswith((".java", ".xml", ".gradle")):
            files_to_check.append(os.path.join(root, f))

for fp in files_to_check:
    with open(fp, "r", encoding="utf-8") as file:
        content = file.read()
    
    new_content = content
    for old, new in replacements:
        new_content = new_content.replace(old, new)
        
    if new_content != content:
        with open(fp, "w", encoding="utf-8") as file:
            file.write(new_content)
        print(f"Updated {fp}")

print("Done")

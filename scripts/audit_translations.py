import os
import re

presentation_dir = 'shared/src/commonMain/kotlin/com/panda/myapplication/presentation'

turkish_chars = set("çğıöşüÇĞİÖŞÜ")

def has_turkish(s):
    return any(c in turkish_chars for c in s)

# Common Turkish words that might not have special chars
tr_keywords = [
    "kapasite", "yorum", "detay", "sistem", "soğut", "seç", "kaldır", 
    "ekle", "başlat", "kapat", "kaydet", "filtre", "fiyat", "tavsiye",
    "kullanıcı", "değerlendirme", "itibar", "kazandırır", "sırala", "özellik",
    "karşılaştır", "seviye", "darboğaz", "performans", "ayar", "çözünürlük"
]

results = {}

for root, dirs, files in os.walk(presentation_dir):
    for file in files:
        if file.endswith('.kt') and file != 'AppStrings.kt':
            filepath = os.path.join(root, file)
            relpath = os.path.relpath(filepath, presentation_dir)
            with open(filepath, 'r', encoding='utf-8') as f:
                lines = f.readlines()
            
            file_issues = []
            for idx, line in enumerate(lines):
                # Look for string literals in Text("..."), title = "...", label = "...", etc.
                matches = re.findall(r'"([^"\\]*(?:\\.[^"\\]*)*)"', line)
                for m in matches:
                    if len(m.strip()) < 2:
                        continue
                    # Ignore import statements, log tags, debug prints, regexes, keys
                    if line.strip().startswith('import ') or line.strip().startswith('package '):
                        continue
                    if 'Log.' in line or 'println' in line:
                        continue
                    if m.startswith('http') or m.startswith('res/') or m.startswith('files/'):
                        continue
                    if re.match(r'^[a-z0-9_\-\.]+$', m): # typical keys or identifiers
                        continue

                    # Check if string contains Turkish characters or keywords
                    lower_m = m.lower()
                    is_tr = has_turkish(m) or any(k in lower_m for k in tr_keywords)
                    
                    # Check if it looks like hardcoded UI text (starts with capital or emoji or has spaces)
                    looks_like_ui = (' ' in m or re.match(r'^[A-ZÇĞİÖŞÜ].*', m) or any(ord(c) > 1000 for c in m))
                    
                    if is_tr:
                        file_issues.append((idx + 1, 'TR_HARDCODED', m, line.strip()))
                    elif looks_like_ui and ('text =' in line or 'title =' in line or 'label =' in line or 'Text(' in line or 'subtitle =' in line):
                        # Potential hardcoded English
                        file_issues.append((idx + 1, 'EN_HARDCODED', m, line.strip()))

            if file_issues:
                results[relpath] = file_issues

print(f"Total files with hardcoded UI strings: {len(results)}")
print("=" * 80)
# Group by directory/feature
by_screen = {}
for path, issues in sorted(results.items()):
    tr_count = sum(1 for _, kind, _, _ in issues if kind == 'TR_HARDCODED')
    en_count = sum(1 for _, kind, _, _ in issues if kind == 'EN_HARDCODED')
    print(f"{path}: {tr_count} Turkish, {en_count} English hardcoded (Total: {len(issues)})")

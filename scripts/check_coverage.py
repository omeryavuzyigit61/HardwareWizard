import json
import re

def sanitize(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")

with open("data/catalog/cpu.json", "r", encoding="utf-8") as f:
    cpus = json.load(f)

with open("data/hardware_benchmarks.json", "r", encoding="utf-8") as f:
    benchmarks = json.load(f)

matched_cpus = benchmarks.get("cpus", {})
missing_cpus = [c["name"] for c in cpus if f"cpu_{sanitize(c['name'])}" not in matched_cpus]
print(f"Total catalog CPUs: {len(cpus)}")
print(f"Missing CPUs ({len(missing_cpus)}):")
for m in missing_cpus:
    print(" -", m)

with open("data/catalog/video-card.json", "r", encoding="utf-8") as f:
    gpus = json.load(f)

matched_gpus = benchmarks.get("gpus", {})
missing_gpus = [g for g in gpus if f"gpu_{sanitize(g['name'])}" not in matched_gpus]
print(f"\nTotal catalog GPUs: {len(gpus)}")
print(f"Missing GPUs ({len(missing_gpus)}):")
chipsets_missing = set(x.get("chipset", "") for x in missing_gpus)
for cs in sorted(chipsets_missing):
    print(" - Chipset missing:", cs)

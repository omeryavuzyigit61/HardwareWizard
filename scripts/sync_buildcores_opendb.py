#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/sync_buildcores_opendb.py
Hardware Wizard - BuildCores OpenDB (github.com/buildcores/buildcores-open-db) Senkronizasyon Motoru

Tek bir HTTPS arsiv istegiyle (~16 MB tar.gz) 48.000+ acik donanim JSON kaydini bellekte okur;
Cloudflare veya IP engelleme riski olmadan 32 kategoriden gercek teknik verileri katalog
dosyalarindaki eksik/bos alanlara dinamik olarak isler.
"""

import io
import json
import os
import re
import sys
import tarfile
import urllib.error
import urllib.request
from collections import defaultdict
from typing import Any, Dict, Iterable, List, Optional, Set, Tuple

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
CATALOG_DIR = os.path.join(ROOT_DIR, "data", "catalog")
COMPOSE_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

OPENDB_TARBALL_URL = "https://codeload.github.com/buildcores/buildcores-open-db/tar.gz/refs/heads/main"

# composeResources yalnızca kanonik dosya adlarını tutar (autonomous_hardware_discovery.py ile uyumlu)
DUPLICATE_CATALOG_NAMES = {"gpu.json", "ram.json", "psu.json", "storage.json", "cooler.json"}

# Varyant ayırıcı tokenlar: biri varsa diğeriyle yanlış eşleşme engellenir
DISTINCT_VARIANT_TOKENS = {
    "ti", "super", "xt", "xtx", "gre", "x3d", "ultra", "pro", "plus", "max",
    "mini", "itx", "matx", "eatx", "atx", "lite", "se", "v2", "v3", "evo",
    "wifi", "wi-fi", "ddr4", "ddr5", "oc", "white", "black", "rgb", "oled",
    "mini-led", "4k", "qhd", "fhd", "1tb", "2tb", "4tb", "8tb", "500gb",
    "512gb", "256gb", "16gb", "32gb", "64gb", "48gb", "96gb", "128gb",
}

TARGET_OPENDB_FOLDERS = {
    "CPU", "GPU", "Motherboard", "RAM", "Storage", "PSU", "CPUCooler", "PCCase",
    "Mouse", "Keyboard", "Headphones", "Monitor", "Microphone", "Mousepad",
    "Webcam", "Chair", "CaseFan", "Desk", "Lighting",
}

PERIPHERAL_CATEGORY_TO_OPENDB = {
    "MOUSE": "Mouse",
    "KEYBOARD": "Keyboard",
    "HEADSET": "Headphones",
    "MONITOR": "Monitor",
    "MICROPHONE": "Microphone",
    "MOUSEPAD": "Mousepad",
    "WEBCAM": "Webcam",
    "GAMING_CHAIR": "Chair",
    "CASE_FAN": "CaseFan",
    "GAMING_DESK": "Desk",
    "GAMING_LIGHTING": "Lighting",
}


def norm_text(s: Any) -> str:
    if not isinstance(s, str):
        return ""
    s = s.lower().replace("_", " ").replace("-", " ").replace("/", " ")
    return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9.\s]+", " ", s)).strip()


def tokenize(s: Any) -> Set[str]:
    n = norm_text(s)
    if not n:
        return set()
    return {t for t in n.split() if len(t) >= 2}


def is_empty_value(val: Any) -> bool:
    if val is None:
        return True
    if isinstance(val, str):
        return val.strip() == "" or val.strip().upper() in {"N/A", "NONE", "UNKNOWN", "BELIRTILMEDI", "BELİRTİLMEDİ"}
    if isinstance(val, (int, float)) and not isinstance(val, bool):
        return val == 0
    if isinstance(val, (list, dict)):
        return len(val) == 0
    return False


def as_int(val: Any) -> Optional[int]:
    if isinstance(val, bool) or val is None:
        return None
    if isinstance(val, int):
        return val
    if isinstance(val, float):
        return int(round(val))
    return None


def set_if_missing(target: Dict[str, Any], key: str, value: Any) -> bool:
    if value is None or value == "" or value == [] or value == {}:
        return False
    if isinstance(value, (int, float)) and not isinstance(value, bool) and value == 0:
        return False
    if is_empty_value(target.get(key)):
        target[key] = value
        return True
    return False


def extract_brand_token(name: str, fallback: str = "") -> str:
    if fallback:
        return norm_text(fallback).split()[0] if norm_text(fallback) else ""
    tokens = norm_text(name).split()
    return tokens[0] if tokens else ""


class OpenDbIndex:
    """Bellek içi hızlı eşleştirme indeksi (ad, parça kodu, temiz token kesişimi)."""

    def __init__(self, records: List[Dict[str, Any]]):
        self.by_exact_name: Dict[str, Dict[str, Any]] = {}
        self.by_part_number: Dict[str, Dict[str, Any]] = {}
        self.by_brand: Dict[str, List[Tuple[Set[str], Dict[str, Any]]]] = defaultdict(list)

        for rec in records:
            meta = rec.get("metadata") or {}
            name = meta.get("name") or ""
            n_name = norm_text(name)
            if n_name and n_name not in self.by_exact_name:
                self.by_exact_name[n_name] = rec

            for pn in meta.get("part_numbers") or []:
                if isinstance(pn, str):
                    pn_clean = re.sub(r"[^A-Z0-9]", "", pn.upper())
                    if len(pn_clean) >= 6 and any(c.isdigit() for c in pn_clean):
                        self.by_part_number.setdefault(pn_clean, rec)

            brand = extract_brand_token(name, meta.get("manufacturer") or "")
            toks = tokenize(name)
            if brand and toks:
                self.by_brand[brand].append((toks, rec))

    def find_match(self, item_name: str, item_brand: str = "", item_model: str = "") -> Optional[Dict[str, Any]]:
        for candidate in (item_name, f"{item_brand} {item_model}".strip()):
            n = norm_text(candidate)
            if n and n in self.by_exact_name:
                return self.by_exact_name[n]

        upper_compact = re.sub(r"[^A-Z0-9]", "", (item_name + " " + item_model).upper())
        for pn, rec in self.by_part_number.items():
            if pn in upper_compact:
                return rec

        item_tokens = tokenize(item_name)
        if not item_tokens:
            return None

        brand = extract_brand_token(item_name, item_brand)
        candidates = self.by_brand.get(brand, [])
        if not candidates:
            return None

        item_num_tokens = {t for t in item_tokens if any(c.isdigit() for c in t)}
        item_variants = item_tokens & DISTINCT_VARIANT_TOKENS

        best_rec = None
        best_score = 0.0
        for rec_tokens, rec in candidates:
            if item_variants != (rec_tokens & DISTINCT_VARIANT_TOKENS):
                continue
            rec_num_tokens = {t for t in rec_tokens if any(c.isdigit() for c in t)}
            if item_num_tokens and rec_num_tokens and not (item_num_tokens <= rec_num_tokens or rec_num_tokens <= item_num_tokens):
                continue

            overlap = len(item_tokens & rec_tokens)
            if overlap < 2:
                continue
            union = len(item_tokens | rec_tokens)
            jaccard = overlap / union if union else 0.0
            containment = overlap / max(1, min(len(item_tokens), len(rec_tokens)))
            if jaccard >= 0.58 or (containment >= 0.85 and overlap >= 3):
                score = jaccard + containment
                if score > best_score:
                    best_score = score
                    best_rec = rec

        return best_rec


def fetch_opendb_tarball(url: str = OPENDB_TARBALL_URL) -> Optional[Dict[str, List[Dict[str, Any]]]]:
    print(f"[+] BuildCores OpenDB arşivi indiriliyor: {url}")
    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "HardwareWizard-OpenDB-Sync/1.0",
            "Accept": "application/gzip,application/octet-stream,*/*",
        },
    )
    try:
        with urllib.request.urlopen(req, timeout=35) as resp:
            raw_bytes = resp.read()
    except (urllib.error.URLError, TimeoutError, OSError) as exc:
        print(f"[WARN] BuildCores OpenDB indirilemedi (ağ/zaman aşımı): {exc}")
        return None

    size_mb = len(raw_bytes) / (1024 * 1024)
    print(f"    -> Tek istekte {size_mb:.2f} MB indirildi, bellekte ayrıştırılıyor...")

    buckets: Dict[str, List[Dict[str, Any]]] = defaultdict(list)
    with tarfile.open(fileobj=io.BytesIO(raw_bytes), mode="r:gz") as tar:
        for member in tar:
            if not member.isfile() or "/open-db/" not in member.name or not member.name.endswith(".json"):
                continue
            rel = member.name.split("/open-db/", 1)[1]
            parts = rel.split("/")
            if len(parts) != 2 or parts[0] not in TARGET_OPENDB_FOLDERS:
                continue
            extracted = tar.extractfile(member)
            if extracted is None:
                continue
            try:
                buckets[parts[0]].append(json.load(extracted))
            except Exception:
                continue

    total_loaded = sum(len(v) for v in buckets.values())
    print(f"    -> {len(buckets)} OpenDB kategorisinden {total_loaded} kayıt belleğe yüklendi.")
    return buckets


def format_non_zero_map(mapping: Any, labels: Dict[str, str]) -> str:
    if not isinstance(mapping, dict):
        return ""
    parts = []
    for k, label in labels.items():
        v = mapping.get(k)
        if isinstance(v, (int, float)) and v > 0:
            parts.append(f"{int(v)}x {label}")
    return ", ".join(parts)


def enrich_cpu_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    cores = rec.get("cores") or {}
    clocks = rec.get("clocks") or {}
    perf_clk = clocks.get("performance") or {}
    cache = rec.get("cache") or {}
    specs = rec.get("specifications") or {}
    igpu = specs.get("integratedGraphics") or {}
    mem = specs.get("memory") or {}

    changed += int(set_if_missing(item, "socket", rec.get("socket")))
    changed += int(set_if_missing(item, "microarchitecture", rec.get("microarchitecture")))
    changed += int(set_if_missing(item, "core_count", as_int(cores.get("total"))))
    changed += int(set_if_missing(item, "threads", as_int(cores.get("threads"))))
    changed += int(set_if_missing(item, "core_clock", perf_clk.get("base")))
    changed += int(set_if_missing(item, "boost_clock", perf_clk.get("boost")))
    changed += int(set_if_missing(item, "l2_cache", cache.get("l2")))
    changed += int(set_if_missing(item, "l3_cache", as_int(cache.get("l3"))))
    changed += int(set_if_missing(item, "tdp", as_int(specs.get("tdp"))))
    changed += int(set_if_missing(item, "process_node", specs.get("lithography")))

    igpu_model = igpu.get("model")
    if isinstance(igpu_model, str) and igpu_model.strip().lower() not in {"", "none"}:
        changed += int(set_if_missing(item, "igpu_model", igpu_model.strip()))
        changed += int(set_if_missing(item, "graphics", igpu_model.strip()))

    mem_types = [str(m) for m in (mem.get("types") or []) if m]
    if mem_types:
        changed += int(set_if_missing(item, "max_memory_speed", " / ".join(mem_types)))

    return changed


def enrich_gpu_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    changed += int(set_if_missing(item, "chipset", rec.get("chipset")))
    changed += int(set_if_missing(item, "memory", as_int(rec.get("memory"))))
    changed += int(set_if_missing(item, "memory_type", rec.get("memory_type")))
    changed += int(set_if_missing(item, "core_clock", as_int(rec.get("core_base_clock"))))
    changed += int(set_if_missing(item, "boost_clock", as_int(rec.get("core_boost_clock"))))
    changed += int(set_if_missing(item, "tdp", as_int(rec.get("tdp"))))
    changed += int(set_if_missing(item, "length", rec.get("length")))
    changed += int(set_if_missing(item, "bus_width", as_int(rec.get("memory_bus"))))

    eff_mem = rec.get("effective_memory_clock")
    if isinstance(eff_mem, (int, float)) and eff_mem > 0:
        gbps = round(eff_mem / 1000.0, 2) if eff_mem > 100 else float(eff_mem)
        changed += int(set_if_missing(item, "memory_speed", gbps))

    slot_w = rec.get("total_slot_width")
    if isinstance(slot_w, (int, float)) and slot_w > 0:
        changed += int(set_if_missing(item, "slot_width", f"{slot_w:g} Slot"))

    pwr_str = format_non_zero_map(
        rec.get("power_connectors"),
        {
            "pcie_12V_2x6": "16-Pin 12V-2x6",
            "pcie_12VHPWR": "16-Pin 12VHPWR",
            "pcie_8_pin": "8-Pin PCIe",
            "pcie_6_pin": "6-Pin PCIe",
        },
    )
    if pwr_str:
        changed += int(set_if_missing(item, "power_connectors", pwr_str))

    disp_str = format_non_zero_map(
        rec.get("video_outputs"),
        {
            "displayport_2_1a": "DisplayPort 2.1a",
            "displayport_2_1": "DisplayPort 2.1",
            "displayport_1_4a": "DisplayPort 1.4a",
            "hdmi_2_1": "HDMI 2.1",
            "hdmi_2_0": "HDMI 2.0",
            "dvi_d": "DVI-D",
            "vga": "VGA",
        },
    )
    if disp_str:
        changed += int(set_if_missing(item, "display_outputs", disp_str))

    return changed


def enrich_motherboard_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    mem = rec.get("memory") or {}
    changed += int(set_if_missing(item, "socket", rec.get("socket")))
    changed += int(set_if_missing(item, "chipset", rec.get("chipset")))
    changed += int(set_if_missing(item, "form_factor", rec.get("form_factor")))
    changed += int(set_if_missing(item, "max_memory", as_int(mem.get("max"))))
    changed += int(set_if_missing(item, "memory_slots", as_int(mem.get("slots"))))
    changed += int(set_if_missing(item, "memory_type", mem.get("ram_type")))

    pcie_slots = rec.get("pcie_slots") or []
    if isinstance(pcie_slots, list) and pcie_slots:
        gens = []
        total_pcie = 0
        for s in pcie_slots:
            if not isinstance(s, dict):
                continue
            qty = int(s.get("quantity") or 1)
            total_pcie += qty
            try:
                gens.append(float(s.get("gen") or 0))
            except (TypeError, ValueError):
                pass
        if total_pcie > 0:
            changed += int(set_if_missing(item, "pcie_slots", total_pcie))
        if gens and max(gens) > 0:
            changed += int(set_if_missing(item, "pcie_generation", f"PCIe {max(gens):g}"))

    m2_slots = rec.get("m2_slots") or []
    if isinstance(m2_slots, list) and m2_slots:
        changed += int(set_if_missing(item, "m2_slots", len(m2_slots)))
        descs = []
        for slot in m2_slots:
            if isinstance(slot, dict):
                iface = slot.get("interface") or ""
                size = slot.get("size") or ""
                if iface or size:
                    descs.append(f"{iface} ({size})".strip())
        if descs:
            changed += int(set_if_missing(item, "m2_slots_detail", ", ".join(descs[:4])))

    storage_devs = rec.get("storage_devices") or {}
    sata_6 = int(storage_devs.get("sata_6_gb_s") or 0)
    sata_3 = int(storage_devs.get("sata_3_gb_s") or 0)
    if sata_6 + sata_3 > 0:
        changed += int(set_if_missing(item, "sata_ports", sata_6 + sata_3))

    net_parts = []
    for eth in rec.get("onboard_ethernet") or []:
        if isinstance(eth, dict) and eth.get("speed"):
            ctrl = eth.get("controller") or ""
            net_parts.append(f"{eth['speed']} {ctrl}".strip())
    wifi = rec.get("wireless_networking")
    if isinstance(wifi, str) and wifi.strip() and wifi.strip().lower() != "none":
        net_parts.append(wifi.strip())
        if item.get("has_wifi") is None:
            item["has_wifi"] = True
            changed += 1
    if net_parts:
        changed += int(set_if_missing(item, "network", " + ".join(net_parts)))

    return changed


def enrich_ram_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    speed_mhz = rec.get("speed")
    ram_type = (rec.get("ram_type") or "").upper()
    if isinstance(speed_mhz, (int, float)) and speed_mhz > 0:
        ddr_num = 5 if "DDR5" in ram_type else (4 if "DDR4" in ram_type else (3 if "DDR3" in ram_type else None))
        speed_list = [ddr_num, int(speed_mhz)] if ddr_num else [int(speed_mhz)]
        changed += int(set_if_missing(item, "speed", speed_list))

    mods = rec.get("modules") or {}
    qty = mods.get("quantity")
    cap_gb = mods.get("capacity_gb")
    if isinstance(qty, int) and isinstance(cap_gb, int) and qty > 0 and cap_gb > 0:
        changed += int(set_if_missing(item, "modules", [qty, cap_gb]))

    cl = rec.get("cas_latency")
    changed += int(set_if_missing(item, "cas_latency", cl))
    changed += int(set_if_missing(item, "timings", rec.get("timings")))
    changed += int(set_if_missing(item, "voltage", rec.get("voltage")))
    if item.get("heat_spreader") is None and rec.get("heat_spreader") is not None:
        item["heat_spreader"] = bool(rec["heat_spreader"])
        changed += 1
    if item.get("rgb") is None and rec.get("rgb") is not None:
        item["rgb"] = bool(rec["rgb"])
        changed += 1

    eff_speed = item.get("speed")
    spd_val = eff_speed[1] if isinstance(eff_speed, list) and len(eff_speed) >= 2 else (eff_speed[0] if isinstance(eff_speed, list) and len(eff_speed) == 1 else 0)
    eff_cl = item.get("cas_latency") or 0
    if isinstance(spd_val, (int, float)) and isinstance(eff_cl, (int, float)) and spd_val > 0 and eff_cl > 0:
        fwl = round(2000.0 * float(eff_cl) / float(spd_val), 2)
        changed += int(set_if_missing(item, "first_word_latency", fwl))

    return changed


def enrich_storage_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    changed += int(set_if_missing(item, "capacity", as_int(rec.get("capacity"))))
    s_type = "NVMe SSD" if rec.get("nvme") else rec.get("storage_type")
    changed += int(set_if_missing(item, "type", s_type))
    changed += int(set_if_missing(item, "form_factor", rec.get("form_factor")))
    changed += int(set_if_missing(item, "interface", rec.get("interface")))
    return changed


def enrich_psu_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    changed += int(set_if_missing(item, "wattage", as_int(rec.get("wattage"))))
    changed += int(set_if_missing(item, "type", rec.get("form_factor")))
    changed += int(set_if_missing(item, "efficiency", rec.get("efficiency_rating")))
    changed += int(set_if_missing(item, "modular", rec.get("modular")))

    conns = rec.get("connectors") or {}
    if isinstance(conns, dict):
        if int(conns.get("pcie_12vhpwr") or 0) > 0:
            changed += int(set_if_missing(item, "pcie_12vhpwr", f"{int(conns['pcie_12vhpwr'])}x 16-Pin 12VHPWR"))
        if int(conns.get("eps_8_pin") or 0) > 0:
            changed += int(set_if_missing(item, "eps_connectors", f"{int(conns['eps_8_pin'])}x 8-Pin EPS"))
        if int(conns.get("pcie_6_plus_2_pin") or 0) > 0:
            changed += int(set_if_missing(item, "pcie_connectors", f"{int(conns['pcie_6_plus_2_pin'])}x 6+2-Pin PCIe"))
        if int(conns.get("sata") or 0) > 0:
            changed += int(set_if_missing(item, "sata_connectors", f"{int(conns['sata'])}x SATA"))

    return changed


def enrich_cooler_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    min_rpm = rec.get("min_fan_rpm")
    max_rpm = rec.get("max_fan_rpm")
    if isinstance(max_rpm, (int, float)) and max_rpm > 0:
        rpm_str = f"{int(min_rpm)} - {int(max_rpm)} RPM" if isinstance(min_rpm, (int, float)) and min_rpm > 0 else f"{int(max_rpm)} RPM"
        changed += int(set_if_missing(item, "fan_rpm", rpm_str))

    noise = rec.get("max_noise_level") or rec.get("min_noise_level")
    changed += int(set_if_missing(item, "noise_level", noise))
    changed += int(set_if_missing(item, "height", as_int(rec.get("height"))))
    changed += int(set_if_missing(item, "size", as_int(rec.get("radiator_size"))))

    sockets = [str(s).strip() for s in (rec.get("cpu_sockets") or []) if s]
    if sockets:
        changed += int(set_if_missing(item, "sockets", sockets))

    if is_empty_value(item.get("type")):
        c_type = "Liquid Cooler" if rec.get("water_cooled") else "Air Cooler"
        changed += int(set_if_missing(item, "type", c_type))

    return changed


def enrich_case_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    changed = 0
    changed += int(set_if_missing(item, "type", rec.get("form_factor")))
    changed += int(set_if_missing(item, "side_panel", rec.get("side_panel")))
    changed += int(set_if_missing(item, "external_volume", rec.get("volume")))
    changed += int(set_if_missing(item, "max_gpu_length", as_int(rec.get("max_video_card_length"))))
    changed += int(set_if_missing(item, "max_cpu_cooler_height", as_int(rec.get("max_cpu_cooler_height"))))

    usb = rec.get("front_panel_usb")
    if not usb and isinstance(rec.get("front_usb_ports"), list) and rec["front_usb_ports"]:
        usb = ", ".join(str(x) for x in rec["front_usb_ports"] if x)
    changed += int(set_if_missing(item, "front_panel_usb", usb))

    bays = []
    if int(rec.get("internal_3_5_bays") or 0) > 0:
        bays.append(f"{int(rec['internal_3_5_bays'])}x 3.5 Inch")
    if int(rec.get("internal_2_5_bays") or 0) > 0:
        bays.append(f"{int(rec['internal_2_5_bays'])}x 2.5 Inch")
    if bays:
        changed += int(set_if_missing(item, "drive_bays", ", ".join(bays)))

    return changed


def join_list_str(val: Any) -> str:
    if isinstance(val, list):
        return ", ".join(str(x).strip() for x in val if x)
    if isinstance(val, str):
        return val.strip()
    return ""


def enrich_peripheral_item(item: Dict[str, Any], rec: Dict[str, Any]) -> int:
    cat = (item.get("category") or "").upper()
    changed = 0

    if cat == "MOUSE":
        changed += int(set_if_missing(item, "sensor", rec.get("sensor")))
        changed += int(set_if_missing(item, "max_dpi", as_int(rec.get("max_dpi"))))
        pr = rec.get("polling_rate")
        if isinstance(pr, list) and pr:
            pr = max(x for x in pr if isinstance(x, (int, float)))
        changed += int(set_if_missing(item, "polling_rate_hz", as_int(pr)))
        changed += int(set_if_missing(item, "switch_type", rec.get("switches") or rec.get("switch_type")))
        changed += int(set_if_missing(item, "weight_g", as_int(rec.get("weight"))))
        changed += int(set_if_missing(item, "connectivity", join_list_str(rec.get("connectivity"))))
        changed += int(set_if_missing(item, "battery_hours", as_int(rec.get("battery_life"))))
        changed += int(set_if_missing(item, "shape_type", rec.get("shape") or join_list_str(rec.get("grip_types"))))

    elif cat == "KEYBOARD":
        changed += int(set_if_missing(item, "switch_type", join_list_str([rec.get("switch"), rec.get("switch_type")])))
        changed += int(set_if_missing(item, "layout", rec.get("layout")))
        changed += int(set_if_missing(item, "polling_rate_hz", as_int(rec.get("polling_rate"))))
        changed += int(set_if_missing(item, "connectivity", join_list_str(rec.get("connectivity"))))
        changed += int(set_if_missing(item, "case_material", rec.get("frame_material")))
        if item.get("hot_swap") is None and rec.get("hot_swappable") is not None:
            item["hot_swap"] = bool(rec["hot_swappable"])
            changed += 1

    elif cat == "HEADSET":
        changed += int(set_if_missing(item, "driver_size_mm", as_int(rec.get("driver_size"))))
        f_low = rec.get("frequency_response_low")
        f_high = rec.get("frequency_response_high")
        if f_low and f_high:
            changed += int(set_if_missing(item, "frequency_response", f"{f_low}Hz - {f_high}Hz"))
        changed += int(set_if_missing(item, "mic_type", rec.get("microphone_type")))
        changed += int(set_if_missing(item, "weight_g", as_int(rec.get("weight"))))
        changed += int(set_if_missing(item, "connectivity", join_list_str(rec.get("connection_types"))))

    elif cat == "MONITOR":
        changed += int(set_if_missing(item, "panel_type", rec.get("panel_type")))
        changed += int(set_if_missing(item, "screen_size_inch", rec.get("screen_size")))
        res = rec.get("resolution")
        if isinstance(res, dict) and res.get("horizontalRes") and res.get("verticalRes"):
            changed += int(set_if_missing(item, "resolution", f"{res['horizontalRes']} x {res['verticalRes']}"))
        changed += int(set_if_missing(item, "refresh_rate_hz", as_int(rec.get("refresh_rate"))))
        changed += int(set_if_missing(item, "response_speed_gtg_ms", rec.get("response_time")))
        changed += int(set_if_missing(item, "response_mprt_ms", rec.get("mprt_response_time")))
        changed += int(set_if_missing(item, "brightness_sdr_nit", as_int(rec.get("max_brightness"))))
        changed += int(set_if_missing(item, "hdr_certification", rec.get("hdr")))
        changed += int(set_if_missing(item, "vrr_support", rec.get("adaptive_sync")))

    elif cat == "MICROPHONE":
        changed += int(set_if_missing(item, "polar_pattern", join_list_str(rec.get("polar_pattern"))))
        changed += int(set_if_missing(item, "connectivity", join_list_str(rec.get("connectivity_type"))))

    elif cat == "MOUSEPAD":
        changed += int(set_if_missing(item, "surface_type", rec.get("surface_type") or rec.get("surface_material")))
        changed += int(set_if_missing(item, "base_material", rec.get("base_material")))
        dims = rec.get("dimensions_mm")
        if isinstance(dims, dict) and dims.get("width") and dims.get("depth"):
            changed += int(set_if_missing(item, "dimensions_mm", f"{dims['width']} x {dims['depth']} mm"))

    elif cat == "WEBCAM":
        res = join_list_str(rec.get("resolution"))
        changed += int(set_if_missing(item, "max_resolution", res))
        fps = rec.get("frame_rate")
        if isinstance(fps, list) and fps:
            nums = [int(x) for x in fps if isinstance(x, (int, float)) or str(x).isdigit()]
            if nums:
                changed += int(set_if_missing(item, "max_frame_rate_fps", max(nums)))
        changed += int(set_if_missing(item, "connection_type", join_list_str(rec.get("connectivity_type"))))

    elif cat == "GAMING_CHAIR":
        changed += int(set_if_missing(item, "upholstery_material", join_list_str(rec.get("seat_surface_materials") or rec.get("backrest_surface_materials"))))
        changed += int(set_if_missing(item, "lumbar_support_type", rec.get("lumbar_support")))
        changed += int(set_if_missing(item, "armrest_type", rec.get("armrest_adjustability")))
        changed += int(set_if_missing(item, "max_weight_capacity_kg", as_int(rec.get("max_user_weight_kg"))))

    elif cat == "CASE_FAN":
        changed += int(set_if_missing(item, "fan_size_mm", as_int(rec.get("size"))))
        changed += int(set_if_missing(item, "max_airflow_cfm", rec.get("max_airflow") or rec.get("min_airflow")))
        changed += int(set_if_missing(item, "static_pressure_mmh2o", rec.get("static_pressure")))
        changed += int(set_if_missing(item, "noise_level_dba", rec.get("max_noise_level") or rec.get("min_noise_level")))

    elif cat == "GAMING_DESK":
        surf = rec.get("work_surface_dimensions_mm") or {}
        if isinstance(surf, dict):
            if isinstance(surf.get("width"), (int, float)) and surf["width"] > 0:
                changed += int(set_if_missing(item, "desk_width_cm", int(round(surf["width"] / 10.0))))
            if isinstance(surf.get("depth"), (int, float)) and surf["depth"] > 0:
                changed += int(set_if_missing(item, "desk_depth_cm", int(round(surf["depth"] / 10.0))))
        overall = rec.get("overall_dimensions_mm") or {}
        if isinstance(overall, dict):
            if isinstance(overall.get("height_min"), (int, float)) and overall["height_min"] > 0:
                changed += int(set_if_missing(item, "min_height_cm", int(round(overall["height_min"] / 10.0))))
            if isinstance(overall.get("height_max"), (int, float)) and overall["height_max"] > 0:
                changed += int(set_if_missing(item, "max_height_cm", int(round(overall["height_max"] / 10.0))))
        if item.get("is_motorized_height") is None and isinstance(rec.get("height_adjustment_type"), str):
            item["is_motorized_height"] = "electric" in rec["height_adjustment_type"].lower()
            changed += 1
        changed += int(set_if_missing(item, "max_load_kg", as_int(rec.get("max_supported_weight_kg"))))

    elif cat == "GAMING_LIGHTING":
        changed += int(set_if_missing(item, "lighting_form_factor", rec.get("fixture_kind")))
        temp_k = rec.get("white_color_temperature_k")
        if temp_k:
            changed += int(set_if_missing(item, "color_temp_kelvin", str(temp_k)))

    return changed


def load_json(path: str) -> Optional[List[Dict[str, Any]]]:
    if not os.path.exists(path):
        return None
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data if isinstance(data, list) else None


def save_dual_json(filenames: Iterable[str], data: List[Dict[str, Any]]) -> None:
    os.makedirs(CATALOG_DIR, exist_ok=True)
    os.makedirs(COMPOSE_DIR, exist_ok=True)
    for fn in filenames:
        cat_path = os.path.join(CATALOG_DIR, fn)
        with open(cat_path, "w", encoding="utf-8") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
        if fn not in DUPLICATE_CATALOG_NAMES:
            comp_path = os.path.join(COMPOSE_DIR, fn)
            with open(comp_path, "w", encoding="utf-8") as f:
                json.dump(data, f, ensure_ascii=False, indent=2)


def main() -> int:
    print("=" * 80)
    print("  HARDWARE WIZARD - BUILDCORES OPENDB DİNAMİK TEKNİK VERİ SENKRONİZASYONU")
    print("=" * 80)

    buckets = fetch_opendb_tarball()
    if not buckets:
        print("[WARN] OpenDB senkronizasyonu atlandı (mevcut katalog dosyaları korundu).")
        return 0

    indexes = {folder: OpenDbIndex(records) for folder, records in buckets.items()}

    pc_pipelines = [
        (["cpu.json"], "CPU", enrich_cpu_item),
        (["gpu.json", "video-card.json"], "GPU", enrich_gpu_item),
        (["motherboard.json"], "Motherboard", enrich_motherboard_item),
        (["ram.json", "memory.json"], "RAM", enrich_ram_item),
        (["storage.json", "internal-hard-drive.json"], "Storage", enrich_storage_item),
        (["psu.json", "power-supply.json"], "PSU", enrich_psu_item),
        (["cooler.json", "cpu-cooler.json"], "CPUCooler", enrich_cooler_item),
        (["case.json"], "PCCase", enrich_case_item),
    ]

    for filenames, folder, enrich_fn in pc_pipelines:
        primary_path = os.path.join(CATALOG_DIR, filenames[0])
        items = load_json(primary_path)
        idx = indexes.get(folder)
        if not items or not idx:
            continue

        matched_count = 0
        fields_updated = 0
        for item in items:
            rec = idx.find_match(item.get("name", ""), "", item.get("model", ""))
            if rec:
                matched_count += 1
                fields_updated += enrich_fn(item, rec)

        if fields_updated > 0:
            save_dual_json(filenames, items)
        print(f"✓ [{filenames[0]}] OpenDB ({folder}): {matched_count}/{len(items)} eşleşme, +{fields_updated} teknik alan güncellendi.")

    # Çevre birimleri (peripherals.json)
    per_path = os.path.join(CATALOG_DIR, "peripherals.json")
    per_items = load_json(per_path)
    if per_items:
        p_matched = 0
        p_fields = 0
        for item in per_items:
            cat = (item.get("category") or "").upper()
            folder = PERIPHERAL_CATEGORY_TO_OPENDB.get(cat)
            idx = indexes.get(folder) if folder else None
            if not idx:
                continue
            rec = idx.find_match(item.get("name", ""), item.get("brand", ""), item.get("model", ""))
            if rec:
                p_matched += 1
                p_fields += enrich_peripheral_item(item, rec)

        if p_fields > 0:
            save_dual_json(["peripherals.json"], per_items)
        print(f"✓ [peripherals.json] OpenDB Çevre Birimleri: {p_matched}/{len(per_items)} eşleşme, +{p_fields} teknik alan güncellendi.")

    print("=" * 80)
    return 0


if __name__ == "__main__":
    sys.exit(main())

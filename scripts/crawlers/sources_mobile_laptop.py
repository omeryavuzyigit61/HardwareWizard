"""Checkpointed multi-source crawler for Smartphones, Tablets, Smartwatches & Laptops."""

from __future__ import annotations

import csv
import json
import re
from typing import Any

from .http_client import BlockedAntiBotError, PoliteHttpClient
from .state_manager import CrawlStateManager, normalize_canonical_key

DXOMARK_RANKING_URL = "https://www.dxomark.com/smartphones/"
GEEKBENCH_MOBILE_URL = "https://browser.geekbench.com/mobile-benchmarks.json"
ANDROID_PLAY_DEVICES_CSV = "https://storage.googleapis.com/play_public/supported_devices.csv"
NOTEBOOKCHECK_LAPTOP_SEARCH = "https://www.notebookcheck.net/Laptop-Search.8223.0.html"
NOTEBOOKCHECK_MOBILE_CPU_URL = "https://www.notebookcheck.net/Mobile-Processors-Benchmark-List.2436.0.html"

GSMARENA_BRAND_PAGES = [
    ("Apple", "SMARTPHONE", "https://www.gsmarena.com/apple-phones-48.php"),
    ("Samsung", "SMARTPHONE", "https://www.gsmarena.com/samsung-phones-9.php"),
    ("Xiaomi", "SMARTPHONE", "https://www.gsmarena.com/xiaomi-phones-80.php"),
    ("Google", "SMARTPHONE", "https://www.gsmarena.com/google-phones-107.php"),
    ("OnePlus", "SMARTPHONE", "https://www.gsmarena.com/oneplus-phones-95.php"),
]


def crawl_mobile_and_laptop(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_gsmarena_devices: int = 20,
    delta_only: bool = False,
) -> dict[str, int]:
    counts: dict[str, int] = {}
    counts["dxomark"] = _crawl_dxomark_smartphones(http, state)
    counts["geekbench_mobile"] = _crawl_geekbench_mobile(http, state)
    counts["notebookcheck_cpu"] = _crawl_notebookcheck_mobile_cpus(http, state, delta_only=delta_only)
    counts["gsmarena_devices"] = _crawl_gsmarena_polite(
        http, state, max_devices=max_gsmarena_devices, delta_only=delta_only
    )
    return counts


def _crawl_dxomark_smartphones(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    task = "mobile:dxomark"
    prog = state.get_progress(task)
    try:
        html, status, etag, lm = http.get_text(
            DXOMARK_RANKING_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except BlockedAntiBotError as exc:
        state.log_audit("SMARTPHONE", "DXOMARK", "BLOCKED_ANTIBOT", 0, str(exc))
        return 0
    except Exception as exc:
        state.log_audit("SMARTPHONE", "DXOMARK", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not html:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    m = re.search(r'(\[\{"device":"smartphone".*?\}\])', html)
    if not m:
        state.log_audit("SMARTPHONE", "DXOMARK", "PARSE_MISS", 0, "No embedded ranking JSON")
        return 0

    devices = json.loads(m.group(1))
    rows: list[dict[str, Any]] = []
    for d in devices:
        name = (d.get("name") or "").strip()
        brand = (d.get("brand") or "").strip()
        if not name or not brand:
            continue

        def _score_of(prefix: str) -> int | None:
            v = d.get(f"{prefix}Score")
            if isinstance(v, (int, float)) and v > 0:
                return int(v)
            sub = d.get(prefix)
            if isinstance(sub, dict) and isinstance(sub.get("score"), (int, float)):
                return int(sub["score"])
            return None

        cam = _score_of("mobile")
        selfie = _score_of("selfie")
        display = _score_of("display")
        audio = _score_of("audio")
        battery = _score_of("battery")

        extra: dict[str, str] = {}
        if cam:
            extra["DXOMARK Arka Kamera Puanı"] = f"{cam} pts"
        if selfie:
            extra["DXOMARK Selfie (Ön) Kamera Puanı"] = f"{selfie} pts"
        if display:
            extra["DXOMARK Ekran Kalite Puanı"] = f"{display} pts"
        if battery:
            extra["DXOMARK Batarya Performans Puanı"] = f"{battery} pts"
        if audio:
            extra["DXOMARK Ses / Hoparlör Puanı"] = f"{audio} pts"
        if d.get("launch_date"):
            extra["Çıkış Tarihi"] = str(d["launch_date"])[:10]

        if not extra:
            continue
        img = d.get("image")
        images = [img] if isinstance(img, str) and img.startswith("http") else []
        rows.append(
            {
                "category": "SMARTPHONE",
                "source_name": "DXOMARK",
                "canonical_key": normalize_canonical_key(name),
                "display_name": name,
                "brand": brand,
                "payload": {
                    "dxomark_camera": cam,
                    "dxomark_selfie": selfie,
                    "dxomark_display": display,
                    "dxomark_battery": battery,
                    "dxomark_audio": audio,
                },
                "extra_specs": extra,
                "images": images,
                "source_url": f"https://www.dxomark.com{d['link']}" if d.get("link") else DXOMARK_RANKING_URL,
            }
        )

    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("SMARTPHONE", "DXOMARK", "OK", inserted, "Embedded ranking JSON")
    return inserted


def _crawl_geekbench_mobile(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    task = "mobile:geekbench"
    prog = state.get_progress(task)
    try:
        data, status, etag, lm = http.get_json(
            GEEKBENCH_MOBILE_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except Exception as exc:
        state.log_audit("SMARTPHONE", "Geekbench 6 Mobile", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not data:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    rows: list[dict[str, Any]] = []
    for dev in data.get("devices", []):
        name = (dev.get("name") or "").strip()
        sc = dev.get("score")
        mc = dev.get("multicore_score")
        if not name or not sc or not mc:
            continue
        cat = "TABLET" if any(k in name.lower() for k in ("ipad", "tab ", "pad ", "tablet")) else "SMARTPHONE"
        rows.append(
            {
                "category": cat,
                "source_name": "Geekbench 6 Mobile",
                "canonical_key": normalize_canonical_key(name),
                "display_name": name,
                "brand": name.split()[0],
                "payload": {
                    "geekbench_single": int(sc),
                    "geekbench_multi": int(mc),
                    "description": dev.get("description"),
                },
                "extra_specs": {
                    "Geekbench 6 Tek Çekirdek": str(sc),
                    "Geekbench 6 Çoklu Çekirdek": str(mc),
                },
                "images": [],
                "source_url": GEEKBENCH_MOBILE_URL,
            }
        )
    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("SMARTPHONE", "Geekbench 6 Mobile", "OK", inserted, "mobile-benchmarks.json")
    return inserted


def _crawl_notebookcheck_mobile_cpus(
    http: PoliteHttpClient, state: CrawlStateManager, delta_only: bool
) -> int:
    task = "mobile:notebookcheck:cpu"
    prog = state.get_progress(task)
    if delta_only and prog and prog["status"] == "DONE":
        return 0

    try:
        html, status, etag, lm = http.get_text(
            NOTEBOOKCHECK_MOBILE_CPU_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except Exception as exc:
        state.log_audit("LAPTOP", "Notebookcheck", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not html:
        return 0

    rows: list[dict[str, Any]] = []
    # Map header columns reliably instead of positional guesswork
    ths = [re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", th)).strip().lower() for th in re.findall(r"<th[^>]*>(.*?)</th>", html, re.S)]
    idx_model = next((i for i, h in enumerate(ths) if "model" in h), 1)
    idx_cores = next((i for i, h in enumerate(ths) if "cores" in h or "threads" in h), -1)
    idx_tdp = next((i for i, h in enumerate(ths) if "tdp" in h), -1)

    for tr in re.findall(r'<tr[^>]*class="[^"]*(?:odd|even)[^"]*"[^>]*>(.*?)</tr>', html, re.S):
        tds = [re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", td)).strip() for td in re.findall(r"<td[^>]*>(.*?)</td>", tr, re.S)]
        if len(tds) <= idx_model:
            continue
        pos = tds[0]
        model = tds[idx_model]
        if not model or len(model) < 3 or "Model" in model:
            continue
        cores_td = tds[idx_cores] if 0 <= idx_cores < len(tds) else ""
        tdp_td = tds[idx_tdp] if 0 <= idx_tdp < len(tds) else ""
        extra: dict[str, str] = {"Notebookcheck Mobil İşlemci Sırası": pos}
        if cores_td:
            extra["Çekirdek / İzlek (SoC)"] = cores_td
        if tdp_td and "mib" not in tdp_td.lower() and "mb" not in tdp_td.lower():
            extra["Mobil TDP / Güç Aralığı"] = tdp_td
        rows.append(
            {
                "category": "CPU_MOBILE_REF",
                "source_name": "Notebookcheck",
                "canonical_key": normalize_canonical_key(model),
                "display_name": model,
                "brand": model.split()[0],
                "payload": {"tdp": tdp_td, "cores": cores_td, "rank": pos},
                "extra_specs": extra,
                "images": [],
                "source_url": NOTEBOOKCHECK_MOBILE_CPU_URL,
            }
        )
        if len(rows) >= 500:
            break
    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("LAPTOP", "Notebookcheck", "OK", inserted, "Mobile CPU Benchmark List")
    return inserted


def _crawl_gsmarena_polite(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_devices: int = 20,
    delta_only: bool = False,
) -> int:
    """Polite checkpointed crawler for GSMArena brand indexes -> device specs. Halts immediately on Turnstile."""
    if max_devices <= 0:
        return 0

    crawled_count = 0
    for brand, cat, brand_url in GSMARENA_BRAND_PAGES:
        if crawled_count >= max_devices:
            break
        try:
            list_html, _, _, _ = http.get_text(brand_url)
        except BlockedAntiBotError as exc:
            state.log_audit(cat, "GSMArena", "BLOCKED_ANTIBOT", crawled_count, str(exc))
            return crawled_count
        except Exception as exc:
            state.log_audit(cat, "GSMArena", "ERROR", crawled_count, str(exc))
            continue

        # Extract device slugs cleanly from brand makers gallery
        device_links = re.findall(r'href="([a-z0-9_.-]+-\d+\.php)"[^>]*>.*?<span>([^<]+)</span>', list_html or "", re.S)
        for slug, label in device_links[:12]:
            if crawled_count >= max_devices:
                break
            title = f"{brand} {re.sub(r'<[^>]+>|\\s+', ' ', label).strip()}"
            ckey = normalize_canonical_key(title)
            if delta_only and state.has_source_key(cat, "GSMArena", ckey):
                continue

            dev_url = f"https://www.gsmarena.com/{slug}"
            try:
                dev_html, _, _, _ = http.get_text(dev_url)
            except BlockedAntiBotError as exc:
                state.log_audit(cat, "GSMArena", "BLOCKED_ANTIBOT", crawled_count, str(exc))
                return crawled_count
            except Exception:
                continue

            spec_map = _parse_gsmarena_device_page(dev_html or "")
            img_m = re.search(r'<div class="specs-photo-main">.*?<img[^>]*src="([^"]+)"', dev_html or "", re.S)
            imgs = [img_m.group(1)] if img_m else []

            target_cat = "TABLET" if any(k in title.lower() for k in ("ipad", "galaxy tab", "pad ")) else cat
            state.upsert_raw_batch(
                [
                    {
                        "category": target_cat,
                        "source_name": "GSMArena",
                        "canonical_key": ckey,
                        "display_name": title,
                        "brand": brand,
                        "payload": spec_map["payload"],
                        "extra_specs": spec_map["extra"],
                        "images": imgs,
                        "source_url": dev_url,
                    }
                ]
            )
            crawled_count += 1

    state.log_audit("SMARTPHONE", "GSMArena", "OK", crawled_count, "Polite device spec sync")
    return crawled_count


def _parse_gsmarena_device_page(html: str) -> dict[str, Any]:
    specs: dict[str, str] = {}
    for td_k, td_v in re.findall(r'<td class="ttl"[^>]*>(.*?)</td>\s*<td class="nfo"[^>]*>(.*?)</td>', html, re.S):
        k = re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", td_k)).strip()
        v = re.sub(r"\s+", " ", re.sub(r"<[^>]+>", " ", td_v)).strip()
        if k and v:
            specs[k] = v

    extra: dict[str, str] = {}
    for k_raw, tr_label in [
        ("USB", "USB Bağlantı Standardı"),
        ("WLAN", "Kablosuz Ağ (Wi-Fi)"),
        ("Bluetooth", "Bluetooth Sürümü"),
        ("Sensors", "Sensörler & Biyometri"),
        ("Dimensions", "Fiziksel Ölçüler"),
        ("SIM", "SIM / eSIM Desteği"),
        ("Display", "Ekran Özellikleri (Ham)"),
    ]:
        for sk, sv in specs.items():
            if sk.lower() == k_raw.lower():
                extra[tr_label] = sv[:140]

    payload: dict[str, Any] = {}
    chip = specs.get("Chipset")
    if chip:
        payload["chipset"] = chip.split("(")[0].strip()
    if specs.get("Weight"):
        m_g = re.search(r"(\d{2,4})\s*g", specs["Weight"])
        if m_g:
            payload["weight_grams"] = int(m_g.group(1))
    return {"payload": payload, "extra": extra}

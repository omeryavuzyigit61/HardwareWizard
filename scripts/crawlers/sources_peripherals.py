"""Checkpointed crawler for all 10 Gaming Peripherals & Setup categories."""

from __future__ import annotations

import json
import re
from typing import Any

from .http_client import BlockedAntiBotError, PoliteHttpClient
from .state_manager import CrawlStateManager, normalize_canonical_key

PCP_PERIPH_URLS = {
    "MONITOR": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/monitor.json",
    "MOUSE": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/mouse.json",
    "KEYBOARD": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/keyboard.json",
    "HEADSET": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/headphones.json",
    "WEBCAM": "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/webcam.json",
}

SENSOR_FYI_SENSORS_URL = "https://sensor.fyi/sensors/"
SENSOR_FYI_MICE_URL = "https://sensor.fyi/mice/"
AUTOEQ_INDEX_URL = "https://raw.githubusercontent.com/jaakkopasanen/AutoEq/master/results/INDEX.md"

SHOPIFY_SOURCES = [
    ("keychron", "https://www.keychron.com/products.json", "Keychron"),
    ("pulsar", "https://www.pulsar.gg/products.json", "Pulsar"),
    ("secretlab", "https://secretlab.co/products.json", "Secretlab"),
    ("lethalgaminggear", "https://www.lethalgaminggear.com/products.json", "LethalGamingGear"),
]


def crawl_peripherals(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_shopify_pages: int = 4,
    delta_only: bool = False,
) -> dict[str, int]:
    counts: dict[str, int] = {}
    counts["pcp_periph"] = _crawl_pcp_peripherals(http, state)
    counts["sensor_fyi"] = _crawl_sensor_fyi(http, state, delta_only=delta_only)
    counts["autoeq"] = _crawl_autoeq(http, state, delta_only=delta_only)
    counts["shopify"] = _crawl_shopify_manufacturers(
        http, state, max_pages=max_shopify_pages, delta_only=delta_only
    )
    return counts


def _crawl_pcp_peripherals(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    total = 0
    for cat, url in PCP_PERIPH_URLS.items():
        task = f"periph:pcp:{cat}"
        prog = state.get_progress(task)
        try:
            data, status, etag, lm = http.get_json(
                url,
                etag=prog["etag"] if prog else None,
                last_modified=prog["last_modified"] if prog else None,
            )
        except Exception as exc:
            state.log_audit(cat, "pc-part-dataset", "ERROR", 0, str(exc))
            continue

        if status == 304 or not data:
            state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
            continue

        rows: list[dict[str, Any]] = []
        for item in data:
            name = (item.get("name") or "").strip()
            if not name:
                continue
            brand = name.split()[0]
            payload: dict[str, Any] = {}
            extra: dict[str, str] = {}

            if cat == "MONITOR":
                res = item.get("resolution")
                res_str = f"{res[0]}x{res[1]}" if isinstance(res, list) and len(res) >= 2 else None
                size = item.get("screen_size")
                ppi = None
                if res_str and size:
                    try:
                        diag_px = (res[0] ** 2 + res[1] ** 2) ** 0.5
                        ppi = round(diag_px / float(size))
                    except Exception:
                        pass
                payload = {
                    "screen_size_inch": float(size) if size else None,
                    "resolution": res_str,
                    "refresh_rate_hz": int(item["refresh_rate"]) if item.get("refresh_rate") else None,
                    "response_speed_gtg_ms": float(item["response_time"]) if item.get("response_time") else None,
                    "panel_type": item.get("panel_type"),
                    "ppi": ppi,
                }
                if item.get("aspect_ratio"):
                    extra["En-Boy Oranı (Aspect Ratio)"] = str(item["aspect_ratio"])

            elif cat == "MOUSE":
                payload = {
                    "max_dpi": int(item["max_dpi"]) if item.get("max_dpi") else None,
                    "connectivity": item.get("connection_type"),
                    "shape_type": item.get("hand_orientation"),
                }
                if item.get("tracking_method"):
                    extra["İzleme Teknolojisi"] = str(item["tracking_method"])
                if item.get("color"):
                    extra["Renk"] = str(item["color"])

            elif cat == "KEYBOARD":
                layout = "TKL (%80)" if item.get("tenkeyless") is True else (
                    "Tam Boy (%100)" if item.get("tenkeyless") is False else None
                )
                payload = {
                    "switch_type": item.get("switches"),
                    "layout": layout,
                    "connectivity": item.get("connection_type"),
                }
                if item.get("backlit"):
                    extra["Arka Aydınlatma"] = str(item["backlit"])
                if item.get("style"):
                    extra["Tasarım Sınıfı"] = str(item["style"])

            elif cat == "HEADSET":
                freq = item.get("frequency_response")
                freq_str = (
                    f"{freq[0]}Hz - {freq[1]}kHz"
                    if isinstance(freq, list) and len(freq) >= 2 and freq[0] and freq[1]
                    else None
                )
                conn = "Kablosuz + 3.5mm" if item.get("wireless") else "Kablolu"
                payload = {
                    "driver_type": item.get("type"),
                    "frequency_response": freq_str,
                    "sound_isolation": item.get("enclosure_type"),
                    "connectivity": conn if item.get("wireless") is not None else None,
                    "mic_type": "Dahili Mikrofonlu" if item.get("microphone") else None,
                }
                if item.get("color"):
                    extra["Renk"] = str(item["color"])

            elif cat == "WEBCAM":
                res_list = item.get("resolutions") or []
                max_res = str(res_list[0]).upper() if res_list else None
                payload = {
                    "max_resolution": max_res,
                    "connection_type": item.get("connection"),
                    "focus_type": item.get("focus_type"),
                    "field_of_view_deg": int(item["fov"]) if item.get("fov") else None,
                }
                if res_list:
                    extra["Desteklenen Video Modları"] = ", ".join(str(x).upper() for x in res_list)
                if item.get("os"):
                    extra["İşletim Sistemi Desteği"] = ", ".join(item["os"])

            rows.append(
                {
                    "category": cat,
                    "source_name": "pc-part-dataset",
                    "canonical_key": normalize_canonical_key(name),
                    "display_name": name,
                    "brand": brand,
                    "payload": payload,
                    "extra_specs": extra,
                    "images": [],
                    "source_url": url,
                }
            )
        inserted = state.upsert_raw_batch(rows)
        total += inserted
        state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
        state.log_audit(cat, "pc-part-dataset", "OK", inserted, "GitHub JSON")
    return total


def _crawl_sensor_fyi(http: PoliteHttpClient, state: CrawlStateManager, delta_only: bool) -> int:
    task = "periph:sensor_fyi"
    prog = state.get_progress(task)
    if delta_only and prog and prog["status"] == "DONE":
        return 0

    sensors_by_name: dict[str, dict[str, Any]] = {}
    try:
        s_html, _, _, _ = http.get_text(SENSOR_FYI_SENSORS_URL)
        m_s = re.search(r"JSON\.parse\(`(\{.*?\})`\)", s_html or "", re.S)
        if m_s:
            s_obj = json.loads(m_s.group(1))
            for s in s_obj.get("sensors", []):
                sname = (s.get("name") or "").strip().lower().replace(" ", "")
                if sname:
                    sensors_by_name[sname] = s
    except Exception as exc:
        state.log_audit("MOUSE", "Sensor.fyi", "ERROR", 0, f"sensors: {exc}")

    try:
        m_html, _, _, _ = http.get_text(SENSOR_FYI_MICE_URL)
    except BlockedAntiBotError as exc:
        state.log_audit("MOUSE", "Sensor.fyi", "BLOCKED_ANTIBOT", 0, str(exc))
        return 0
    except Exception as exc:
        state.log_audit("MOUSE", "Sensor.fyi", "ERROR", 0, str(exc))
        return 0

    m_m = re.search(r"JSON\.parse\(`(\[.*?\])`\)", m_html or "", re.S)
    if not m_m:
        return 0

    mice = json.loads(m_m.group(1))
    rows: list[dict[str, Any]] = []
    for item in mice:
        brand = (item.get("brand") or "").strip()
        model = (item.get("model") or "").strip()
        if not brand or not model:
            continue
        full_name = f"{brand} {model}"
        dims = item.get("dimensions") or {}
        sensor_raw = (item.get("sensor") or "").strip()
        sensor_info = sensors_by_name.get(sensor_raw.lower().replace(" ", ""), {})

        extra: dict[str, str] = {}
        if dims.get("length") and dims.get("width") and dims.get("height"):
            extra["Boyutlar (U x G x Y)"] = f"{dims['length']} x {dims['width']} x {dims['height']} mm"
        if sensor_info.get("ips"):
            extra["Sensör İzleme Hızı (IPS)"] = f"{sensor_info['ips']} IPS"
        if sensor_info.get("accel"):
            extra["Maks. Hızlanma (İvme)"] = f"{sensor_info['accel']}G"
        if item.get("additional_buttons") is not None:
            extra["Ek Programlanabilir Tuş"] = str(item["additional_buttons"])
        if item.get("notes"):
            extra["Mühendislik Notu (Sensor.fyi)"] = str(item["notes"])

        lat = item.get("click_latency")
        lat_val = float(lat) if isinstance(lat, (int, float)) else None
        poll = item.get("polling_rate")
        poll_val = int(poll) if isinstance(poll, (int, float)) and poll > 0 else None
        wt = item.get("weight")
        wt_val = int(wt) if isinstance(wt, (int, float)) and wt > 0 else None
        cpi = sensor_info.get("cpi")
        cpi_val = int(cpi) if isinstance(cpi, (int, float)) and cpi > 0 else None

        rows.append(
            {
                "category": "MOUSE",
                "source_name": "Sensor.fyi",
                "canonical_key": normalize_canonical_key(full_name),
                "display_name": full_name,
                "brand": brand,
                "payload": {
                    "sensor": sensor_raw or None,
                    "max_dpi": cpi_val,
                    "polling_rate_hz": poll_val,
                    "weight_g": wt_val,
                    "click_latency_ms": lat_val,
                    "shape_type": item.get("shape"),
                },
                "extra_specs": extra,
                "images": [],
                "source_url": item.get("official_site") or SENSOR_FYI_MICE_URL,
            }
        )

    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted))
    state.log_audit("MOUSE", "Sensor.fyi", "OK", inserted, "Mice + optical sensors JSON")
    return inserted


def _crawl_autoeq(http: PoliteHttpClient, state: CrawlStateManager, delta_only: bool) -> int:
    task = "periph:autoeq"
    prog = state.get_progress(task)
    try:
        md, status, etag, lm = http.get_text(
            AUTOEQ_INDEX_URL,
            etag=prog["etag"] if prog else None,
            last_modified=prog["last_modified"] if prog else None,
        )
    except Exception as exc:
        state.log_audit("HEADSET", "AutoEq", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not md:
        state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
        return 0

    rows: list[dict[str, Any]] = []
    # Matches markdown bullet lines like: - [Model Name](./rtings/...) by Author (type)
    pattern = re.compile(r"^- \[([^\]]+)\]\(\./([^)]+)\)", re.M)
    seen: set[str] = set()
    for name, rel_path in pattern.findall(md):
        clean_name = re.sub(r"\s*\([^)]*sample[^)]*\)", "", name).strip()
        if not clean_name or len(clean_name) < 4:
            continue
        ckey = normalize_canonical_key(clean_name)
        if not ckey or ckey in seen:
            continue
        seen.add(ckey)

        parts = rel_path.split("/")
        meas_source = parts[0] if parts else "AutoEq"
        form_raw = parts[1] if len(parts) > 1 else ""
        driver_form = (
            "Over-Ear (Kulak Çevreleyen)" if "over-ear" in form_raw.lower()
            else ("In-Ear IEM (Kulak İçi)" if "in-ear" in form_raw.lower()
                  else ("On-Ear (Kulak Üstü)" if "on-ear" in form_raw.lower() else None))
        )
        rows.append(
            {
                "category": "HEADSET",
                "source_name": "AutoEq Acoustic DB",
                "canonical_key": ckey,
                "display_name": clean_name,
                "brand": clean_name.split()[0],
                "payload": {
                    "driver_type": driver_form,
                },
                "extra_specs": {
                    "AutoEq Akustik Ölçüm Kaynağı": meas_source,
                    "Ölçüm Profili": form_raw or "Doğrulanmış Frekans Yanıtı",
                    "Frekans Hedef Düzeltmesi": "Harman Target Parametrik EQ Doğrulamalı",
                },
                "images": [],
                "source_url": f"https://github.com/jaakkopasanen/AutoEq/tree/master/results/{rel_path}",
            }
        )
        if delta_only and len(rows) >= 2500:
            break

    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
    state.log_audit("HEADSET", "AutoEq Acoustic DB", "OK", inserted, "GitHub acoustic index")
    return inserted


def _crawl_shopify_manufacturers(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_pages: int = 4,
    delta_only: bool = False,
) -> int:
    total = 0
    for key_name, base_url, source_label in SHOPIFY_SOURCES:
        task = f"periph:shopify:{key_name}"
        prog = state.get_progress(task)
        start_page = 1 if delta_only else int(prog["cursor"] or 1) if (prog and prog["status"] == "IN_PROGRESS") else 1
        if delta_only and prog and prog["status"] == "DONE":
            # Re-check page 1 only in delta mode for newest releases
            page_range = [1]
        else:
            page_range = list(range(start_page, start_page + max_pages))

        for page in page_range:
            url = f"{base_url}?limit=250&page={page}"
            try:
                data, status, _, _ = http.get_json(url)
            except BlockedAntiBotError as exc:
                state.log_audit("PERIPHERAL", source_label, "BLOCKED_ANTIBOT", total, str(exc))
                break
            except Exception as exc:
                state.save_progress(task, "ERROR", cursor=str(page), note=str(exc))
                break

            products = (data or {}).get("products") or []
            if not products:
                state.save_progress(task, "DONE", cursor=str(page))
                break

            rows: list[dict[str, Any]] = []
            for p in products:
                title = (p.get("title") or "").strip()
                ptype = (p.get("product_type") or "").strip()
                tags = " ".join(p.get("tags") or []) if isinstance(p.get("tags"), list) else str(p.get("tags") or "")
                cat = _classify_shopify_product(key_name, title, ptype, tags)
                if not cat:
                    continue

                body_text = re.sub(r"<[^>]+>", " ", p.get("body_html") or "")
                body_text = re.sub(r"\s+", " ", body_text).strip()
                images = [img.get("src") for img in (p.get("images") or []) if img.get("src")][:6]
                vendor = (p.get("vendor") or source_label).strip()
                if vendor.lower() in {"lethalgaminggear", "lethal gaming gear"}:
                    vendor = title.split()[0]

                payload, extra = _extract_shopify_specs(cat, title, body_text)
                rows.append(
                    {
                        "category": cat,
                        "source_name": f"{source_label} Official",
                        "canonical_key": normalize_canonical_key(title),
                        "display_name": title,
                        "brand": vendor,
                        "payload": payload,
                        "extra_specs": extra,
                        "images": images,
                        "source_url": base_url.replace("/products.json", f"/products/{p.get('handle', '')}"),
                    }
                )
            inserted = state.upsert_raw_batch(rows)
            total += inserted
            state.save_progress(task, "IN_PROGRESS", cursor=str(page + 1))
            if len(products) < 250:
                state.save_progress(task, "DONE", cursor=str(page))
                break
    return total


def _classify_shopify_product(store_key: str, title: str, ptype: str, tags: str) -> str | None:
    low = f"{title} {ptype} {tags}".lower()
    if any(skip in low for skip in ("keycap set", "cable", "grip tape", "skates", "switch pack", "lubricant", "pillow only", "skin ", "cover", "wrist rest", "carry case", "coiled")):
        return None
    if store_key == "secretlab":
        if "magnus" in low or "desk" in low:
            return "GAMING_DESK"
        if any(k in low for k in ("titan", "omega", "neuechair", "chair")):
            return "GAMING_CHAIR"
        return None
    if store_key == "keychron":
        if any(k in low for k in ("mouse ", " m3", " m4", " m2", " m6", " m7")) and "keyboard" not in low:
            return "MOUSE"
        if any(k in low for k in ("keyboard", "q1", "q2", "q3", "q5", "q6", "v1", "v2", "k2", "k3", "k8", "he ")):
            return "KEYBOARD"
        return None
    if any(k in low for k in ("mousepad", "mouse pad", "deskmat", "desk mat", "artisan", "saturn", "venus", "neptune", "superglide pad")):
        return "MOUSEPAD"
    if any(k in low for k in ("keyboard", "wooting", "hall effect", "he60", "pcmk")):
        return "KEYBOARD"
    if any(k in low for k in ("mouse", "x2", "x2h", "xlite", "fes", "lamzu", "ninjutso", "op1", "xm2")):
        return "MOUSE"
    return None


def _extract_shopify_specs(cat: str, title: str, body: str) -> tuple[dict[str, Any], dict[str, str]]:
    payload: dict[str, Any] = {}
    extra: dict[str, str] = {}
    low = f"{title} {body}".lower()

    if cat == "MOUSE":
        m_w = re.search(r"\b(\d{2,3})\s*g(?:rams)?\b", low)
        if m_w and 25 <= int(m_w.group(1)) <= 160:
            payload["weight_g"] = int(m_w.group(1))
        m_hz = re.search(r"\b(1000|2000|4000|8000)\s*hz\b|\b([48])k\s*polling", low)
        if m_hz:
            payload["polling_rate_hz"] = int(m_hz.group(1)) if m_hz.group(1) else int(m_hz.group(2)) * 1000
        m_sen = re.search(r"\b(PAW\s*3950|PAW\s*3395|XS-1|HERO\s*2|Focus\s*Pro\s*\d+K?)\b", body, re.I)
        if m_sen:
            payload["sensor"] = m_sen.group(1).upper()

    elif cat == "KEYBOARD":
        is_he = any(k in low for k in ("hall effect", "magnetic switch", "rapid trigger"))
        if is_he:
            payload["is_hall_effect"] = True
            payload["rapid_trigger_supported"] = "rapid trigger" in low
        if "gasket" in low:
            extra["Montaj Yapısı (Mount)"] = "Gasket Mount"
        if "qmk" in low or "via" in low:
            extra["Firmware / Yazılım"] = "QMK / VIA / Web Configurator"
        if "hot-swap" in low or "hotswap" in low:
            payload["hot_swap"] = True

    elif cat == "MOUSEPAD":
        m_dim = re.search(r"(\d{3})\s*[x×]\s*(\d{3})(?:\s*[x×]\s*(\d(?:\.\d)?))?\s*mm", low)
        if m_dim:
            payload["dimensions_mm"] = f"{m_dim.group(1)} x {m_dim.group(2)} mm"
            if m_dim.group(3):
                payload["thickness_mm"] = float(m_dim.group(3))
        if "poron" in low:
            payload["base_material"] = "Japon Poron Taban"
        if "glass" in low:
            payload["surface_type"] = "Alüminosilikat Temperli Cam"
            payload["speed_glide_tier"] = "Glass Ultra-Fast"
        elif "cordura" in low:
            payload["surface_type"] = "Cordura Kumaş"
        for hard in ("XSOFT", "SOFT", "MID"):
            if re.search(rf"\b{hard}\b", body):
                extra["Taban Sertliği"] = hard
                break

    elif cat == "GAMING_CHAIR":
        if "cold-cure" in low or "cold cure" in low:
            extra["Sünger Teknolojisi"] = "Soğuk Döküm Köpük (Cold-Cure Foam)"
        if "4d armrest" in low or "4-direction" in low:
            payload["armrest_type"] = "4D Metal / Manyetik Kolçak"
        m_kg = re.search(r"(?:max|capacity|support)[^\d]{0,12}(\d{3})\s*kg", low)
        if m_kg:
            payload["max_weight_capacity_kg"] = int(m_kg.group(1))

    elif cat == "GAMING_DESK":
        if "magnus" in low:
            payload["surface_finish"] = "Tam Çelik Manyetik Kablo Kanallı Tabla"
            payload["has_cable_management"] = True
        if "motorized" in low or "sit-to-stand" in low or "standing" in low:
            payload["is_motorized_height"] = True

    return payload, extra

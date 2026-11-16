"""Checkpointed crawler for Networking (Router, Modem, AP, Mesh) & UPS categories."""

from __future__ import annotations

import re
from typing import Any

from .http_client import BlockedAntiBotError, PoliteHttpClient
from .state_manager import CrawlStateManager, normalize_canonical_key

OPENWRT_RELEASES_BASE = "https://downloads.openwrt.org/releases/24.10.0/targets/"
NUT_DDL_INDEX = "https://networkupstools.org/ddl/"
PCP_UPS_URL = "https://raw.githubusercontent.com/docyx/pc-part-dataset/main/data/json/ups.json"

OPENWRT_WIFI_KEYWORDS = {
    "mt7996": ("Wi-Fi 7 (802.11be)", "MediaTek Filogic 880 / MT7996"),
    "mt7988": ("Wi-Fi 7 (802.11be)", "MediaTek Filogic 880 (MT7988 Quad A73)"),
    "mt7986": ("Wi-Fi 6 (802.11ax)", "MediaTek Filogic 830 (MT7986 Quad A53)"),
    "mt7981": ("Wi-Fi 6 (802.11ax)", "MediaTek Filogic 820 (MT7981 Dual A53)"),
    "mt7915": ("Wi-Fi 6 (802.11ax)", "MediaTek MT7915"),
    "mt7916": ("Wi-Fi 6E (802.11ax)", "MediaTek MT7916"),
    "ath12k": ("Wi-Fi 7 (802.11be)", "Qualcomm Networking Pro Wi-Fi 7 (ath12k)"),
    "ath11k": ("Wi-Fi 6 / 6E (802.11ax)", "Qualcomm IPQ807x / IPQ60xx (ath11k)"),
    "ipq807": ("Wi-Fi 6 (AX3600+)", "Qualcomm IPQ8074 Quad-Core"),
    "mt7621": ("Wi-Fi 5 / AC", "MediaTek MT7621AT Dual-Core 880MHz"),
}


def crawl_networking_and_ups(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_nut_vendors: int = 8,
    max_openwrt_targets: int = 12,
    delta_only: bool = False,
) -> dict[str, int]:
    counts: dict[str, int] = {}
    counts["ups_pcp"] = _crawl_pcp_ups(http, state)
    counts["ups_nut"] = _crawl_nut_ddl(http, state, max_vendors=max_nut_vendors, delta_only=delta_only)
    counts["net_openwrt"] = _crawl_openwrt_targets(
        http, state, max_targets=max_openwrt_targets, delta_only=delta_only
    )
    return counts


def _crawl_pcp_ups(http: PoliteHttpClient, state: CrawlStateManager) -> int:
    task = "net_ups:pcp_ups"
    prog = state.get_progress(task)
    etag = prog["etag"] if prog else None
    last_mod = prog["last_modified"] if prog else None

    try:
        data, status, new_etag, new_lm = http.get_json(PCP_UPS_URL, etag=etag, last_modified=last_mod)
    except Exception as exc:
        state.log_audit("UPS", "pc-part-dataset", "ERROR", 0, str(exc))
        return 0

    if status == 304 or not data:
        state.save_progress(task, "DONE", cursor="unchanged", etag=new_etag, last_modified=new_lm)
        return 0

    rows: list[dict[str, Any]] = []
    for item in data:
        name = (item.get("name") or "").strip()
        if not name:
            continue
        brand = name.split()[0]
        va = item.get("capacity_va")
        watt = item.get("capacity_w")
        extra: dict[str, str] = {}
        if va and watt:
            try:
                pf = round(float(watt) / float(va), 2)
                if 0.4 <= pf <= 1.0:
                    extra["Güç Faktörü (Power Factor)"] = f"{pf} PF"
            except Exception:
                pass
        if "PFC" in name.upper() or "SINE" in name.upper():
            extra["Dalga Tipi"] = "Tam Sinüs (Pure Sine Wave)"
        rows.append(
            {
                "category": "UPS",
                "source_name": "pc-part-dataset",
                "canonical_key": normalize_canonical_key(name),
                "display_name": name,
                "brand": brand,
                "payload": {
                    "va_rating": int(va) if va else None,
                    "watt_capacity": int(watt) if watt else None,
                    "sinewave_type": extra.get("Dalga Tipi"),
                },
                "extra_specs": extra,
                "images": [],
                "source_url": PCP_UPS_URL,
            }
        )

    inserted = state.upsert_raw_batch(rows)
    state.save_progress(task, "DONE", cursor=str(inserted), etag=new_etag, last_modified=new_lm)
    state.log_audit("UPS", "pc-part-dataset", "OK", inserted, "Full UPS dataset")
    return inserted


def _crawl_nut_ddl(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_vendors: int = 8,
    delta_only: bool = False,
) -> int:
    """Crawls Network UPS Tools DDL vendor hardware matrices."""
    priority_vendors = ("APC", "Cyber_Power_Systems", "Eaton", "Tripp_Lite", "Liebert", "FSP", "Riello", "Vertiv")
    try:
        idx_html, _, _, _ = http.get_text(NUT_DDL_INDEX)
    except BlockedAntiBotError as exc:
        state.log_audit("UPS", "NUT UPS DDL", "BLOCKED_ANTIBOT", 0, str(exc))
        return 0
    except Exception as exc:
        state.log_audit("UPS", "NUT UPS DDL", "ERROR", 0, str(exc))
        return 0

    if not idx_html:
        return 0

    raw_hrefs = re.findall(r'href="([A-Za-z0-9_.-]+)/(?:index\.html)?"', idx_html)
    vendor_dirs = [f"{v}/" for v in dict.fromkeys(raw_hrefs) if v not in ("..", ".", "historic", "css", "images")]
    ordered = [f"{p}/" for p in priority_vendors if f"{p}/" in vendor_dirs]
    for v in vendor_dirs:
        if v not in ordered:
            ordered.append(v)

    total_saved = 0
    for vendor_dir in ordered[:max_vendors]:
        vendor_name = vendor_dir.rstrip("/").replace("_", " ")
        task = f"ups:nut:{vendor_dir.rstrip('/')}"
        prog = state.get_progress(task)
        if delta_only and prog and prog["status"] == "DONE":
            continue

        vendor_url = f"{NUT_DDL_INDEX}{vendor_dir}"
        try:
            vhtml, _, _, _ = http.get_text(vendor_url)
        except Exception as exc:
            state.save_progress(task, "ERROR", note=str(exc))
            continue

        model_links = re.findall(r'href="([A-Za-z0-9_.-]+\.html)"[^>]*>([^<]+)</a>', vhtml or "")
        rows: list[dict[str, Any]] = []
        seen: set[str] = set()
        for href, raw_label in model_links:
            if href in ("index.html",) or href.startswith("..") or "nut" in raw_label.lower():
                continue
            model = re.sub(r"\s+", " ", raw_label).strip()
            if len(model) < 2 or model.lower() in {"home", "download", "features"}:
                continue
            full_name = model if model.lower().startswith(vendor_name.lower()) else f"{vendor_name} {model}"
            ckey = normalize_canonical_key(full_name)
            if not ckey or ckey in seen:
                continue
            seen.add(ckey)

            va_match = re.search(r"(\d{3,5})\s*(?:VA|ES|BR|BX|SMT|SMX|SUA|CP|PR)", full_name, re.I)
            va_val = int(va_match.group(1)) if va_match and 300 <= int(va_match.group(1)) <= 20000 else None
            rows.append(
                {
                    "category": "UPS",
                    "source_name": "NUT UPS DDL",
                    "canonical_key": ckey,
                    "display_name": full_name,
                    "brand": vendor_name,
                    "payload": {
                        "va_rating": va_val,
                        "has_usb_interface": True,
                    },
                    "extra_specs": {
                        "NUT DDL Protokol Desteği": "Doğrulanmış USB HID / SNMP / Serial Sürücü",
                        "Referans Sayfası": f"{vendor_url}{href}",
                    },
                    "images": [],
                    "source_url": f"{vendor_url}{href}",
                }
            )
        inserted = state.upsert_raw_batch(rows)
        total_saved += inserted
        state.save_progress(task, "DONE", cursor=str(inserted))

    state.log_audit("UPS", "NUT UPS DDL", "OK", total_saved, f"Crawled {min(len(ordered), max_vendors)} vendors")
    return total_saved


def _crawl_openwrt_targets(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_targets: int = 12,
    delta_only: bool = False,
) -> int:
    """Crawls official OpenWrt 24.10 release target profiles.json (no Anubis PoW)."""
    high_value_targets = [
        "mediatek/filogic/",
        "mediatek/mt7622/",
        "qualcommax/ipq807x/",
        "qualcommax/ipq60xx/",
        "ipq806x/generic/",
        "ramips/mt7621/",
        "ipq40xx/generic/",
        "ath79/generic/",
        "lantiq/xrx200/",
        "bcm53xx/generic/",
        "mvebu/cortexa9/",
        "rockchip/armv8/",
    ]
    total_saved = 0
    for target in high_value_targets[:max_targets]:
        task = f"net:openwrt:{target}"
        prog = state.get_progress(task)
        url = f"{OPENWRT_RELEASES_BASE}{target}profiles.json"
        if delta_only and prog and prog["status"] == "DONE":
            # Conditional check via ETag if supported
            pass

        try:
            data, status, etag, lm = http.get_json(
                url,
                etag=prog["etag"] if prog else None,
                last_modified=prog["last_modified"] if prog else None,
            )
        except Exception as exc:
            state.save_progress(task, "ERROR", note=str(exc))
            continue

        if status == 304 or not data:
            state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
            continue

        profiles = data.get("profiles", {})
        target_name = data.get("target") or target.strip("/")
        rows: list[dict[str, Any]] = []
        for prof_id, pdata in profiles.items():
            titles = pdata.get("titles") or []
            if not titles:
                continue
            t0 = titles[0]
            vendor = (t0.get("vendor") or "").strip()
            model = (t0.get("model") or "").strip()
            variant = (t0.get("variant") or "").strip()
            if not vendor or not model:
                continue
            full_name = f"{vendor} {model}" + (f" ({variant})" if variant else "")
            pkgs = " ".join(pdata.get("device_packages") or [])

            category = _classify_network_device(full_name, prof_id, target_name)
            wifi_std, soc_desc = _infer_openwrt_radio_and_soc(pkgs, target_name)

            payload = {
                "wifi_standard": wifi_std,
                "chipset": soc_desc,
                "processor_and_ram": soc_desc,
                "vdsl_profile": "VDSL2 Vectoring (35b/17a)" if category == "MODEM" else None,
                "has_sqm_bufferbloat": True,
            }
            extra = {
                "OpenWrt 24.10 Desteği": "Resmi Destekli (CAKE SQM & OpenWrt Kernel)",
                "Hedef Mimari (Target)": target_name,
                "Cihaz Yonga & Radyo Paketleri": ", ".join((pdata.get("device_packages") or [])[:6]) or target_name,
                "Board ID": ", ".join(pdata.get("supported_devices") or [prof_id]),
            }
            rows.append(
                {
                    "category": category,
                    "source_name": "OpenWrt Releases",
                    "canonical_key": normalize_canonical_key(full_name),
                    "display_name": full_name,
                    "brand": vendor,
                    "payload": payload,
                    "extra_specs": extra,
                    "images": [],
                    "source_url": url,
                }
            )
        inserted = state.upsert_raw_batch(rows)
        total_saved += inserted
        state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)

    state.log_audit("ROUTER", "OpenWrt Releases", "OK", total_saved, "Target profiles.json")
    return total_saved


def _classify_network_device(name: str, prof_id: str, target: str) -> str:
    low = f"{name} {prof_id}".lower()
    if any(k in low for k in ("vr", "td-", "modem", "dsl", "fritzbox 7")) or "lantiq" in target:
        return "MODEM"
    if any(k in low for k in ("unifi u", "eap", "cudy ap", "wax", "cap ", "hap ", "access point")):
        return "ACCESS_POINT"
    if any(k in low for k in ("re2", "re3", "re5", "re6", "deco", "mesh", "extender", "vel op", "orbi", "eero")):
        return "RANGE_EXTENDER"
    return "ROUTER"


def _infer_openwrt_radio_and_soc(packages: str, target: str) -> tuple[str | None, str]:
    low = packages.lower()
    for kw, (wifi, soc) in OPENWRT_WIFI_KEYWORDS.items():
        if kw in low:
            return wifi, soc
    if "lantiq" in target:
        return "Wi-Fi 5 / AC (VDSL2)", "Lantiq xRX200 / VRX268"
    return None, target

"""Clean multi-source product gallery & image health verification crawlers."""

from __future__ import annotations

import html as html_lib
import re
import ssl
import urllib.error
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor, as_completed
from typing import Any

from .http_client import BlockedAntiBotError, PoliteHttpClient
from .state_manager import CrawlStateManager, normalize_canonical_key

GAMINGGEN_CATEGORY_MAP = [
    ("ekran-karti", "GPU"),
    ("islemci", "CPU"),
    ("anakart", "MOTHERBOARD"),
    ("ram-bellek", "RAM"),
    ("ssd", "STORAGE"),
    ("guc-kaynagi-psu", "PSU"),
    ("islemci-sogutucu", "COOLER"),
    ("kasa", "CASE"),
    ("gaming-laptop", "LAPTOP"),
    ("notebook", "LAPTOP"),
    ("gaming-monitor", "MONITOR"),
    ("monitor", "MONITOR"),
    ("klavye", "KEYBOARD"),
    ("mouse", "MOUSE"),
    ("kulaklik", "HEADSET"),
    ("mikrofon", "MICROPHONE"),
    ("mouse-pad", "MOUSEPAD"),
    ("oyuncu-koltugu", "GAMING_CHAIR"),
    ("kasa-fanlari", "CASE_FAN"),
    ("router", "ROUTER"),
    ("menzil-genisletici", "RANGE_EXTENDER"),
    ("masa", "GAMING_DESK"),
    ("ipad", "TABLET"),
    ("tablet", "TABLET"),
    ("akilli-saat", "SMARTWATCH"),
]

GSMARENA_BRAND_INDEXES = [
    ("Apple", "https://www.gsmarena.com/apple-phones-48.php"),
    ("Apple", "https://www.gsmarena.com/apple-phones-f-48-0-p2.php"),
    ("Samsung", "https://www.gsmarena.com/samsung-phones-9.php"),
    ("Samsung", "https://www.gsmarena.com/samsung-phones-f-9-0-p2.php"),
    ("Samsung", "https://www.gsmarena.com/samsung-phones-f-9-0-p3.php"),
    ("Xiaomi", "https://www.gsmarena.com/xiaomi-phones-80.php"),
    ("Xiaomi", "https://www.gsmarena.com/xiaomi-phones-f-80-0-p2.php"),
    ("Xiaomi", "https://www.gsmarena.com/xiaomi-phones-f-80-0-p3.php"),
    ("Google", "https://www.gsmarena.com/google-phones-107.php"),
    ("Google", "https://www.gsmarena.com/google-phones-f-107-0-p2.php"),
    ("OnePlus", "https://www.gsmarena.com/oneplus-phones-95.php"),
    ("OnePlus", "https://www.gsmarena.com/oneplus-phones-f-95-0-p2.php"),
    ("Huawei", "https://www.gsmarena.com/huawei-phones-58.php"),
    ("Huawei", "https://www.gsmarena.com/huawei-phones-f-58-0-p2.php"),
    ("Honor", "https://www.gsmarena.com/honor-phones-121.php"),
    ("Honor", "https://www.gsmarena.com/honor-phones-f-121-0-p2.php"),
    ("Oppo", "https://www.gsmarena.com/oppo-phones-82.php"),
    ("Oppo", "https://www.gsmarena.com/oppo-phones-f-82-0-p2.php"),
    ("Vivo", "https://www.gsmarena.com/vivo-phones-98.php"),
    ("Realme", "https://www.gsmarena.com/realme-phones-118.php"),
    ("Motorola", "https://www.gsmarena.com/motorola-phones-4.php"),
    ("Lenovo", "https://www.gsmarena.com/lenovo-phones-73.php"),
    ("ASUS", "https://www.gsmarena.com/asus-phones-46.php"),
    ("Sony", "https://www.gsmarena.com/sony-phones-7.php"),
]


def _clean_text(raw: str) -> str:
    return re.sub(r"\s+", " ", html_lib.unescape(re.sub(r"<[^>]+>", " ", raw or ""))).strip()


def crawl_gaming_gen_store(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    max_pages_per_cat: int = 3,
    delta_only: bool = False,
) -> int:
    total_inserted = 0
    for slug, cat in GAMINGGEN_CATEGORY_MAP:
        task = f"img:gaminggen:{slug}"
        prog = state.get_progress(task)
        if delta_only and prog and prog["status"] == "DONE" and slug not in (
            "ekran-karti", "islemci", "anakart", "ram-bellek", "ssd",
            "gaming-laptop", "notebook", "monitor", "klavye", "mouse",
        ):
            continue

        cat_rows: list[dict[str, Any]] = []
        pages = 1 if delta_only else max_pages_per_cat
        for page in range(1, pages + 1):
            url = f"https://www.gaming.gen.tr/wp-json/wc/store/v1/products?category={slug}&per_page=100&page={page}"
            try:
                data, status, etag, lm = http.get_json(url)
            except BlockedAntiBotError as exc:
                state.log_audit(cat, "GamingGen Store API", "BLOCKED_ANTIBOT", total_inserted, str(exc))
                return total_inserted
            except Exception as exc:
                state.save_progress(task, "ERROR", note=str(exc))
                break

            if not isinstance(data, list) or not data:
                break

            for p in data:
                name = _clean_text(str(p.get("name") or ""))
                if not name:
                    continue
                # Skip prebuilt PC bundles when pulling components unless category is LAPTOP/MONITOR
                low_name = name.lower()
                if cat in ("GPU", "CPU", "MOTHERBOARD", "RAM", "STORAGE", "PSU") and (
                    "tavsiye sistem" in low_name or (" / " in name and low_name.count("/") >= 2)
                ):
                    continue

                imgs = [
                    im.get("src")
                    for im in (p.get("images") or [])
                    if isinstance(im, dict) and isinstance(im.get("src"), str) and im["src"].startswith("https://")
                ]
                if not imgs:
                    continue

                prices = p.get("prices") or {}
                minor_unit = int(prices.get("currency_minor_unit") or 2)
                raw_price = prices.get("price")
                price_try = round(float(raw_price) / (10 ** minor_unit), 2) if raw_price else None

                brand = name.split()[0]
                brands_list = p.get("brands") or []
                if brands_list and isinstance(brands_list[0], dict) and brands_list[0].get("name"):
                    brand = _clean_text(str(brands_list[0]["name"]))

                cat_rows.append(
                    {
                        "category": cat,
                        "source_name": "GamingGen Store API",
                        "canonical_key": normalize_canonical_key(name),
                        "display_name": name,
                        "brand": brand,
                        "payload": {
                            "price_try": price_try,
                            "permalink": p.get("permalink"),
                            "sku": p.get("sku"),
                        },
                        "extra_specs": {},
                        "images": imgs[:6],
                        "source_url": p.get("permalink") or url,
                    }
                )
            if len(data) < 100:
                break

        inserted = state.upsert_raw_batch(cat_rows)
        total_inserted += inserted
        state.save_progress(task, "DONE", cursor=str(inserted))
    state.log_audit("IMAGE_GALLERY", "GamingGen Store API", "OK", total_inserted, f"{len(GAMINGGEN_CATEGORY_MAP)} categories")
    return total_inserted


def crawl_gsmarena_galleries(
    http: PoliteHttpClient,
    state: CrawlStateManager,
    delta_only: bool = False,
) -> int:
    """Extracts device gallery thumbnails directly from GSMArena makers index pages."""
    total_inserted = 0
    for brand, brand_url in GSMARENA_BRAND_INDEXES:
        page_tag = re.sub(r"[^a-z0-9]", "", brand_url.rsplit("/", 1)[-1])
        task = f"img:gsmarena_gallery:{brand.lower()}:{page_tag}"
        prog = state.get_progress(task)
        try:
            html, status, etag, lm = http.get_text(
                brand_url,
                etag=prog["etag"] if prog else None,
                last_modified=prog["last_modified"] if prog else None,
            )
        except BlockedAntiBotError as exc:
            state.log_audit("SMARTPHONE", "GSMArena Gallery", "BLOCKED_ANTIBOT", total_inserted, str(exc))
            return total_inserted
        except Exception as exc:
            state.save_progress(task, "ERROR", note=str(exc))
            continue

        if status == 304 or not html:
            state.save_progress(task, "DONE", cursor="304", etag=etag, last_modified=lm)
            continue

        makers_m = re.search(r'<div class="makers">(.*?)</div>', html, re.S)
        section = makers_m.group(1) if makers_m else html
        cards = re.findall(
            r'<a[^>]+href=["\']?([a-z0-9_.\-]+\.php)["\']?[^>]*>.*?<img[^>]+src=["\']?(https://fdn2\.gsmarena\.com/[^"\'\s>]+)["\']?[^>]*>.*?<strong><span>(.*?)</span></strong>',
            section,
            re.S | re.I,
        )
        rows: list[dict[str, Any]] = []
        for href, img_url, raw_label in cards:
            label = _clean_text(raw_label)
            if not label or "static" in img_url:
                continue
            full_name = label if label.lower().startswith(brand.lower()) else f"{brand} {label}"
            low = full_name.lower()
            if any(w in low for w in ("watch", "band ", "fit ")):
                cat = "SMARTWATCH"
            elif any(w in low for w in ("ipad", "tab ", "matepad", "pad ")):
                cat = "TABLET"
            else:
                cat = "SMARTPHONE"
            rows.append(
                {
                    "category": cat,
                    "source_name": "GSMArena Gallery",
                    "canonical_key": normalize_canonical_key(full_name),
                    "display_name": full_name,
                    "brand": brand,
                    "payload": {"gsmarena_slug": href},
                    "extra_specs": {},
                    "images": [img_url],
                    "source_url": f"https://www.gsmarena.com/{href}",
                }
            )
        inserted = state.upsert_raw_batch(rows)
        total_inserted += inserted
        state.save_progress(task, "DONE", cursor=str(inserted), etag=etag, last_modified=lm)
        if delta_only and total_inserted >= 250:
            break

    state.log_audit("MOBILE", "GSMArena Gallery", "OK", total_inserted, f"{len(GSMARENA_BRAND_INDEXES)} brands")
    return total_inserted


def search_wikimedia_image(http: PoliteHttpClient, query: str) -> str | None:
    """Queries Wikimedia Commons API for a real photo matching a hardware model."""
    q = urllib.parse.quote_plus(query)
    url = (
        "https://commons.wikimedia.org/w/api.php"
        f"?action=query&generator=search&gsrsearch={q}&gsrnamespace=6&prop=imageinfo&iiprop=url|mime&format=json&gsrlimit=5"
    )
    try:
        data, _, _, _ = http.get_json(url)
    except Exception:
        return None
    pages = (data or {}).get("query", {}).get("pages", {})
    if not isinstance(pages, dict):
        return None

    q_toks = {t for t in re.findall(r"[a-z0-9]{2,}", query.lower()) if t not in {"the", "for", "and", "with"}}
    for page in sorted(pages.values(), key=lambda x: x.get("index", 999)):
        infos = page.get("imageinfo") or []
        if not infos:
            continue
        info = infos[0]
        mime = str(info.get("mime") or "").lower()
        u = str(info.get("url") or "")
        if not u.startswith("https://") or mime not in ("image/jpeg", "image/png", "image/webp"):
            continue
        if re.search(r"\.(webm|mp4|ogv|ogg|mov|avi|svg|pdf|tif|tiff)(?:\?|$)", u, re.I):
            continue
        title_l = str(page.get("title") or "").lower()
        if any(
            bad in title_l
            for bad in (
                "logo", "icon", "diagram", "die_shot", "screenshot", "chart", "map",
                "unboxing", "comparison", "vergleich", "teardown", "_shop", "_store",
                "geekerwan",
            )
        ) or re.search(r"\b\d{2}\.\d{2}\.\d{2}\b", title_l):
            continue
        matched = sum(1 for t in q_toks if t in title_l)
        if q_toks and matched >= len(q_toks):
            return u
    return None


def verify_image_url_pool(
    state: CrawlStateManager,
    urls: list[str],
    max_workers: int = 24,
    recheck_failed: bool = False,
) -> dict[str, bool]:
    """Verifies HTTP 200 image reachability on Android UA with cached persistence in image_health."""
    health = state.load_image_health_map()
    unique_urls = [u for u in dict.fromkeys(urls) if isinstance(u, str) and u.startswith("https://")]
    to_check = [u for u in unique_urls if (u not in health) or (recheck_failed and not health[u])]
    if not to_check:
        return {u: health.get(u, False) for u in unique_urls}

    ssl_ctx = ssl.create_default_context()
    headers = {
        "User-Agent": "Dalvik/2.1.0 (Linux; U; Android 14)",
        "Accept": "image/avif,image/webp,image/png,image/jpeg,image/*,*/*;q=0.8",
    }

    def _probe(u: str) -> tuple[str, bool, int, str]:
        for method in ("HEAD", "GET"):
            try:
                hdrs = dict(headers)
                if method == "GET":
                    hdrs["Range"] = "bytes=0-1023"
                req = urllib.request.Request(u, headers=hdrs, method=method)
                with urllib.request.urlopen(req, timeout=6, context=ssl_ctx) as resp:
                    ct = str(resp.headers.get("Content-Type") or "").lower()
                    ok_ct = (not ct) or ct.startswith(("image/", "application/octet-stream", "binary/"))
                    if resp.status in (200, 206) and ok_ct and "svg" not in ct:
                        return (u, True, resp.status, ct)
                    if method == "HEAD" and resp.status in (403, 405):
                        continue
                    return (u, False, resp.status, ct)
            except urllib.error.HTTPError as he:
                if method == "HEAD" and he.code in (400, 403, 405, 501):
                    continue
                return (u, False, int(he.code), "")
            except Exception:
                return (u, False, 0, "")
        return (u, False, 0, "")

    batch: list[tuple[str, bool, int, str]] = []
    with ThreadPoolExecutor(max_workers=max_workers) as pool:
        futs = [pool.submit(_probe, u) for u in to_check]
        for fut in as_completed(futs):
            res = fut.result()
            batch.append(res)
            health[res[0]] = res[1]
            if len(batch) >= 200:
                state.set_image_health_batch(batch)
                batch.clear()
    if batch:
        state.set_image_health_batch(batch)

    return {u: health.get(u, False) for u in unique_urls}

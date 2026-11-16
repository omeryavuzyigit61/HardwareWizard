"""Polite, rate-limited HTTP client with anti-bot detection and ETag support."""

from __future__ import annotations

import json
import ssl
import time
import urllib.error
import urllib.parse
import urllib.request
from typing import Any, Optional


class BlockedAntiBotError(RuntimeError):
    """Raised immediately when a target presents CAPTCHA / Turnstile / PoW checks."""


class PoliteHttpClient:
    """Rate-limited urllib client honoring per-domain cooldowns and ETag caching."""

    DEFAULT_DELAYS = {
        "raw.githubusercontent.com": 0.25,
        "api.github.com": 0.6,
        "docs.google.com": 1.0,
        "browser.geekbench.com": 1.2,
        "downloads.openwrt.org": 0.4,
        "networkupstools.org": 0.8,
        "sensor.fyi": 1.5,
        "www.techpowerup.com": 3.5,
        "www.cpubenchmark.net": 2.0,
        "www.videocardbenchmark.net": 2.0,
        "www.dxomark.com": 2.5,
        "www.notebookcheck.net": 2.5,
        "www.gsmarena.com": 4.5,
        "www.gaming.gen.tr": 0.6,
        "www.itopya.com": 0.25,
        "itopya.com": 0.25,
        "img.yenieera22.com": 0.15,
        "commons.wikimedia.org": 0.5,
        "motherboarddb.com": 2.0,
        "www.rtings.com": 2.5,
        "www.keychron.com": 1.2,
        "www.pulsar.gg": 1.2,
        "secretlab.co": 1.2,
        "www.lethalgaminggear.com": 1.2,
        "ironscalelabs.com": 1.0,
        "query.wikidata.org": 1.0,
        "www.wikidata.org": 0.8,
        "en.wikipedia.org": 0.5,
    }

    _ANTIBOT_MARKERS = (
        "making sure you're not a bot",
        "making sure you&#39;re not a bot",
        "just a moment...",
        "cf-turnstile",
        "turnstile check",
        "g-recaptcha",
        "hcaptcha",
        "enable javascript and cookies to continue",
        "attention required! | cloudflare",
    )

    def __init__(self, timeout: int = 18) -> None:
        self.timeout = timeout
        self.ssl_ctx = ssl.create_default_context()
        self._last_request_by_host: dict[str, float] = {}
        self.headers = {
            "User-Agent": (
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36"
            ),
            "Accept": "text/html,application/xhtml+xml,application/json,text/csv;q=0.9,*/*;q=0.8",
            "Accept-Language": "en-US,en;q=0.9",
        }

    def _wait_for_host(self, url: str) -> None:
        host = urllib.parse.urlparse(url).netloc.lower()
        min_delay = self.DEFAULT_DELAYS.get(host, 2.0)
        prev = self._last_request_by_host.get(host, 0.0)
        elapsed = time.monotonic() - prev
        if elapsed < min_delay:
            time.sleep(min_delay - elapsed)
        self._last_request_by_host[host] = time.monotonic()

    @classmethod
    def looks_blocked(cls, body: str) -> bool:
        low = body[:25000].lower()
        if any(m in low for m in cls._ANTIBOT_MARKERS):
            return True
        if "<title></title>" in low and "no-cache, no-store, must-revalidate" in low:
            return True
        if "captcha" in low and "<section class=\"details\"" not in low and "techpowerup" in low:
            # TPU AJAX JSON won't contain captcha; detail HTML might.
            if not body.lstrip().startswith("{"):
                return True
        return False

    def get_text(
        self,
        url: str,
        etag: Optional[str] = None,
        last_modified: Optional[str] = None,
        max_retries: int = 2,
    ) -> tuple[Optional[str], int, Optional[str], Optional[str]]:
        """Fetches URL politely. Returns (body_or_none_on_304, status, etag, last_modified)."""
        req_headers = dict(self.headers)
        if etag:
            req_headers["If-None-Match"] = etag
        if last_modified:
            req_headers["If-Modified-Since"] = last_modified

        backoff = 3.0
        for attempt in range(max_retries + 1):
            self._wait_for_host(url)
            req = urllib.request.Request(url, headers=req_headers)
            try:
                with urllib.request.urlopen(req, timeout=self.timeout, context=self.ssl_ctx) as resp:
                    raw = resp.read().decode("utf-8", errors="ignore")
                    if self.looks_blocked(raw):
                        raise BlockedAntiBotError(f"Anti-bot / challenge page detected at {url}")
                    return (
                        raw,
                        resp.status,
                        resp.headers.get("ETag"),
                        resp.headers.get("Last-Modified"),
                    )
            except urllib.error.HTTPError as e:
                if e.code == 304:
                    return None, 304, etag, last_modified
                body = ""
                try:
                    body = e.read(4096).decode("utf-8", errors="ignore")
                except Exception:
                    pass
                if e.code == 403 or self.looks_blocked(body):
                    raise BlockedAntiBotError(f"HTTP {e.code} anti-bot / forbidden at {url}") from e
                if e.code in (429, 502, 503, 504) and attempt < max_retries:
                    time.sleep(backoff)
                    backoff *= 2.5
                    continue
                raise
        raise RuntimeError(f"Exhausted retries for {url}")

    def get_json(
        self,
        url: str,
        etag: Optional[str] = None,
        last_modified: Optional[str] = None,
    ) -> tuple[Optional[Any], int, Optional[str], Optional[str]]:
        text, status, new_etag, new_lm = self.get_text(url, etag=etag, last_modified=last_modified)
        if text is None:
            return None, status, new_etag, new_lm
        return json.loads(text), status, new_etag, new_lm

    def check_image_url(self, url: str) -> tuple[bool, int, str]:
        """Lightweight HEAD (with GET fallback on 405) to confirm live image media."""
        if not url or not url.startswith("http"):
            return False, 0, ""
        req = urllib.request.Request(url, headers=self.headers, method="HEAD")
        try:
            with urllib.request.urlopen(req, timeout=8, context=self.ssl_ctx) as resp:
                ctype = (resp.headers.get("Content-Type") or "").lower()
                return ("image" in ctype or "octet-stream" in ctype), resp.status, ctype
        except urllib.error.HTTPError as e:
            if e.code in (403, 405):
                # Some CDNs block HEAD; test 1KB range GET
                try:
                    h = dict(self.headers)
                    h["Range"] = "bytes=0-1023"
                    with urllib.request.urlopen(
                        urllib.request.Request(url, headers=h), timeout=8, context=self.ssl_ctx
                    ) as r2:
                        ctype = (r2.headers.get("Content-Type") or "").lower()
                        return ("image" in ctype or resp_is_image_magic(r2.read(16))), r2.status, ctype
                except Exception:
                    return False, e.code, ""
            return False, e.code, ""
        except Exception:
            return False, 0, ""


def resp_is_image_magic(chunk: bytes) -> bool:
    return (
        chunk.startswith(b"\xff\xd8\xff")
        or chunk.startswith(b"\x89PNG")
        or chunk.startswith(b"RIFF")
        or chunk.startswith(b"GIF8")
    )

#!/usr/bin/env python3
"""Unified CLI entrypoint for checkpointed full & delta hardware crawls."""

from __future__ import annotations

import argparse
import os
import sys
import time

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
if ROOT_DIR not in sys.path:
    sys.path.insert(0, ROOT_DIR)

from scripts.crawlers.http_client import PoliteHttpClient
from scripts.crawlers.sources_images import crawl_gaming_gen_store, crawl_gsmarena_galleries
from scripts.crawlers.sources_mobile_laptop import crawl_mobile_and_laptop
from scripts.crawlers.sources_network_ups import crawl_networking_and_ups
from scripts.crawlers.sources_pc import crawl_pc_components
from scripts.crawlers.sources_peripherals import crawl_peripherals
from scripts.crawlers.state_manager import CrawlStateManager


def main() -> int:
    parser = argparse.ArgumentParser(description="HardwareWizard Multi-Source Checkpointed Crawler")
    parser.add_argument("--mode", choices=("full", "delta"), default="delta", help="Full checkpointed crawl or delta refresh")
    parser.add_argument("--groups", default="pc,peripherals,mobile,network,images", help="Comma-separated groups to run")
    parser.add_argument("--gsmarena-limit", type=int, default=15, help="Max GSMArena device pages per run (10s delay each)")
    parser.add_argument("--shopify-pages", type=int, default=3, help="Max Shopify pages per brand")
    parser.add_argument("--nut-vendors", type=int, default=6, help="Max NUT UPS DDL vendors")
    args = parser.parse_args()

    delta_only = args.mode == "delta"
    groups = {g.strip().lower() for g in args.groups.split(",") if g.strip()}
    t0 = time.monotonic()

    http = PoliteHttpClient()
    state = CrawlStateManager()
    print(f"=== HardwareWizard Crawler Start (mode={args.mode}, groups={sorted(groups)}) ===")

    if "pc" in groups:
        res = crawl_pc_components(http, state, delta_only=delta_only)
        print(f"[PC] {res}")
    if "peripherals" in groups:
        res = crawl_peripherals(http, state, max_shopify_pages=args.shopify_pages, delta_only=delta_only)
        print(f"[PERIPHERALS] {res}")
    if "network" in groups:
        res = crawl_networking_and_ups(http, state, max_nut_vendors=args.nut_vendors, delta_only=delta_only)
        print(f"[NETWORK & UPS] {res}")
    if "mobile" in groups:
        res = crawl_mobile_and_laptop(http, state, max_gsmarena_devices=args.gsmarena_limit, delta_only=delta_only)
        print(f"[MOBILE & LAPTOP] {res}")
    if "images" in groups:
        gg_cnt = crawl_gaming_gen_store(http, state, max_pages_per_cat=3, delta_only=delta_only)
        gsm_cnt = crawl_gsmarena_galleries(http, state, delta_only=delta_only)
        print(f"[IMAGES] GamingGen={gg_cnt}, GSMArenaGallery={gsm_cnt}")

    print("\n=== Crawl State Summary (category | source | count) ===")
    for cat, src, cnt in state.summary_counts():
        print(f"  {cat:16s} | {src:22s} | {cnt:6d}")

    elapsed = time.monotonic() - t0
    print(f"\nCompleted in {elapsed:.1f}s -> {state.db_path}")
    state.close()
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

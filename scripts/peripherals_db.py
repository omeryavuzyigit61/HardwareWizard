#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/peripherals_db.py
Curated Global Peripherals Database (2022-2026)
Standards: DisplaySpecifications, EloShapes, and Rtings
Price logic: priceTry / current_price_try has real TR market value if sold in TR, else None.
"""

from massive_catalog_data import (
    get_all_monitors,
    get_all_mice,
    get_all_keyboards,
    get_all_headsets
)

MONITORS_DATA = get_all_monitors()
MICE_DATA = get_all_mice()
KEYBOARDS_DATA = get_all_keyboards()
HEADSETS_DATA = get_all_headsets()

if __name__ == "__main__":
    print("=" * 50)
    print("TOTAL MASTER PERIPHERALS IN DATABASE:")
    print(f"Monitors:  {len(MONITORS_DATA)}")
    print(f"Mice:      {len(MICE_DATA)}")
    print(f"Keyboards: {len(KEYBOARDS_DATA)}")
    print(f"Headsets:  {len(HEADSETS_DATA)}")
    print(f"GRAND TOTAL: {len(MONITORS_DATA) + len(MICE_DATA) + len(KEYBOARDS_DATA) + len(HEADSETS_DATA)} PRODUCTS")
    print("=" * 50)

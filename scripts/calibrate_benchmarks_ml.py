#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/calibrate_benchmarks_ml.py

Hardware Wizard — Unified ML-Calibrated Benchmark & Physical Frame-Time Pipeline (v6.0)

1. Anchors desktop GPUs (TechPowerUp 4K/Raster Relative Index, RTX 4090 = 100, uncapped!)
   and desktop CPUs (1080p Gaming Index, Ryzen 7 9800X3D = 100 + Productivity Index, Ryzen 9 9950X = 100).
2. Trains pure-Python Log-Ridge Regression models on verified anchors & reports Leave-One-Out CV MAPE,
   exporting exact coefficients for Kotlin UnifiedFpsSimulator.kt fallback on unseen hardware.
3. Evaluates the Unified Physical Frame-Time Pipeline (L_p norm, p = 3.2) across all 12 games x 3 resolutions:
   - GPU items simulated with fixed Reference CPU: Ryzen 7 9800X3D (gaming=100, single-CCD 96 MB 3D V-Cache)
   - CPU items simulated with fixed Reference GPU: RTX 5090 32GB (gpuIndex=133, 32 GB VRAM)
   => Guarantees bit-for-bit identical FPS for (9800X3D + RTX 5090) across CPU Detail, GPU Detail, PC Wizard & VS Arena!
4. Fixes corrupted DB records (9950X3D / 9900X3D L3=128 MB, RTX 5080 vs 4090 inversion, 12400 vs 12400F split,
   RTX 4060 40-point spread, 61-GPU score=100 ceiling compression).
5. Optionally applies directly to bundled Room v36 DB (--apply-room) and emits compact Postgres SQL migration
   for Supabase (--emit-sql supabase_benchmark_v6_calibration.sql, catalog_revision = 7).
"""

from __future__ import annotations

import argparse
import json
import math
import os
import re
import sqlite3
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Any

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")

ROOT_DIR = Path(__file__).resolve().parent.parent
ROOM_DB_PATH = ROOT_DIR / "androidApp" / "src" / "main" / "assets" / "databases" / "hardware_wizard.db"
SQL_OUT_PATH = ROOT_DIR / "supabase_benchmark_v6_calibration.sql"

TARGET_CATALOG_REVISION = 7
FRAME_TIME_P = 3.2  # DX12/Vulkan pipelined CPU-GPU norm exponent


# ==============================================================================
# 1. VERIFIED GPU ANCHOR TABLE (TechPowerUp 2025/2026 Relative Performance)
#    Baseline: RTX 4090 = 100.0 (UNCAPPED! RTX 5090 = 133.0)
#    Fields: (canonical_key, tpu_rel_4k, time_spy_graphics, steel_nomad, ref_boost_mhz, vram_gb, bus_bits, tdp_w, arch_tier)
# ==============================================================================
GPU_ANCHORS: list[tuple[str, float, int, int, int, int, int, int, float]] = [
    # NVIDIA Blackwell (RTX 50)
    ("rtx 5090",         133.0, 48500, 14400, 2407, 32, 512, 575, 1.32),
    ("rtx 5090 d v2",    122.0, 44500, 13200, 2407, 24, 384, 575, 1.30),
    ("rtx 5090 d",       131.0, 47800, 14100, 2407, 32, 512, 575, 1.32),
    ("rtx 5080",          86.0, 32600,  8900, 2617, 16, 256, 360, 1.30),
    ("rtx 5070 ti",       75.0, 27800,  7150, 2452, 16, 256, 300, 1.28),
    ("rtx 5070",          60.0, 22400,  5600, 2512, 12, 192, 250, 1.26),
    ("rtx 5060 ti 16",    48.0, 16900,  4150, 2572, 16, 128, 180, 1.25),
    ("rtx 5060 ti",       47.0, 16600,  4050, 2572,  8, 128, 180, 1.25),
    ("rtx 5060",          39.0, 13800,  3300, 2497,  8, 128, 145, 1.24),
    ("rtx 5050",          28.0,  9800,  2300, 2572,  8, 128, 130, 1.20),

    # NVIDIA Ada Lovelace (RTX 40 + Workstation Ada)
    ("rtx 6000 ada",      98.0, 35500,  9400, 2505, 48, 384, 300, 1.22),
    ("rtx 5000 ada",      74.0, 27200,  6900, 2130, 32, 256, 250, 1.20),
    ("rtx 4000 ada",      46.0, 16400,  4000, 2175, 20, 160, 130, 1.20),
    ("rtx 4000 sff ada",  36.0, 12800,  3100, 1565, 20, 160,  70, 1.18),
    ("rtx 4090 d",        92.0, 33800,  8900, 2520, 24, 384, 425, 1.24),
    ("rtx 4090",         100.0, 36800,  9800, 2520, 24, 384, 450, 1.25),
    ("rtx 4080 super",    79.0, 28800,  7450, 2550, 16, 256, 320, 1.25),
    ("rtx 4080",          77.0, 28100,  7250, 2505, 16, 256, 320, 1.25),
    ("rtx 4070 ti super", 69.0, 24800,  6250, 2610, 16, 256, 285, 1.24),
    ("rtx 4070 ti",       63.0, 22800,  5650, 2610, 12, 192, 285, 1.24),
    ("rtx 4070 super",    60.0, 21200,  5250, 2475, 12, 192, 220, 1.24),
    ("rtx 4070 gddr6",    51.0, 17800,  4350, 2475, 12, 192, 200, 1.23),
    ("rtx 4070",          52.0, 18200,  4450, 2475, 12, 192, 200, 1.24),
    ("rtx 4060 ti 16",    42.0, 13800,  3300, 2535, 16, 128, 165, 1.22),
    ("rtx 4060 ti",       41.0, 13600,  3250, 2535,  8, 128, 160, 1.22),
    ("rtx 4060",          34.0, 10800,  2550, 2460,  8, 128, 115, 1.22),
    ("rtx 4010",          11.0,  3400,   800, 2000,  4,  64,  50, 1.10),

    # NVIDIA Ampere (RTX 30)
    ("rtx 3090 ti",       67.0, 22400,  5500, 1860, 24, 384, 450, 1.00),
    ("rtx 3090",          61.0, 20300,  4950, 1695, 24, 384, 350, 1.00),
    ("rtx 3080 ti",       60.0, 19800,  4800, 1665, 12, 384, 350, 1.00),
    ("rtx 3080",          53.0, 17800,  4300, 1710, 10, 320, 320, 1.00),
    ("rtx 3070 ti",       46.0, 15100,  3600, 1770,  8, 256, 290, 1.00),
    ("rtx 3070",          43.0, 13800,  3300, 1725,  8, 256, 220, 1.00),
    ("rtx 3060 ti",       38.0, 12100,  2850, 1665,  8, 256, 200, 1.00),
    ("rtx 3060",          29.0,  8900,  2050, 1777, 12, 192, 170, 0.98),
    ("rtx 3050",          20.0,  6200,  1400, 1777,  8, 128, 130, 0.96),

    # AMD RDNA 4 (RX 9000)
    ("rx 9070 xt",        75.0, 28600,  7250, 2970, 16, 256, 304, 1.24),
    ("rx 9070",           66.0, 24800,  6200, 2520, 16, 256, 220, 1.24),
    ("rx 9070 gre",       58.0, 21400,  5350, 2750, 12, 192, 220, 1.22),
    ("rx 9060 xt 16",     47.0, 16800,  4150, 3130, 16, 128, 160, 1.22),
    ("rx 9060 xt",        45.0, 16200,  4000, 3130,  8, 128, 160, 1.22),
    ("rx 9060",           37.0, 13100,  3150, 2900,  8, 128, 150, 1.20),

    # AMD RDNA 3 (RX 7000)
    ("rx 7900 xtx",       80.0, 29800,  7350, 2500, 24, 384, 355, 1.12),
    ("rx 7900 xt",        68.0, 25600,  6150, 2400, 20, 320, 315, 1.12),
    ("rx 7900 gre",       59.0, 22400,  5300, 2245, 16, 256, 260, 1.11),
    ("rx 7800 xt",        54.0, 19800,  4650, 2430, 16, 256, 263, 1.10),
    ("rx 7700 xt",        46.0, 17100,  3950, 2544, 12, 192, 245, 1.10),
    ("rx 7700",           42.0, 15400,  3550, 2400, 16, 256, 220, 1.09),
    ("rx 7600 xt",        35.0, 11800,  2650, 2755, 16, 128, 190, 1.08),
    ("rx 7600",           33.0, 10900,  2450, 2655,  8, 128, 165, 1.08),

    # AMD RDNA 2 (RX 6000)
    ("rx 6950 xt",        61.0, 21800,  5150, 2310, 16, 256, 335, 1.02),
    ("rx 6900 xt",        57.0, 20400,  4750, 2250, 16, 256, 300, 1.02),
    ("rx 6800 xt",        54.0, 19200,  4450, 2250, 16, 256, 300, 1.02),
    ("rx 6800",           47.0, 16300,  3800, 2105, 16, 256, 250, 1.02),
    ("rx 6750 xt",        42.0, 13800,  3200, 2600, 12, 192, 250, 1.01),
    ("rx 6700 xt",        39.0, 12800,  2950, 2581, 12, 192, 230, 1.01),
    ("rx 6650 xt",        33.0, 10400,  2350, 2635,  8, 128, 180, 1.00),
    ("rx 6600 xt",        31.0,  9800,  2200, 2589,  8, 128, 160, 1.00),
    ("rx 6600",           26.0,  8200,  1850, 2491,  8, 128, 132, 1.00),

    # Intel Arc Battlemage & Alchemist
    ("arc b580",          38.0, 14600,  3400, 2670, 12, 192, 190, 1.12),
    ("arc b570",          33.0, 12600,  2900, 2500, 10, 160, 150, 1.10),
    ("arc a770",          32.0, 13500,  2900, 2400, 16, 256, 225, 0.94),
    ("arc a750",          29.0, 12300,  2650, 2400,  8, 256, 225, 0.93),
]


# ==============================================================================
# 2. VERIFIED CPU ANCHOR TABLE (TechPowerUp 1080p Gaming + Cinebench R23 Multi)
#    Gaming Baseline: Ryzen 7 9800X3D = 100.0
#    Productivity Baseline: Ryzen 9 9950X = 100.0 (CB23 Multi = 43,200)
#    Fields: (key, gaming_idx, prod_idx, cb23_single, cb23_multi, passmark, l3_mb_fix, ccd_count, is_x3d)
# ==============================================================================
CPU_ANCHORS: list[tuple[str, float, float, int, int, int, int | None, int, bool]] = [
    # AMD Ryzen 9000 (Zen 5 - Granite Ridge)
    ("9950x3d2", 102.0, 101.0, 2320, 43600, 68000, 192, 2, True),
    ("9950x3d",   99.0, 100.0, 2310, 43100, 66800, 128, 2, True),
    ("9800x3d",  100.0,  56.0, 2240, 24200, 44200,  96, 1, True),
    ("9900x3d",   93.0,  78.0, 2260, 33800, 54500, 128, 2, True),
    ("9950x",     80.0, 100.0, 2340, 43200, 66500,  64, 2, False),
    ("9900x",     78.0,  79.0, 2280, 34200, 54800,  64, 2, False),
    ("9700x",     81.0,  51.0, 2210, 21800, 39500,  32, 1, False),
    ("9600x",     79.0,  40.0, 2180, 17200, 33200,  32, 1, False),
    ("9600",      77.0,  38.0, 2120, 16400, 31600,  32, 1, False),

    # AMD Ryzen 7000 (Zen 4 - Raphael)
    ("7800x3d",   92.0,  44.0, 1960, 19200, 36800,  96, 1, True),
    ("7950x3d",   90.0,  88.0, 2080, 38200, 61200, 128, 2, True),
    ("7900x3d",   84.0,  69.0, 2020, 29800, 50200, 128, 2, True),
    ("7600x3d",   85.0,  34.0, 1890, 14800, 29400,  96, 1, True),
    ("7950x",     77.0,  92.0, 2110, 39600, 63400,  64, 2, False),
    ("7900x",     75.0,  73.0, 2060, 31500, 52900,  64, 2, False),
    ("7900",      73.0,  66.0, 1990, 28500, 48800,  64, 2, False),
    ("7700x",     77.0,  46.0, 2020, 19900, 36200,  32, 1, False),
    ("7700",      75.0,  44.0, 1960, 19000, 34900,  32, 1, False),
    ("7600x",     76.0,  36.0, 1980, 15400, 28600,  32, 1, False),
    ("7600",      74.0,  34.0, 1920, 14700, 27400,  32, 1, False),
    ("7500f",     73.0,  33.0, 1890, 14300, 26800,  32, 1, False),

    # AMD Ryzen 8000G (Zen 4 APU - 16 MB L3)
    ("8700g",     68.0,  41.0, 1820, 17800, 31800,  16, 1, False),
    ("8700f",     67.0,  40.0, 1800, 17400, 31200,  16, 1, False),
    ("8600g",     64.0,  31.0, 1780, 13400, 25200,  16, 1, False),
    ("8500g",     58.0,  26.0, 1700, 11200, 21500,  16, 1, False),
    ("8400f",     61.0,  29.0, 1740, 12600, 23800,  16, 1, False),

    # AMD Ryzen 5000 (Zen 3 - Vermeer / Cezanne)
    ("5800x3d",   75.0,  35.0, 1520, 15100, 28400,  96, 1, True),
    ("5700x3d",   72.0,  32.0, 1420, 13800, 26400,  96, 1, True),
    ("5600x3d",   70.0,  25.0, 1460, 10800, 22400,  96, 1, True),
    ("5950x",     64.0,  66.0, 1640, 28600, 45800,  64, 2, False),
    ("5900xt",    64.0,  65.0, 1620, 28000, 45000,  64, 2, False),
    ("5900x",     63.0,  52.0, 1620, 22400, 39500,  64, 2, False),
    ("5800xt",    64.0,  36.0, 1630, 15600, 28800,  32, 1, False),
    ("5800x",     64.0,  36.0, 1610, 15400, 28400,  32, 1, False),
    ("5700x",     62.0,  34.0, 1560, 14600, 26800,  32, 1, False),
    ("5700g",     55.0,  31.0, 1490, 13400, 24600,  16, 1, False),
    ("5700",      54.0,  30.0, 1470, 13000, 24000,  16, 1, False),
    ("5600xt",    63.0,  27.0, 1580, 11800, 22500,  32, 1, False),
    ("5600x",     62.0,  27.0, 1560, 11600, 22000,  32, 1, False),
    ("5600",      61.0,  26.0, 1510, 11100, 21500,  32, 1, False),
    ("5600g",     52.0,  24.0, 1460, 10400, 19800,  16, 1, False),
    ("5500",      51.0,  23.0, 1440, 10100, 19500,  16, 1, False),

    # AMD Ryzen 3000 / 2000 / 1000
    ("3950x",     51.0,  57.0, 1320, 24600, 39200,  64, 2, False),
    ("3900x",     50.0,  43.0, 1310, 18600, 32800,  64, 2, False),
    ("3800x",     50.0,  30.0, 1300, 12800, 23200,  32, 1, False),
    ("3700x",     49.0,  29.0, 1270, 12400, 22600,  32, 1, False),
    ("3600x",     48.0,  22.0, 1270,  9600, 18200,  32, 1, False),
    ("3600",      47.0,  21.0, 1240,  9200, 17800,  32, 1, False),

    # Intel Core Ultra 200S (Arrow Lake-S)
    ("285k",      78.0, 105.0, 2320, 45600, 67230,  36, 1, False),
    ("285",       74.0,  89.0, 2220, 38500, 58000,  36, 1, False),
    ("265k",      76.0,  87.0, 2260, 37800, 58569,  30, 1, False),
    ("265kf",     76.0,  87.0, 2260, 37800, 58569,  30, 1, False),
    ("265",       73.0,  76.0, 2180, 33000, 52000,  30, 1, False),
    ("245k",      72.0,  60.0, 2180, 26000, 42500,  24, 1, False),
    ("245kf",     72.0,  60.0, 2180, 26000, 42500,  24, 1, False),
    ("245",       69.0,  53.0, 2100, 23000, 38000,  24, 1, False),

    # Intel 14th Gen (Raptor Lake Refresh)
    ("14900ks",   87.0,  97.0, 2350, 42000, 61500,  36, 1, False),
    ("14900k",    86.0,  94.0, 2300, 40800, 59500,  36, 1, False),
    ("14900kf",   86.0,  94.0, 2300, 40800, 59500,  36, 1, False),
    ("14900f",    81.0,  80.0, 2220, 34500, 49500,  36, 1, False),
    ("14900",     81.0,  80.0, 2220, 34500, 49500,  36, 1, False),
    ("14700k",    83.0,  81.0, 2160, 34800, 53200,  33, 1, False),
    ("14700kf",   83.0,  81.0, 2160, 34800, 53200,  33, 1, False),
    ("14700f",    78.0,  69.0, 2100, 29800, 44500,  33, 1, False),
    ("14700",     78.0,  69.0, 2100, 29800, 44500,  33, 1, False),
    ("14600k",    79.0,  57.0, 2060, 24600, 39200,  24, 1, False),
    ("14600kf",   79.0,  57.0, 2060, 24600, 39200,  24, 1, False),
    ("14600",     76.0,  53.0, 2010, 22800, 36000,  24, 1, False),
    ("14500",     72.0,  47.0, 1930, 20400, 32200,  24, 1, False),
    ("14400f",    66.0,  36.0, 1810, 15600, 26000,  20, 1, False),
    ("14400",     66.0,  36.0, 1810, 15600, 26000,  20, 1, False),
    ("14100f",    58.0,  21.0, 1780,  9100, 15800,  12, 1, False),
    ("14100",     58.0,  21.0, 1780,  9100, 15800,  12, 1, False),

    # Intel 13th Gen (Raptor Lake)
    ("13900ks",   85.0,  94.0, 2320, 40500, 60369,  36, 1, False),
    ("13900k",    84.0,  91.0, 2260, 39200, 58800,  36, 1, False),
    ("13900kf",   84.0,  91.0, 2260, 39200, 58800,  36, 1, False),
    ("13900f",    79.0,  78.0, 2180, 33600, 48081,  36, 1, False),
    ("13900",     79.0,  78.0, 2180, 33600, 48081,  36, 1, False),
    ("13700k",    80.0,  71.0, 2120, 30600, 46800,  30, 1, False),
    ("13700kf",   80.0,  71.0, 2120, 30600, 46800,  30, 1, False),
    ("13700f",    75.0,  61.0, 2040, 26400, 38500,  30, 1, False),
    ("13700",     75.0,  61.0, 2040, 26400, 38500,  30, 1, False),
    ("13600k",    77.0,  56.0, 2010, 24200, 38000,  24, 1, False),
    ("13600kf",   77.0,  56.0, 2010, 24200, 38000,  24, 1, False),
    ("13600",     73.0,  50.0, 1950, 21500, 33000,  24, 1, False),
    ("13500",     70.0,  46.0, 1890, 19800, 31500,  24, 1, False),
    ("13400f",    65.0,  36.0, 1790, 15500, 25500,  20, 1, False),
    ("13400",     65.0,  36.0, 1790, 15500, 25500,  20, 1, False),
    ("13100f",    56.0,  20.0, 1740,  8700, 14900,  12, 1, False),
    ("13100",     56.0,  20.0, 1740,  8700, 14900,  12, 1, False),

    # Intel 12th Gen (Alder Lake)
    ("12900ks",   75.0,  66.0, 2060, 28600, 43378,  30, 1, False),
    ("12900k",    73.0,  63.0, 2010, 27400, 41200,  30, 1, False),
    ("12900kf",   73.0,  63.0, 2010, 27400, 41200,  30, 1, False),
    ("12900f",    69.0,  55.0, 1950, 23800, 35710,  30, 1, False),
    ("12900",     69.0,  55.0, 1950, 23800, 35710,  30, 1, False),
    ("12700k",    70.0,  53.0, 1930, 22800, 34600,  25, 1, False),
    ("12700kf",   70.0,  53.0, 1930, 22800, 34600,  25, 1, False),
    ("12700f",    67.0,  47.0, 1870, 20400, 30500,  25, 1, False),
    ("12700",     67.0,  47.0, 1870, 20400, 30500,  25, 1, False),
    ("12600k",    67.0,  41.0, 1890, 17600, 27600,  20, 1, False),
    ("12600kf",   67.0,  41.0, 1890, 17600, 27600,  20, 1, False),
    ("12600",     63.0,  30.0, 1840, 13000, 21200,  18, 1, False),
    ("12400f",    61.0,  29.0, 1720, 12400, 19500,  18, 1, False),
    ("12400",     61.0,  29.0, 1720, 12400, 19500,  18, 1, False),
    ("12100f",    53.0,  19.0, 1680,  8400, 13800,  12, 1, False),
    ("12100",     53.0,  19.0, 1680,  8400, 13800,  12, 1, False),

    # Intel 11th / 10th Gen (Rocket / Comet Lake)
    ("11900k",    56.0,  37.0, 1660, 16000, 25400,  16, 1, False),
    ("11700k",    55.0,  35.0, 1600, 15000, 24500,  16, 1, False),
    ("11600k",    53.0,  26.0, 1580, 11300, 19800,  12, 1, False),
    ("11400f",    50.0,  24.0, 1460, 10200, 17200,  12, 1, False),
    ("11400",     50.0,  24.0, 1460, 10200, 17200,  12, 1, False),
    ("10900k",    54.0,  37.0, 1420, 16100, 23600,  20, 1, False),
    ("10700k",    52.0,  30.0, 1380, 12900, 19500,  16, 1, False),
    ("10600k",    49.0,  22.0, 1340,  9600, 14600,  12, 1, False),
    ("10400f",    46.0,  19.0, 1220,  8200, 12400,  12, 1, False),
    ("10400",     46.0,  19.0, 1220,  8200, 12400,  12, 1, False),
]


# ==============================================================================
# 3. 12-GAME PHYSICAL PROFILES (Calibrated to RTX 4090 = 100 & Ryzen 7 9800X3D = 100)
#    gpu1080/1440/4k = isolated GPU ceiling for RTX 4090 (idx=100)
#    cpuBase = isolated CPU ceiling for Ryzen 7 9800X3D (gamingIdx=100)
#    gpuExp = GPU scaling exponent, x3dAffinity = extra 3D V-Cache sensitivity
#    vram1080/1440/4k = native Ultra/High VRAM footprint (GB)
#    baseLow1 = base 1% Low / Avg ratio for neutral CPU with adequate VRAM
# ==============================================================================
@dataclass(frozen=True)
class GameProfile:
    key: str
    is_esports: bool
    gpu_1080: float
    gpu_1440: float
    gpu_4k: float
    cpu_base: float
    gpu_exp: float
    x3d_affinity: float
    vram_1080: float
    vram_1440: float
    vram_4k: float
    base_low1: float


GAME_PROFILES: list[GameProfile] = [
    # 5 E-Sports Titles (Competitive settings — matches TechPowerUp/HUB lab benches)
    GameProfile("counter_strike_2",      True, 1150.0, 760.0, 390.0, 920.0, 0.92, 0.14, 4.5,  5.5,  7.0, 0.72),
    GameProfile("valorant",              True, 1180.0, 860.0, 480.0, 960.0, 0.86, 0.16, 3.0,  3.8,  5.0, 0.74),
    GameProfile("apex_legends",          True,  460.0, 340.0, 200.0, 390.0, 0.94, 0.10, 5.5,  6.8,  8.5, 0.76),
    GameProfile("warzone",               True,  330.0, 245.0, 145.0, 295.0, 0.95, 0.14, 7.5,  9.5, 12.0, 0.73),
    GameProfile("pubg",                  True,  410.0, 305.0, 175.0, 350.0, 0.94, 0.13, 6.0,  7.5,  9.5, 0.72),
    # 7 AAA Heavy Titles (Native Ultra / High Raster — verified against TPU/HUB)
    GameProfile("cyberpunk_2077",        False, 245.0, 158.0,  82.0, 215.0, 1.00, 0.12, 7.2,  8.6, 12.2, 0.77),
    GameProfile("alan_wake_2",           False, 172.0, 114.0,  58.0, 195.0, 1.02, 0.05, 8.5, 10.8, 14.0, 0.78),
    GameProfile("red_dead_redemption_2", False, 240.0, 175.0, 105.0, 235.0, 0.98, 0.08, 6.5,  8.0, 10.5, 0.79),
    GameProfile("black_myth_wukong",     False, 185.0, 124.0,  64.0, 205.0, 1.01, 0.06, 8.0, 10.2, 13.5, 0.75),
    GameProfile("forza_horizon_5",       False, 310.0, 235.0, 146.0, 285.0, 0.97, 0.09, 7.5,  9.2, 11.8, 0.81),
    GameProfile("hogwarts_legacy",       False, 225.0, 154.0,  84.0, 198.0, 0.99, 0.13, 8.8, 11.0, 14.5, 0.74),
    GameProfile("baldurs_gate_3",        False, 265.0, 196.0, 118.0, 188.0, 0.96, 0.16, 7.0,  8.5, 11.0, 0.74),
]

REF_CPU_GAMING = 100.0   # Ryzen 7 9800X3D
REF_CPU_X3D = True
REF_CPU_CCD = 1
REF_CPU_CORES = 8
REF_GPU_INDEX = 133.0    # GeForce RTX 5090 32GB
REF_GPU_VRAM = 32.0


# ==============================================================================
# 4. PURE-PYTHON RIDGE REGRESSION (Gaussian Elimination, Zero External Deps!)
# ==============================================================================
def solve_linear_system(a: list[list[float]], b: list[float]) -> list[float]:
    n = len(b)
    mat = [row[:] + [b[i]] for i, row in enumerate(a)]
    for col in range(n):
        piv = max(range(col, n), key=lambda r: abs(mat[r][col]))
        mat[col], mat[piv] = mat[piv], mat[col]
        denom = mat[col][col] if abs(mat[col][col]) > 1e-12 else 1e-12
        for j in range(col, n + 1):
            mat[col][j] /= denom
        for r in range(n):
            if r != col:
                f = mat[r][col]
                for j in range(col, n + 1):
                    mat[r][j] -= f * mat[col][j]
    return [mat[i][n] for i in range(n)]


def fit_ridge(xs: list[list[float]], ys: list[float], lam: float = 0.5) -> list[float]:
    d = len(xs[0])
    xtx = [[0.0] * d for _ in range(d)]
    xty = [0.0] * d
    for x, y in zip(xs, ys):
        for i in range(d):
            xty[i] += x[i] * y
            for j in range(d):
                xtx[i][j] += x[i] * x[j]
    for i in range(1, d):  # do not regularize intercept (index 0)
        xtx[i][i] += lam
    return solve_linear_system(xtx, xty)


def predict_linear(w: list[float], x: list[float]) -> float:
    return sum(wi * xi for wi, xi in zip(w, x))


def train_gpu_ml_model() -> tuple[list[float], float]:
    """Train Log-Ridge model on GPU anchors: features = [1, log(vram), log(bus), log(tdp), log(arch_tier)]."""
    xs, ys = [], []
    for _, rel, _, _, _, vram, bus, tdp, arch_tier in GPU_ANCHORS:
        xs.append([
            1.0,
            math.log(max(vram, 2.0)),
            math.log(max(bus, 64.0)),
            math.log(max(tdp, 40.0)),
            math.log(max(arch_tier, 0.7)),
        ])
        ys.append(math.log(rel))

    # Leave-One-Out CV
    errs = []
    for i in range(len(xs)):
        x_tr = xs[:i] + xs[i + 1:]
        y_tr = ys[:i] + ys[i + 1:]
        w_cv = fit_ridge(x_tr, y_tr, lam=0.35)
        pred = math.exp(predict_linear(w_cv, xs[i]))
        true_v = math.exp(ys[i])
        errs.append(abs(pred - true_v) / true_v * 100.0)
    w_full = fit_ridge(xs, ys, lam=0.35)
    return w_full, sum(errs) / len(errs)


def train_cpu_ml_models() -> tuple[list[float], float, list[float], float]:
    """Train Log-Ridge models for CPU gamingIndex & productivityIndex from synthetic/spec features."""
    xs_g, ys_g, xs_p, ys_p = [], [], [], []
    for key, g_idx, p_idx, cb_s, cb_m, pm, l3_mb, ccd, is_x3d in CPU_ANCHORS:
        eff_l3 = (l3_mb or 32) / float(max(ccd, 1))
        dual_non_x3d = 1.0 if (ccd >= 2 and not is_x3d) else 0.0
        arrow_lake = 1.0 if key in ("285k", "285", "265k", "265kf", "265", "245k", "245kf", "245") else 0.0
        xs_g.append([
            1.0,
            math.log(max(cb_s, 800)),
            math.log(max(eff_l3, 8.0)),
            1.0 if is_x3d else 0.0,
            dual_non_x3d,
            arrow_lake,
        ])
        ys_g.append(math.log(g_idx))
        xs_p.append([
            1.0,
            math.log(max(cb_m, 4000)),
            math.log(max(pm, 6000)),
        ])
        ys_p.append(math.log(p_idx))

    errs_g, errs_p = [], []
    for i in range(len(xs_g)):
        wg = fit_ridge(xs_g[:i] + xs_g[i + 1:], ys_g[:i] + ys_g[i + 1:], lam=0.25)
        pred_g = math.exp(predict_linear(wg, xs_g[i]))
        errs_g.append(abs(pred_g - math.exp(ys_g[i])) / math.exp(ys_g[i]) * 100.0)

        wp = fit_ridge(xs_p[:i] + xs_p[i + 1:], ys_p[:i] + ys_p[i + 1:], lam=0.25)
        pred_p = math.exp(predict_linear(wp, xs_p[i]))
        errs_p.append(abs(pred_p - math.exp(ys_p[i])) / math.exp(ys_p[i]) * 100.0)

    return (
        fit_ridge(xs_g, ys_g, lam=0.25),
        sum(errs_g) / len(errs_g),
        fit_ridge(xs_p, ys_p, lam=0.25),
        sum(errs_p) / len(errs_p),
    )


# ==============================================================================
# 5. UNIFIED PHYSICAL FRAME-TIME SIMULATOR (Matches Kotlin UnifiedFpsSimulator.kt)
# ==============================================================================
def simulate_game_fps(
    game: GameProfile,
    gpu_index: float,
    gpu_vram_gb: float,
    cpu_gaming_index: float,
    cpu_is_x3d: bool,
    cpu_ccd_count: int,
    cpu_cores: int,
) -> dict[str, int]:
    """
    Physical pipelined frame-time model (L_p norm, p = 3.2):
      T_sys = (T_gpu^p + T_cpu^p)^(1/p)
      FPS_sys = 1000 / T_sys = (F_gpu^(-p) + F_cpu^(-p))^(-1/p)
    Returns dict matching ResolutionFps JSON schema:
      1080p, 1440p, 4k, low1_1080p, low1_1440p, low1_4k
    """
    gpu_ratio = max(gpu_index / 100.0, 0.08)
    cpu_ratio = max(cpu_gaming_index / 100.0, 0.18)

    # Extra per-game 3D V-Cache sensitivity adjustment:
    # Since cpu_gaming_index is already 1080p geometric-mean calibrated (9800X3D=100, 9900X=78),
    # x3d_affinity tilts cache-hungry titles (CS2, Valorant, BG3, Cyberpunk) slightly further apart
    # while keeping cache-neutral titles (Alan Wake 2, Wukong) closer together.
    # Centered around mean affinity = 0.11 so geometric mean stays exactly at cpu_gaming_index.
    affinity_delta = game.x3d_affinity - 0.11
    if cpu_is_x3d:
        cpu_game_mult = 1.0 + affinity_delta * 0.65
    else:
        cpu_game_mult = 1.0 - affinity_delta * 0.65
    cpu_game_mult = max(0.88, min(1.12, cpu_game_mult))

    # Dynamic 1% Low stability modifier (architecture-aware!)
    cpu_low1_mod = 0.0
    if cpu_is_x3d and cpu_ccd_count <= 1:
        cpu_low1_mod += 0.07   # Single-CCD 96 MB 3D V-Cache (9800X3D, 7800X3D, 5800X3D)
    elif cpu_is_x3d and cpu_ccd_count >= 2:
        cpu_low1_mod += 0.04   # Dual-CCD X3D (9950X3D, 7950X3D, 9900X3D)
    elif cpu_ccd_count >= 2:
        cpu_low1_mod -= 0.04   # Dual-CCD non-X3D cross-CCD latency penalty (9900X, 9950X, 7900X)
    if cpu_cores <= 4:
        cpu_low1_mod -= 0.06   # Quad-core frame pacing stutter penalty
    elif cpu_cores <= 6 and not cpu_is_x3d:
        cpu_low1_mod -= 0.02

    out: dict[str, int] = {}
    for res_key, gpu_base, vram_req, cpu_res_drag in (
        ("1080p", game.gpu_1080, game.vram_1080, 1.00),
        ("1440p", game.gpu_1440, game.vram_1440, 0.97),
        ("4k",    game.gpu_4k,   game.vram_4k,   0.94),
    ):
        # Pure GPU capacity with VRAM overflow penalty
        vram_deficit = max(0.0, vram_req - max(gpu_vram_gb, 4.0))
        vram_avg_pen = 1.0 / (1.0 + 0.060 * (vram_deficit ** 1.30))
        vram_low1_pen = min(0.24, 0.042 * (vram_deficit ** 1.25))

        f_gpu = max(8.0, gpu_base * (gpu_ratio ** game.gpu_exp) * vram_avg_pen)
        f_cpu = max(18.0, game.cpu_base * cpu_ratio * cpu_game_mult * cpu_res_drag)

        # Pipelined L_p combination
        f_sys = (f_gpu ** (-FRAME_TIME_P) + f_cpu ** (-FRAME_TIME_P)) ** (-1.0 / FRAME_TIME_P)
        avg_fps = max(10, int(round(f_sys)))

        # Weight CPU low1 modifier by how CPU-bound this resolution/game is
        cpu_share = (f_cpu ** (-FRAME_TIME_P)) / (f_gpu ** (-FRAME_TIME_P) + f_cpu ** (-FRAME_TIME_P))
        eff_low1_ratio = game.base_low1 + cpu_low1_mod * (0.45 + 0.55 * cpu_share) - vram_low1_pen
        eff_low1_ratio = max(0.45, min(0.89, eff_low1_ratio))
        low1_fps = max(6, min(avg_fps - 2, int(round(avg_fps * eff_low1_ratio))))

        out[res_key] = avg_fps
        out[f"low1_{res_key}"] = low1_fps

    return out


def build_12_game_matrix(
    gpu_index: float,
    gpu_vram_gb: float,
    cpu_gaming_index: float,
    cpu_is_x3d: bool,
    cpu_ccd_count: int,
    cpu_cores: int,
) -> dict[str, dict[str, int]]:
    return {
        g.key: simulate_game_fps(
            g, gpu_index, gpu_vram_gb, cpu_gaming_index, cpu_is_x3d, cpu_ccd_count, cpu_cores
        )
        for g in GAME_PROFILES
    }


# ==============================================================================
# 6. CANONICAL MATCHING FOR ALL DB GPUs AND CPUs
# ==============================================================================
# Sort anchors longest-key-first so "rtx 4060 ti 16" matches before "rtx 4060 ti" before "rtx 4060"
GPU_ANCHORS_SORTED = sorted(GPU_ANCHORS, key=lambda x: len(x[0]), reverse=True)
CPU_ANCHORS_SORTED = sorted(CPU_ANCHORS, key=lambda x: len(x[0]), reverse=True)


def match_gpu_anchor(chipset: str, model: str, vram_gb: float):
    norm_c = re.sub(r"\s+", " ", (chipset or "").lower()).strip()
    norm_m = re.sub(r"\s+", " ", (model or "").lower()).strip()
    combined = f"{norm_c} {norm_m}"

    # Special 16GB variant distinction for 4060 Ti / 5060 Ti / 9060 XT
    for base in ("rtx 5060 ti", "rtx 4060 ti", "rx 9060 xt"):
        if base in combined and vram_gb >= 15.5:
            target = f"{base} 16"
            for row in GPU_ANCHORS_SORTED:
                if row[0] == target:
                    return row

    for row in GPU_ANCHORS_SORTED:
        key = row[0]
        # word-boundary match so "rtx 4070" doesn't swallow "rtx 4070 ti" (already sorted longest-first)
        if re.search(rf"(?<![a-z0-9]){re.escape(key)}(?![a-z0-9])", norm_c):
            return row
    for row in GPU_ANCHORS_SORTED:
        key = row[0]
        if re.search(rf"(?<![a-z0-9]){re.escape(key)}(?![a-z0-9])", norm_m):
            return row
    return None


def match_cpu_anchor(model: str):
    norm = re.sub(r"[^a-z0-9]+", " ", (model or "").lower()).strip()
    tokens = norm.split()
    for row in CPU_ANCHORS_SORTED:
        key = row[0]
        if key in tokens:
            return row
    # Fallback substring with strict boundary (handles e.g. "i5-12400F" -> "12400f")
    for row in CPU_ANCHORS_SORTED:
        key = row[0]
        if re.search(rf"(?<![a-z0-9]){re.escape(key)}(?![a-z0-9])", norm):
            return row
    return None


def infer_cpu_arch_ipc(arch: str, socket: str, model: str) -> tuple[float, int, bool]:
    """Fallback architecture IPC tier (CB23 single proxy), ccd_count, is_x3d for unanchored CPUs."""
    a = (arch or "").lower()
    m = (model or "").lower()
    s = (socket or "").lower()
    is_x3d = "x3d" in m
    if "zen 5" in a or "granite ridge" in a or "shimada peak" in a:
        ipc = 2220.0
    elif "arrow lake" in a or "lga1851" in s:
        ipc = 2200.0
    elif "raptor lake" in a:
        ipc = 2020.0
    elif "zen 4" in a or "raphael" in a or "storm peak" in a or "phoenix" in a or "hawk point" in a:
        ipc = 1960.0
    elif "alder lake" in a:
        ipc = 1800.0
    elif "zen 3" in a or "vermeer" in a or "cezanne" in a or "chagall" in a:
        ipc = 1520.0
    elif "rocket lake" in a:
        ipc = 1540.0
    elif "comet lake" in a:
        ipc = 1320.0
    elif "zen 2" in a or "matisse" in a or "renoir" in a or "castle peak" in a:
        ipc = 1260.0
    elif "cascade lake" in a:
        ipc = 1220.0
    elif "zen+" in a:
        ipc = 1040.0
    elif "zen" in a:
        ipc = 940.0
    else:
        ipc = 1350.0
    return ipc, 1, is_x3d


def tier_for_gpu_index(rel: float) -> str:
    if rel >= 95:
        return "S+-Tier"
    if rel >= 72:
        return "S-Tier"
    if rel >= 54:
        return "A+-Tier"
    if rel >= 38:
        return "A-Tier"
    if rel >= 26:
        return "B-Tier"
    return "C-Tier"


def tier_for_cpu(gaming_idx: float, prod_idx: float) -> str:
    peak = max(gaming_idx, min(prod_idx, 105.0))
    if peak >= 92:
        return "S+-Tier"
    if peak >= 80:
        return "S-Tier"
    if peak >= 70:
        return "A+-Tier"
    if peak >= 58:
        return "A-Tier"
    if peak >= 44:
        return "B-Tier"
    return "C-Tier"


# ==============================================================================
# 7. MAIN CALIBRATION ENGINE
# ==============================================================================
def calibrate_all(db_path: Path, apply_room: bool, emit_sql: Path | None) -> None:
    gpu_w, gpu_loo_mape = train_gpu_ml_model()
    cpu_wg, cpu_g_mape, cpu_wp, cpu_p_mape = train_cpu_ml_models()

    print("=== ML LOG-RIDGE MODEL TRAINING & LOO-CV VERIFICATION ===")
    print(f"  GPU Log-Ridge ({len(GPU_ANCHORS)} anchors): LOO-CV MAPE = {gpu_loo_mape:.2f}%")
    print(f"    Weights [b, logVram, logBus, logTdp, logArch]: {[round(x, 5) for x in gpu_w]}")
    print(f"  CPU Gaming Log-Ridge ({len(CPU_ANCHORS)} anchors): LOO-CV MAPE = {cpu_g_mape:.2f}%")
    print(f"    Weights [b, logCbS, logEffL3, isX3d, isDualNonX3d, isArrow]: {[round(x, 5) for x in cpu_wg]}")
    print(f"  CPU Productivity Log-Ridge ({len(CPU_ANCHORS)} anchors): LOO-CV MAPE = {cpu_p_mape:.2f}%")
    print(f"    Weights [b, logCbM, logPM]: {[round(x, 5) for x in cpu_wp]}")

    conn = sqlite3.connect(str(db_path))
    conn.row_factory = sqlite3.Row
    cur = conn.cursor()

    gpu_updates: list[dict[str, Any]] = []
    cpu_updates: list[dict[str, Any]] = []
    cpu_spec_fixes: list[tuple[str, int, str]] = []  # (item_id, l3_mb, core_topology)

    # --- A. Calibrate ALL GPUs ---
    gpu_rows = cur.execute("""
        SELECT h.id, h.brand, h.model, h.gpu_synthetic,
               g.chipset, g.vram_mb, g.boost_clock_mhz, g.tdp_watts, g.bus_width_bits, g.architecture
        FROM hardware_items h JOIN gpu_specs g ON g.item_id = h.id
        WHERE h.category = 'GPU'
    """).fetchall()

    anchored_gpu_count = 0
    for r in gpu_rows:
        vram_gb = max((r["vram_mb"] or 8192) / 1024.0, 2.0)
        boost_mhz = r["boost_clock_mhz"] or 0
        tdp = r["tdp_watts"] or 150
        bus = r["bus_width_bits"] or 128
        arch = (r["architecture"] or "").lower()

        anchor = match_gpu_anchor(r["chipset"], r["model"], vram_gb)
        syn = json.loads(r["gpu_synthetic"]) if r["gpu_synthetic"] else {}

        if anchor is not None:
            anchored_gpu_count += 1
            _, base_rel, ts_g, steel_nomad, ref_boost, _, _, _, _ = anchor
            # Tiny ±2% realistic AIB factory boost OC offset
            if boost_mhz > 800 and ref_boost > 800:
                oc_delta = max(-0.015, min(0.025, ((boost_mhz - ref_boost) / ref_boost) * 0.35))
            else:
                oc_delta = 0.0
            rel_idx = round(base_rel * (1.0 + oc_delta), 1)
            syn["time_spy_graphics"] = int(round(ts_g * (1.0 + oc_delta)))
            syn["steel_nomad"] = int(round(steel_nomad * (1.0 + oc_delta)))
            suite = "TechPowerUp & ML Calibrated v6 (Ref: Ryzen 7 9800X3D)"
        else:
            arch_tier = (
                1.28 if "blackwell" in arch else
                1.24 if "ada" in arch or "rdna 4" in arch else
                1.10 if "rdna 3" in arch or "battlemage" in arch else
                1.00
            )
            pred = math.exp(predict_linear(gpu_w, [
                1.0, math.log(vram_gb), math.log(max(bus, 64)), math.log(max(tdp, 40)), math.log(arch_tier)
            ]))
            rel_idx = round(max(8.0, min(145.0, pred)), 1)
            syn.setdefault("time_spy_graphics", int(round(rel_idx * 355.0)))
            syn.setdefault("steel_nomad", int(round(rel_idx * 92.0)))
            suite = "ML Log-Ridge Calibrated v6 (Ref: Ryzen 7 9800X3D)"

        # Normalized 1..100 score (RTX 5090 = 100 -> zero 100-cap ties!)
        score_100 = max(5, min(100, int(round((rel_idx / 133.0) * 100.0))))
        # Uncapped TechPowerUp Relative Index stored in benchmark_gaming_index (RTX 4090 = 100, RTX 5090 = 133)
        gaming_idx_int = max(5, int(round(rel_idx)))
        tier = tier_for_gpu_index(rel_idx)

        game_fps = build_12_game_matrix(
            gpu_index=rel_idx,
            gpu_vram_gb=vram_gb,
            cpu_gaming_index=REF_CPU_GAMING,
            cpu_is_x3d=REF_CPU_X3D,
            cpu_ccd_count=REF_CPU_CCD,
            cpu_cores=REF_CPU_CORES,
        )

        gpu_updates.append({
            "id": r["id"],
            "model": r["model"],
            "chipset": r["chipset"],
            "score": score_100,
            "gaming_index": gaming_idx_int,
            "tier": tier,
            "suite": suite,
            "gpu_synthetic": syn,
            "game_fps": game_fps,
        })

    # --- B. Calibrate ALL CPUs ---
    cpu_rows = cur.execute("""
        SELECT h.id, h.brand, h.model, h.cpu_synthetic,
               c.core_count, c.thread_count, c.base_clock_mhz, c.boost_clock_mhz,
               c.tdp_watts, c.l3_cache_mb, c.architecture, c.socket, c.core_topology
        FROM hardware_items h JOIN cpu_specs c ON c.item_id = h.id
        WHERE h.category = 'CPU'
    """).fetchall()

    anchored_cpu_count = 0
    for r in cpu_rows:
        model = r["model"]
        cores = r["core_count"] or 6
        boost_mhz = r["boost_clock_mhz"] or r["base_clock_mhz"] or 4200
        tdp = r["tdp_watts"] or 65
        l3_mb = r["l3_cache_mb"] or 16
        arch = r["architecture"] or ""
        socket = r["socket"] or ""
        syn = json.loads(r["cpu_synthetic"]) if r["cpu_synthetic"] else {}

        anchor = match_cpu_anchor(model)
        if anchor is not None:
            anchored_cpu_count += 1
            _, g_idx, p_idx, cb_s, cb_m, pm, l3_fix, ccd_count, is_x3d = anchor
            # Apply low-TDP variant scaling (T / E / GE / TE) if suffix present
            m_low = model.lower()
            is_low_power = bool(re.search(r"\b\d{4,5}(t|te|e|ge)\b", m_low))
            if is_low_power:
                g_idx = round(g_idx * 0.87, 1)
                p_idx = round(p_idx * 0.72, 1)
                cb_s = int(round(cb_s * 0.91))
                cb_m = int(round(cb_m * 0.72))
                pm = int(round(pm * 0.72))

            if l3_fix is not None:
                ccd_label = (
                    "Çift CCD • 3D V-Cache" if (ccd_count >= 2 and is_x3d) else
                    "Tek CCD • 3D V-Cache" if is_x3d else
                    "Çift CCD" if ccd_count >= 2 else
                    "Tek CCD"
                )
                topo = f"{ccd_count}×{cores // max(ccd_count, 1)}C ({ccd_label})"
                if l3_mb != l3_fix or (is_x3d and ccd_count >= 2) or r["id"] in (
                    "cpu_amd_ryzen_5_3600x", "cpu_amd_ryzen_5_9600",
                    "cpu_amd_ryzen_7_3700x", "cpu_amd_ryzen_7_3800x",
                    "cpu_amd_ryzen_9_3900x", "cpu_amd_ryzen_9_3950x",
                ):
                    cpu_spec_fixes.append((r["id"], l3_fix, topo))
                l3_mb = l3_fix

            syn["cinebench_r23_single"] = cb_s
            syn["cinebench_r23_multi"] = cb_m
            syn.setdefault("passmark_cpu_mark", pm)
            suite = "TechPowerUp & Cinebench Calibrated v6 (Ref: RTX 5090)"
        else:
            # ML Log-Ridge prediction for unanchored CPUs (Threadripper, older Gen, niche SKUs)
            ipc_proxy, _, is_x3d = infer_cpu_arch_ipc(arch, socket, model)
            is_tr = "threadripper" in model.lower() or cores >= 24 and "amd" in (r["brand"] or "").lower()
            ccd_count = max(2, cores // 8) if is_tr else (2 if ("amd" in (r["brand"] or "").lower() and cores >= 12) else 1)
            eff_l3 = (l3_mb or 16) / float(max(ccd_count, 1))
            dual_non_x3d = 1.0 if (ccd_count >= 2 and not is_x3d) else 0.0
            arrow_lake = 1.0 if ("arrow lake" in arch.lower() or "lga1851" in socket.lower()) else 0.0

            pm_raw = syn.get("passmark_cpu_mark") or int(cores * (boost_mhz / 1000.0) * (ipc_proxy / 2.2))
            cb_s = syn.get("cinebench_r23_single") or int(round(ipc_proxy * (boost_mhz / 5000.0) ** 0.55))
            cb_m = syn.get("cinebench_r23_multi") or int(round(pm_raw * 0.64))
            syn["cinebench_r23_single"] = max(600, cb_s)
            syn["cinebench_r23_multi"] = max(3000, cb_m)
            syn.setdefault("passmark_cpu_mark", pm_raw)

            pred_g = math.exp(predict_linear(cpu_wg, [
                1.0, math.log(max(cb_s, 800)), math.log(max(eff_l3, 8.0)),
                1.0 if is_x3d else 0.0, dual_non_x3d, arrow_lake,
            ]))
            # Threadripper NUMA/multi-CCD penalty in gaming
            if is_tr:
                pred_g *= 0.86
            # Low-TDP penalty (35W T-series)
            if tdp <= 40:
                pred_g *= 0.88
            elif cores <= 4:
                pred_g *= 0.86
            g_idx = round(max(22.0, min(98.0, pred_g)), 1)

            pred_p = math.exp(predict_linear(cpu_wp, [
                1.0, math.log(max(cb_m, 3000)), math.log(max(pm_raw, 5000)),
            ]))
            p_idx = round(max(12.0, min(260.0, pred_p)), 1)
            suite = "ML Log-Ridge Calibrated v6 (Ref: RTX 5090)"

        # Composite 1..100 overall score (55% Gaming + 45% capped Productivity)
        overall_100 = max(10, min(100, int(round(0.55 * g_idx + 0.45 * min(100.0, p_idx)))))
        gaming_idx_int = max(15, min(105, int(round(g_idx))))
        tier = tier_for_cpu(g_idx, p_idx)

        game_fps = build_12_game_matrix(
            gpu_index=REF_GPU_INDEX,
            gpu_vram_gb=REF_GPU_VRAM,
            cpu_gaming_index=g_idx,
            cpu_is_x3d=is_x3d,
            cpu_ccd_count=ccd_count,
            cpu_cores=cores,
        )

        cpu_updates.append({
            "id": r["id"],
            "model": model,
            "score": overall_100,
            "gaming_index": gaming_idx_int,
            "prod_index": int(round(p_idx)),
            "tier": tier,
            "suite": suite,
            "cpu_synthetic": syn,
            "game_fps": game_fps,
            "l3_mb": l3_mb,
            "ccd_count": ccd_count,
            "is_x3d": is_x3d,
        })

    print(f"\n=== COVERAGE SUMMARY ===")
    print(f"  GPUs calibrated: {len(gpu_updates)} (Anchored: {anchored_gpu_count} / {len(gpu_updates)} = {anchored_gpu_count/len(gpu_updates)*100:.1f}%)")
    print(f"  CPUs calibrated: {len(cpu_updates)} (Anchored: {anchored_cpu_count} / {len(cpu_updates)} = {anchored_cpu_count/len(cpu_updates)*100:.1f}%)")
    print(f"  CPU L3/topology fixes: {len(cpu_spec_fixes)} -> {cpu_spec_fixes[:10]}")

    # --- C. Sanity Verification Printout ---
    print("\n=== SANITY CHECK: KEY CPUs (Cyberpunk 1080p/1440p/4K + CS2 1080p) ===")
    key_cpus = ["9800X3D", "9950X3D", "9900X3D", "7800X3D", "9700X", "9950X", "9900X", "14900K", "285K", "12400F", "12400"]
    by_cpu = {u["model"]: u for u in cpu_updates}
    for k in key_cpus:
        matches = [u for m, u in by_cpu.items() if re.search(rf"(?<![A-Za-z0-9]){re.escape(k)}(?![A-Za-z0-9])", m, re.I)]
        if matches:
            u = min(matches, key=lambda x: len(x["model"]))
            cp = u["game_fps"]["cyberpunk_2077"]
            cs = u["game_fps"]["counter_strike_2"]
            print(
                f"  {u['model']:<22} | gIdx={u['gaming_index']:>3} pIdx={u['prod_index']:>3} sc={u['score']:>3} | "
                f"L3={u['l3_mb']:>3}M CCD={u['ccd_count']} | "
                f"CP={cp['1080p']}/{cp['1440p']}/{cp['4k']} (1%L:{cp['low1_1080p']}) | "
                f"CS2={cs['1080p']} (1%L:{cs['low1_1080p']})"
            )

    print("\n=== SANITY CHECK: KEY GPUs (Cyberpunk 1080p/1440p/4K) ===")
    for chip in ("GeForce RTX 5090", "GeForce RTX 4090", "GeForce RTX 5080", "GeForce RTX 4080 SUPER",
                 "Radeon RX 7900 XTX", "GeForce RTX 5070 Ti", "Radeon RX 9070 XT", "GeForce RTX 5070",
                 "GeForce RTX 4070", "GeForce RTX 4060 Ti", "GeForce RTX 4060", "GeForce RTX 3060"):
        matches = [u for u in gpu_updates if (u["chipset"] or "").lower() == chip.lower()]
        if matches:
            u = matches[0]
            cp = u["game_fps"]["cyberpunk_2077"]
            print(
                f"  {chip:<24} | relIdx={u['gaming_index']:>3} sc100={u['score']:>3} ({u['tier']:<7}) | "
                f"CP={cp['1080p']:>3}/{cp['1440p']:>3}/{cp['4k']:>3} (1%L 4K:{cp['low1_4k']:>3})"
            )

    # Verify exact 9800X3D + RTX 5090 identity across CPU Detail & GPU Detail!
    u_9800 = next(u for u in cpu_updates if "9800X3D" in u["model"].upper())
    u_5090 = next(u for u in gpu_updates if (u["chipset"] or "").lower() == "geforce rtx 5090")
    cp_cpu = u_9800["game_fps"]["cyberpunk_2077"]
    cp_gpu = u_5090["game_fps"]["cyberpunk_2077"]
    print(f"\n=== 3-SCREEN IDENTITY CHECK (9800X3D + RTX 5090 Cyberpunk 2077) ===")
    print(f"  CPU Detail (9800X3D + Ref 5090): {cp_cpu}")
    print(f"  GPU Detail (RTX 5090 + Ref 9800X3D): {cp_gpu}")

    # --- D. Apply to Room SQLite DB ---
    if apply_room:
        print(f"\nApplying updates to Room DB: {db_path} ...")
        for item_id, l3_fix, topo in cpu_spec_fixes:
            cur.execute(
                "UPDATE cpu_specs SET l3_cache_mb = ?, core_topology = COALESCE(NULLIF(core_topology, ''), ?) WHERE item_id = ?",
                (l3_fix, topo, item_id),
            )
            # Explicitly update topology on fixed X3D rows too
            cur.execute("UPDATE cpu_specs SET core_topology = ? WHERE item_id = ?", (topo, item_id))

        for u in gpu_updates:
            cur.execute(
                """
                UPDATE hardware_items
                SET benchmark_score = ?,
                    benchmark_gaming_index = ?,
                    benchmark_tier = ?,
                    benchmark_test_suite = ?,
                    gpu_synthetic = ?,
                    game_fps = ?
                WHERE id = ?
                """,
                (
                    u["score"], u["gaming_index"], u["tier"], u["suite"],
                    json.dumps(u["gpu_synthetic"], separators=(",", ":")),
                    json.dumps(u["game_fps"], separators=(",", ":")),
                    u["id"],
                ),
            )

        for u in cpu_updates:
            cur.execute(
                """
                UPDATE hardware_items
                SET benchmark_score = ?,
                    benchmark_gaming_index = ?,
                    benchmark_tier = ?,
                    benchmark_test_suite = ?,
                    cpu_synthetic = ?,
                    game_fps = ?
                WHERE id = ?
                """,
                (
                    u["score"], u["gaming_index"], u["tier"], u["suite"],
                    json.dumps(u["cpu_synthetic"], separators=(",", ":")),
                    json.dumps(u["game_fps"], separators=(",", ":")),
                    u["id"],
                ),
            )

        # Bump catalog_revision in app_settings so bundled DB matches Supabase rev 7
        has_settings = cur.execute(
            "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='app_settings'"
        ).fetchone()[0]
        if has_settings:
            cur.execute(
                "INSERT OR REPLACE INTO app_settings (key, value) VALUES ('catalog_revision', ?)",
                (str(TARGET_CATALOG_REVISION),),
            )

        conn.commit()
        print(f"  Room DB updated successfully (app_settings.catalog_revision = {TARGET_CATALOG_REVISION})!")

    # --- E. Emit Ultra-Compact Pure-Postgres Calibration SQL (~32 KB total!) ---
    if emit_sql is not None:
        print(f"\nWriting compact pure-Postgres Supabase migration SQL -> {emit_sql} ...")

        def sql_str(s: str | None) -> str:
            if s is None:
                return "NULL"
            return "'" + s.replace("'", "''") + "'"

        game_tuples = ",\n    ".join(
            f"('{g.key}', {g.gpu_1080}, {g.gpu_1440}, {g.gpu_4k}, {g.cpu_base}, "
            f"{g.gpu_exp}, {g.x3d_affinity}, {g.vram_1080}, {g.vram_1440}, {g.vram_4k}, {g.base_low1})"
            for g in GAME_PROFILES
        )

        lines: list[str] = [
            "-- ==============================================================================",
            "-- HARDWARE WIZARD — UNIFIED ML-CALIBRATED BENCHMARKS & X3D FIX (catalog_revision = 7)",
            "-- Generated by scripts/calibrate_benchmarks_ml.py",
            "-- Compact pure-Postgres L_p (p=3.2) physical frame-time calibration for all CPUs & GPUs.",
            "-- ==============================================================================",
            "BEGIN;",
            "",
            "-- 1. Transaction-scoped physical L_p (p=3.2) 12-game FPS simulator (matches Kotlin UnifiedFpsSimulator)",
            "CREATE OR REPLACE FUNCTION pg_temp.sim_v6_matrix(",
            "  gpu_idx DOUBLE PRECISION, gpu_vram DOUBLE PRECISION,",
            "  cpu_idx DOUBLE PRECISION, is_x3d BOOLEAN, ccd_cnt INT, cores INT",
            ") RETURNS JSONB LANGUAGE plpgsql IMMUTABLE AS $$",
            "DECLARE",
            "  p CONSTANT DOUBLE PRECISION := 3.2;",
            "  g RECORD;",
            "  gpu_r DOUBLE PRECISION := GREATEST(gpu_idx / 100.0, 0.08);",
            "  cpu_r DOUBLE PRECISION := GREATEST(cpu_idx / 100.0, 0.18);",
            "  aff_d DOUBLE PRECISION; cmult DOUBLE PRECISION; low_mod DOUBLE PRECISION := 0.0;",
            "  vdef DOUBLE PRECISION; vpen DOUBLE PRECISION; vlpen DOUBLE PRECISION;",
            "  fg DOUBLE PRECISION; fc DOUBLE PRECISION; fsys DOUBLE PRECISION; cshare DOUBLE PRECISION;",
            "  lrat DOUBLE PRECISION; a1080 INT; l1080 INT; a1440 INT; l1440 INT; a4k INT; l4k INT;",
            "  res JSONB := '{}'::jsonb;",
            "BEGIN",
            "  IF is_x3d AND ccd_cnt <= 1 THEN low_mod := 0.07;",
            "  ELSIF is_x3d AND ccd_cnt >= 2 THEN low_mod := 0.04;",
            "  ELSIF ccd_cnt >= 2 THEN low_mod := -0.04; END IF;",
            "  IF cores <= 4 THEN low_mod := low_mod - 0.06;",
            "  ELSIF cores <= 6 AND NOT is_x3d THEN low_mod := low_mod - 0.02; END IF;",
            "",
            "  FOR g IN SELECT * FROM (VALUES",
            f"    {game_tuples}",
            "  ) AS t(k, g1080, g1440, g4k, cbase, gexp, xaff, v1080, v1440, v4k, blow1) LOOP",
            "    aff_d := g.xaff - 0.11;",
            "    cmult := LEAST(1.12, GREATEST(0.88, CASE WHEN is_x3d THEN 1.0 + aff_d * 0.65 ELSE 1.0 - aff_d * 0.65 END));",
            "",
            "    vdef := GREATEST(0.0, g.v1080 - GREATEST(gpu_vram, 4.0));",
            "    vpen := 1.0 / (1.0 + 0.060 * POWER(vdef, 1.30)); vlpen := LEAST(0.24, 0.042 * POWER(vdef, 1.25));",
            "    fg := GREATEST(8.0, g.g1080 * POWER(gpu_r, g.gexp) * vpen);",
            "    fc := GREATEST(18.0, g.cbase * cpu_r * cmult);",
            "    fsys := POWER(POWER(fg, -p) + POWER(fc, -p), -1.0 / p);",
            "    cshare := POWER(fc, -p) / (POWER(fg, -p) + POWER(fc, -p));",
            "    a1080 := GREATEST(10, ROUND(fsys::NUMERIC)::INT);",
            "    lrat := LEAST(0.89, GREATEST(0.45, g.blow1 + low_mod * (0.45 + 0.55 * cshare) - vlpen));",
            "    l1080 := GREATEST(6, LEAST(a1080 - 2, ROUND((a1080 * lrat)::NUMERIC)::INT));",
            "",
            "    vdef := GREATEST(0.0, g.v1440 - GREATEST(gpu_vram, 4.0));",
            "    vpen := 1.0 / (1.0 + 0.060 * POWER(vdef, 1.30)); vlpen := LEAST(0.24, 0.042 * POWER(vdef, 1.25));",
            "    fg := GREATEST(8.0, g.g1440 * POWER(gpu_r, g.gexp) * vpen);",
            "    fc := GREATEST(18.0, g.cbase * cpu_r * cmult * 0.97);",
            "    fsys := POWER(POWER(fg, -p) + POWER(fc, -p), -1.0 / p);",
            "    cshare := POWER(fc, -p) / (POWER(fg, -p) + POWER(fc, -p));",
            "    a1440 := GREATEST(10, ROUND(fsys::NUMERIC)::INT);",
            "    lrat := LEAST(0.89, GREATEST(0.45, g.blow1 + low_mod * (0.45 + 0.55 * cshare) - vlpen));",
            "    l1440 := GREATEST(6, LEAST(a1440 - 2, ROUND((a1440 * lrat)::NUMERIC)::INT));",
            "",
            "    vdef := GREATEST(0.0, g.v4k - GREATEST(gpu_vram, 4.0));",
            "    vpen := 1.0 / (1.0 + 0.060 * POWER(vdef, 1.30)); vlpen := LEAST(0.24, 0.042 * POWER(vdef, 1.25));",
            "    fg := GREATEST(8.0, g.g4k * POWER(gpu_r, g.gexp) * vpen);",
            "    fc := GREATEST(18.0, g.cbase * cpu_r * cmult * 0.94);",
            "    fsys := POWER(POWER(fg, -p) + POWER(fc, -p), -1.0 / p);",
            "    cshare := POWER(fc, -p) / (POWER(fg, -p) + POWER(fc, -p));",
            "    a4k := GREATEST(10, ROUND(fsys::NUMERIC)::INT);",
            "    lrat := LEAST(0.89, GREATEST(0.45, g.blow1 + low_mod * (0.45 + 0.55 * cshare) - vlpen));",
            "    l4k := GREATEST(6, LEAST(a4k - 2, ROUND((a4k * lrat)::NUMERIC)::INT));",
            "",
            "    res := res || jsonb_build_object(g.k, jsonb_build_object(",
            "      '1080p', a1080, 'low1_1080p', l1080,",
            "      '1440p', a1440, 'low1_1440p', l1440,",
            "      '4k', a4k, 'low1_4k', l4k",
            "    ));",
            "  END LOOP;",
            "  RETURN res;",
            "END; $$;",
            "",
            "-- 2. Fix corrupted L3 cache & topology in spec_payload (Ryzen 9 9950X3D & 9900X3D -> 128 MB, etc.)",
        ]
        for item_id, l3_fix, topo in cpu_spec_fixes:
            esc_id = item_id.replace("'", "''")
            esc_topo = topo.replace("'", "''")
            lines.append(
                f"UPDATE public.hardware_catalog SET spec_payload = jsonb_set(jsonb_set(spec_payload, '{{l3_cache_mb}}', '{l3_fix}'::jsonb, true), '{{core_topology}}', to_jsonb('{esc_topo}'::text), true) WHERE id = '{esc_id}';"
            )

        lines.extend([
            "",
            f"-- 3. Update all {len(cpu_updates)} CPUs (exact calibrated scores, synthetics & Ref RTX 5090 FPS)",
            "UPDATE public.hardware_catalog AS hc SET",
            "  benchmark_score = v.sc,",
            "  benchmark_gaming_index = v.gi,",
            "  benchmark_tier = v.tr,",
            "  benchmark_test_suite = v.st,",
            "  cpu_synthetic = COALESCE(hc.cpu_synthetic, '{}'::jsonb) || jsonb_build_object('cinebench_r23_single', v.cbs, 'cinebench_r23_multi', v.cbm, 'passmark_cpu_mark', v.pm),",
            "  game_fps = pg_temp.sim_v6_matrix(133.0, 32.0, v.gi::DOUBLE PRECISION, v.x3d, v.ccd, COALESCE((hc.spec_payload->>'core_count')::INT, 8)),",
            f"  catalog_revision = {TARGET_CATALOG_REVISION},",
            "  updated_at = NOW()",
            "FROM (VALUES",
        ])
        cpu_vals = []
        for u in cpu_updates:
            syn = u["cpu_synthetic"]
            st_short = "TechPowerUp & Cinebench Calibrated v6 (Ref: RTX 5090)" if "TechPowerUp" in u["suite"] else "ML Log-Ridge Calibrated v6 (Ref: RTX 5090)"
            cpu_vals.append(
                f"  ({sql_str(u['id'])},{u['score']},{u['gaming_index']},{sql_str(u['tier'])},{sql_str(st_short)},"
                f"{syn.get('cinebench_r23_single', 1500)},{syn.get('cinebench_r23_multi', 15000)},{syn.get('passmark_cpu_mark', 20000)},"
                f"{'TRUE' if u['is_x3d'] else 'FALSE'},{u['ccd_count']})"
            )
        lines.append(",\n".join(cpu_vals))
        lines.append(") AS v(id, sc, gi, tr, st, cbs, cbm, pm, x3d, ccd)")
        lines.append("WHERE hc.id = v.id;")
        lines.append("")

        # Group GPUs cleanly by chipset anchor rules (63 rows instead of 1,298!)
        # Match order: longest key first, plus 16GB variant distinction for 4060 Ti / 5060 Ti / 9060 XT
        lines.extend([
            f"-- 4. Update all {len(gpu_updates)} GPUs via verified 63-chipset TechPowerUp anchors (+ AIB factory boost OC delta)",
            "WITH gpu_anchors(ord, kw, base_rel, ts_g, sn, ref_boost, need16) AS (VALUES",
        ])
        anc_vals = []
        for idx, (key, rel, ts_g, sn, ref_b, vram_a, _, _, _) in enumerate(GPU_ANCHORS_SORTED, 1):
            need16 = key.endswith(" 16")
            base_kw = key[:-3] if need16 else key
            anc_vals.append(f"  ({idx},{sql_str(base_kw)},{rel},{ts_g},{sn},{ref_b},{'TRUE' if need16 else 'FALSE'})")
        lines.append(",\n".join(anc_vals))
        lines.extend([
            "), matched_gpu AS (",
            "  SELECT DISTINCT ON (hc.id)",
            "    hc.id,",
            "    GREATEST(COALESCE((hc.spec_payload->>'vram_mb')::DOUBLE PRECISION / 1024.0, 8.0), 2.0) AS vram_gb,",
            "    COALESCE((hc.spec_payload->>'boost_clock_mhz')::DOUBLE PRECISION, 0.0) AS boost_mhz,",
            "    a.base_rel, a.ts_g, a.sn, a.ref_boost",
            "  FROM public.hardware_catalog hc",
            "  JOIN gpu_anchors a ON (",
            "    (LOWER(COALESCE(hc.spec_payload->>'chipset','') || ' ' || hc.model) ~ ('(^|[^a-z0-9])' || a.kw || '([^a-z0-9]|$)'))",
            "    AND (NOT a.need16 OR COALESCE((hc.spec_payload->>'vram_mb')::INT, 0) >= 15800)",
            "  )",
            "  WHERE hc.category = 'GPU' AND hc.is_active = TRUE",
            "  ORDER BY hc.id, a.ord ASC",
            "), computed_gpu AS (",
            "  SELECT",
            "    id, vram_gb,",
            "    ROUND((base_rel * (1.0 + CASE WHEN boost_mhz > 800 AND ref_boost > 800 THEN LEAST(0.025, GREATEST(-0.015, ((boost_mhz - ref_boost) / ref_boost) * 0.35)) ELSE 0.0 END))::NUMERIC, 1)::DOUBLE PRECISION AS rel_idx,",
            "    ROUND((ts_g * (1.0 + CASE WHEN boost_mhz > 800 AND ref_boost > 800 THEN LEAST(0.025, GREATEST(-0.015, ((boost_mhz - ref_boost) / ref_boost) * 0.35)) ELSE 0.0 END))::NUMERIC)::INT AS ts_adj,",
            "    ROUND((sn * (1.0 + CASE WHEN boost_mhz > 800 AND ref_boost > 800 THEN LEAST(0.025, GREATEST(-0.015, ((boost_mhz - ref_boost) / ref_boost) * 0.35)) ELSE 0.0 END))::NUMERIC)::INT AS sn_adj",
            "  FROM matched_gpu",
            ")",
            "UPDATE public.hardware_catalog AS hc SET",
            "  benchmark_score = GREATEST(5, LEAST(100, ROUND(((cg.rel_idx / 133.0) * 100.0)::NUMERIC)::INT)),",
            "  benchmark_gaming_index = GREATEST(5, ROUND(cg.rel_idx::NUMERIC)::INT),",
            "  benchmark_tier = CASE WHEN cg.rel_idx >= 95 THEN 'S+-Tier' WHEN cg.rel_idx >= 72 THEN 'S-Tier' WHEN cg.rel_idx >= 54 THEN 'A+-Tier' WHEN cg.rel_idx >= 38 THEN 'A-Tier' WHEN cg.rel_idx >= 26 THEN 'B-Tier' ELSE 'C-Tier' END,",
            "  benchmark_test_suite = 'TechPowerUp & ML Calibrated v6 (Ref: Ryzen 7 9800X3D)',",
            "  gpu_synthetic = COALESCE(hc.gpu_synthetic, '{}'::jsonb) || jsonb_build_object('time_spy_graphics', cg.ts_adj, 'steel_nomad', cg.sn_adj),",
            "  game_fps = pg_temp.sim_v6_matrix(cg.rel_idx, cg.vram_gb, 100.0, TRUE, 1, 8),",
            f"  catalog_revision = {TARGET_CATALOG_REVISION},",
            "  updated_at = NOW()",
            "FROM computed_gpu cg",
            "WHERE hc.id = cg.id;",
            "",
            "-- 5. Bump catalog_meta.latest_revision to 7 so mobile clients pull the calibrated delta",
            f"UPDATE public.catalog_meta SET latest_revision = {TARGET_CATALOG_REVISION}, last_delta_at = NOW(), last_checked_at = NOW() WHERE id = 1;",
            "COMMIT;",
            "",
        ])
        emit_sql.write_text("\n".join(lines), encoding="utf-8")
        print(f"  Wrote {emit_sql.stat().st_size / 1024:.1f} KB ultra-compact pure-Postgres SQL migration!")

    conn.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--apply-room", action="store_true", help="Write calibrated values into bundled Room DB")
    parser.add_argument("--emit-sql", action="store_true", help="Generate supabase_benchmark_v6_calibration.sql")
    args = parser.parse_args()
    calibrate_all(
        db_path=ROOM_DB_PATH,
        apply_room=args.apply_room,
        emit_sql=SQL_OUT_PATH if args.emit_sql else None,
    )

#!/usr/bin/env python3
"""
Hardware Wizard - Genişletilmiş CPU-Monkey Benchmark Veri Tabanı Oluşturucu
CPU ve GPU'lar için TechPowerUp / CPU-Monkey standartlarında çoklu sentetik test paketi:
- CPU: Cinebench 2024, Cinebench R23, Geekbench 6, PassMark CPU Mark
- GPU: 3DMark Time Spy (Graphics), Time Spy Extreme, Steel Nomad
- Oyunlar: Cyberpunk 2077, Forza Horizon 5, Counter-Strike 2 (1080p, 1440p, 4K)
"""

import json
import os
import re

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
FILES_DIR = os.path.join(ROOT_DIR, "shared", "src", "commonMain", "composeResources", "files")

TARGETS = [
    os.path.join(ROOT_DIR, "data", "hardware_benchmarks.json"),
    os.path.join(FILES_DIR, "hardware_benchmarks.json")
]


def sanitize(name: str) -> str:
    return re.sub(r"[^a-z0-9]+", "_", name.lower()).strip("_")


# Doğrulanmış CPU Benchmark Verileri (TechPowerUp + Cinebench + Geekbench 6 + PassMark)
CPU_DB = [
    {
        "keywords": ["9900X"],
        "name": "AMD Ryzen 9 9900X",
        "gaming_relative_index": 0.95,
        "synthetic": {
            "cinebench_2024_single": 134,
            "cinebench_2024_multi": 2180,
            "cinebench_r23_single": 2240,
            "cinebench_r23_multi": 33200,
            "geekbench_6_single": 3410,
            "geekbench_6_multi": 20450,
            "passmark_cpu_mark": 54200
        },
        "fps_cp": (168.0, 134.0, 89.0),
        "fps_fh": (245.0, 200.0, 132.0),
        "fps_cs": (590.0, 470.0, 290.0)
    },
    {
        "keywords": ["9800X3D"],
        "name": "AMD Ryzen 7 9800X3D",
        "gaming_relative_index": 1.08,
        "synthetic": {
            "cinebench_2024_single": 142,
            "cinebench_2024_multi": 1560,
            "cinebench_r23_single": 2150,
            "cinebench_r23_multi": 23400,
            "geekbench_6_single": 3480,
            "geekbench_6_multi": 18200,
            "passmark_cpu_mark": 46500
        },
        "fps_cp": (185.0, 150.0, 98.0),
        "fps_fh": (265.0, 218.0, 145.0),
        "fps_cs": (640.0, 510.0, 310.0)
    },
    {
        "keywords": ["7800X3D"],
        "name": "AMD Ryzen 7 7800X3D",
        "gaming_relative_index": 1.00,
        "synthetic": {
            "cinebench_2024_single": 110,
            "cinebench_2024_multi": 1120,
            "cinebench_r23_single": 1820,
            "cinebench_r23_multi": 18500,
            "geekbench_6_single": 2780,
            "geekbench_6_multi": 15400,
            "passmark_cpu_mark": 35200
        },
        "fps_cp": (165.0, 132.0, 88.0),
        "fps_fh": (240.0, 195.0, 130.0),
        "fps_cs": (580.0, 460.0, 285.0)
    },
    {
        "keywords": ["7950X3D"],
        "name": "AMD Ryzen 9 7950X3D",
        "gaming_relative_index": 0.99,
        "synthetic": {
            "cinebench_2024_single": 122,
            "cinebench_2024_multi": 2040,
            "cinebench_r23_single": 2050,
            "cinebench_r23_multi": 36000,
            "geekbench_6_single": 3020,
            "geekbench_6_multi": 20800,
            "passmark_cpu_mark": 62500
        },
        "fps_cp": (162.0, 130.0, 87.0),
        "fps_fh": (236.0, 192.0, 128.0),
        "fps_cs": (570.0, 455.0, 280.0)
    },
    {
        "keywords": ["14900K"],
        "name": "Intel Core i9-14900K",
        "gaming_relative_index": 0.98,
        "synthetic": {
            "cinebench_2024_single": 138,
            "cinebench_2024_multi": 2240,
            "cinebench_r23_single": 2300,
            "cinebench_r23_multi": 40500,
            "geekbench_6_single": 3150,
            "geekbench_6_multi": 21400,
            "passmark_cpu_mark": 61200
        },
        "fps_cp": (158.0, 128.0, 86.0),
        "fps_fh": (230.0, 188.0, 126.0),
        "fps_cs": (565.0, 450.0, 278.0)
    },
    {
        "keywords": ["14700K"],
        "name": "Intel Core i7-14700K",
        "gaming_relative_index": 0.94,
        "synthetic": {
            "cinebench_2024_single": 130,
            "cinebench_2024_multi": 1940,
            "cinebench_r23_single": 2180,
            "cinebench_r23_multi": 34800,
            "geekbench_6_single": 2980,
            "geekbench_6_multi": 19500,
            "passmark_cpu_mark": 53500
        },
        "fps_cp": (152.0, 124.0, 84.0),
        "fps_fh": (224.0, 182.0, 122.0),
        "fps_cs": (545.0, 435.0, 270.0)
    },
    {
        "keywords": ["13900K"],
        "name": "Intel Core i9-13900K",
        "gaming_relative_index": 0.96,
        "synthetic": {
            "cinebench_2024_single": 134,
            "cinebench_2024_multi": 2180,
            "cinebench_r23_single": 2240,
            "cinebench_r23_multi": 38800,
            "geekbench_6_single": 3080,
            "geekbench_6_multi": 20800,
            "passmark_cpu_mark": 59800
        },
        "fps_cp": (155.0, 126.0, 85.0),
        "fps_fh": (226.0, 184.0, 124.0),
        "fps_cs": (550.0, 440.0, 272.0)
    },
    {
        "keywords": ["13700K"],
        "name": "Intel Core i7-13700K",
        "gaming_relative_index": 0.92,
        "synthetic": {
            "cinebench_2024_single": 126,
            "cinebench_2024_multi": 1820,
            "cinebench_r23_single": 2120,
            "cinebench_r23_multi": 30800,
            "geekbench_6_single": 2900,
            "geekbench_6_multi": 18400,
            "passmark_cpu_mark": 47200
        },
        "fps_cp": (148.0, 120.0, 82.0),
        "fps_fh": (218.0, 178.0, 118.0),
        "fps_cs": (530.0, 425.0, 265.0)
    },
    {
        "keywords": ["7900X"],
        "name": "AMD Ryzen 9 7900X",
        "gaming_relative_index": 0.91,
        "synthetic": {
            "cinebench_2024_single": 120,
            "cinebench_2024_multi": 1680,
            "cinebench_r23_single": 2020,
            "cinebench_r23_multi": 29000,
            "geekbench_6_single": 2940,
            "geekbench_6_multi": 18600,
            "passmark_cpu_mark": 52100
        },
        "fps_cp": (145.0, 118.0, 81.0),
        "fps_fh": (212.0, 174.0, 116.0),
        "fps_cs": (520.0, 415.0, 260.0)
    },
    {
        "keywords": ["7700X"],
        "name": "AMD Ryzen 7 7700X",
        "gaming_relative_index": 0.86,
        "synthetic": {
            "cinebench_2024_single": 118,
            "cinebench_2024_multi": 1140,
            "cinebench_r23_single": 2000,
            "cinebench_r23_multi": 19800,
            "geekbench_6_single": 2880,
            "geekbench_6_multi": 14200,
            "passmark_cpu_mark": 36100
        },
        "fps_cp": (140.0, 114.0, 79.0),
        "fps_fh": (205.0, 168.0, 112.0),
        "fps_cs": (495.0, 395.0, 250.0)
    },
    {
        "keywords": ["14600K"],
        "name": "Intel Core i5-14600K",
        "gaming_relative_index": 0.85,
        "synthetic": {
            "cinebench_2024_single": 124,
            "cinebench_2024_multi": 1420,
            "cinebench_r23_single": 2070,
            "cinebench_r23_multi": 24800,
            "geekbench_6_single": 2840,
            "geekbench_6_multi": 15800,
            "passmark_cpu_mark": 39500
        },
        "fps_cp": (138.0, 112.0, 78.0),
        "fps_fh": (202.0, 165.0, 110.0),
        "fps_cs": (485.0, 390.0, 245.0)
    },
    {
        "keywords": ["5800X3D"],
        "name": "AMD Ryzen 7 5800X3D",
        "gaming_relative_index": 0.82,
        "synthetic": {
            "cinebench_2024_single": 92,
            "cinebench_2024_multi": 860,
            "cinebench_r23_single": 1490,
            "cinebench_r23_multi": 15100,
            "geekbench_6_single": 2180,
            "geekbench_6_multi": 11500,
            "passmark_cpu_mark": 28400
        },
        "fps_cp": (135.0, 110.0, 77.0),
        "fps_fh": (198.0, 162.0, 108.0),
        "fps_cs": (475.0, 380.0, 240.0)
    },
    {
        "keywords": ["13600K"],
        "name": "Intel Core i5-13600K",
        "gaming_relative_index": 0.81,
        "synthetic": {
            "cinebench_2024_single": 120,
            "cinebench_2024_multi": 1380,
            "cinebench_r23_single": 2000,
            "cinebench_r23_multi": 24100,
            "geekbench_6_single": 2780,
            "geekbench_6_multi": 15200,
            "passmark_cpu_mark": 38200
        },
        "fps_cp": (134.0, 109.0, 76.0),
        "fps_fh": (196.0, 160.0, 107.0),
        "fps_cs": (470.0, 375.0, 238.0)
    },
    {
        "keywords": ["7600X"],
        "name": "AMD Ryzen 5 7600X",
        "gaming_relative_index": 0.79,
        "synthetic": {
            "cinebench_2024_single": 115,
            "cinebench_2024_multi": 890,
            "cinebench_r23_single": 1950,
            "cinebench_r23_multi": 15200,
            "geekbench_6_single": 2820,
            "geekbench_6_multi": 11800,
            "passmark_cpu_mark": 28900
        },
        "fps_cp": (130.0, 105.0, 74.0),
        "fps_fh": (190.0, 155.0, 104.0),
        "fps_cs": (450.0, 360.0, 230.0)
    },
    {
        "keywords": ["5800X"],
        "name": "AMD Ryzen 7 5800X",
        "gaming_relative_index": 0.72,
        "synthetic": {
            "cinebench_2024_single": 98,
            "cinebench_2024_multi": 910,
            "cinebench_r23_single": 1600,
            "cinebench_r23_multi": 15300,
            "geekbench_6_single": 2240,
            "geekbench_6_multi": 11800,
            "passmark_cpu_mark": 28100
        },
        "fps_cp": (120.0, 98.0, 70.0),
        "fps_fh": (175.0, 142.0, 96.0),
        "fps_cs": (410.0, 330.0, 210.0)
    },
    {
        "keywords": ["5600X"],
        "name": "AMD Ryzen 5 5600X",
        "gaming_relative_index": 0.65,
        "synthetic": {
            "cinebench_2024_single": 94,
            "cinebench_2024_multi": 680,
            "cinebench_r23_single": 1540,
            "cinebench_r23_multi": 11200,
            "geekbench_6_single": 2180,
            "geekbench_6_multi": 8600,
            "passmark_cpu_mark": 22100
        },
        "fps_cp": (110.0, 89.0, 64.0),
        "fps_fh": (160.0, 130.0, 88.0),
        "fps_cs": (370.0, 295.0, 190.0)
    },
    {
        "keywords": ["12400"],
        "name": "Intel Core i5-12400F",
        "gaming_relative_index": 0.58,
        "synthetic": {
            "cinebench_2024_single": 102,
            "cinebench_2024_multi": 720,
            "cinebench_r23_single": 1690,
            "cinebench_r23_multi": 12400,
            "geekbench_6_single": 2280,
            "geekbench_6_multi": 9400,
            "passmark_cpu_mark": 19800
        },
        "fps_cp": (102.0, 82.0, 60.0),
        "fps_fh": (148.0, 120.0, 82.0),
        "fps_cs": (340.0, 270.0, 175.0)
    }
]

# Doğrulanmış GPU Benchmark Verileri (TechPowerUp + 3DMark Time Spy / Extreme / Steel Nomad)
GPU_DB = [
    {
        "chipset_key": "rtx_5090",
        "keywords": ["5090"],
        "name": "GeForce RTX 5090",
        "relative_performance_pct": 1.95,
        "synthetic": {
            "time_spy_graphics": 48500,
            "time_spy_extreme": 26800,
            "steel_nomad": 13400
        },
        "fps_cp": (240.0, 195.0, 118.0),
        "fps_fh": (320.0, 270.0, 198.0),
        "fps_cs": (780.0, 680.0, 460.0)
    },
    {
        "chipset_key": "rtx_5080",
        "keywords": ["5080"],
        "name": "GeForce RTX 5080",
        "relative_performance_pct": 1.45,
        "synthetic": {
            "time_spy_graphics": 37200,
            "time_spy_extreme": 19600,
            "steel_nomad": 9650
        },
        "fps_cp": (185.0, 145.0, 88.0),
        "fps_fh": (265.0, 220.0, 155.0),
        "fps_cs": (660.0, 560.0, 365.0)
    },
    {
        "chipset_key": "rtx_5070_ti",
        "keywords": ["5070 Ti", "5070Ti", "5070 TI"],
        "name": "GeForce RTX 5070 Ti",
        "relative_performance_pct": 1.25,
        "synthetic": {
            "time_spy_graphics": 30500,
            "time_spy_extreme": 15800,
            "steel_nomad": 7450
        },
        "fps_cp": (158.0, 122.0, 72.0),
        "fps_fh": (235.0, 190.0, 134.0),
        "fps_cs": (580.0, 480.0, 310.0)
    },
    {
        "chipset_key": "rtx_5070",
        "keywords": ["5070"],
        "name": "GeForce RTX 5070",
        "relative_performance_pct": 1.08,
        "synthetic": {
            "time_spy_graphics": 25800,
            "time_spy_extreme": 13100,
            "steel_nomad": 6150
        },
        "fps_cp": (135.0, 102.0, 58.0),
        "fps_fh": (202.0, 158.0, 110.0),
        "fps_cs": (500.0, 400.0, 255.0)
    },
    {
        "chipset_key": "rtx_4090",
        "keywords": ["4090"],
        "name": "GeForce RTX 4090",
        "relative_performance_pct": 1.35,
        "synthetic": {
            "time_spy_graphics": 36500,
            "time_spy_extreme": 19400,
            "steel_nomad": 9420
        },
        "fps_cp": (172.0, 134.0, 82.0),
        "fps_fh": (250.0, 205.0, 148.0),
        "fps_cs": (610.0, 510.0, 340.0)
    },
    {
        "chipset_key": "rx_7900_xtx",
        "keywords": ["7900 XTX", "7900XTX"],
        "name": "Radeon RX 7900 XTX",
        "relative_performance_pct": 1.18,
        "synthetic": {
            "time_spy_graphics": 29500,
            "time_spy_extreme": 15200,
            "steel_nomad": 6900
        },
        "fps_cp": (148.0, 114.0, 66.0),
        "fps_fh": (224.0, 180.0, 126.0),
        "fps_cs": (550.0, 450.0, 295.0)
    },
    {
        "chipset_key": "rtx_4080_super",
        "keywords": ["4080 SUPER", "4080Super"],
        "name": "GeForce RTX 4080 SUPER",
        "relative_performance_pct": 1.15,
        "synthetic": {
            "time_spy_graphics": 28400,
            "time_spy_extreme": 14100,
            "steel_nomad": 6750
        },
        "fps_cp": (142.0, 108.0, 62.0),
        "fps_fh": (210.0, 165.0, 115.0),
        "fps_cs": (520.0, 420.0, 260.0)
    },
    {
        "chipset_key": "rtx_4080",
        "keywords": ["4080"],
        "name": "GeForce RTX 4080",
        "relative_performance_pct": 1.11,
        "synthetic": {
            "time_spy_graphics": 27100,
            "time_spy_extreme": 13500,
            "steel_nomad": 6480
        },
        "fps_cp": (138.0, 104.0, 59.0),
        "fps_fh": (204.0, 160.0, 110.0),
        "fps_cs": (505.0, 405.0, 252.0)
    },
    {
        "chipset_key": "rx_7900_xt",
        "keywords": ["7900 XT", "7900XT"],
        "name": "Radeon RX 7900 XT",
        "relative_performance_pct": 1.02,
        "synthetic": {
            "time_spy_graphics": 25600,
            "time_spy_extreme": 12900,
            "steel_nomad": 5850
        },
        "fps_cp": (128.0, 96.0, 54.0),
        "fps_fh": (192.0, 150.0, 104.0),
        "fps_cs": (470.0, 375.0, 240.0)
    },
    {
        "chipset_key": "rtx_4070_ti_super",
        "keywords": ["4070 Ti SUPER", "4070Ti SUPER", "4070 Ti Super", "4070ti super"],
        "name": "GeForce RTX 4070 Ti SUPER",
        "relative_performance_pct": 1.01,
        "synthetic": {
            "time_spy_graphics": 24500,
            "time_spy_extreme": 12100,
            "steel_nomad": 5720
        },
        "fps_cp": (126.0, 94.0, 53.0),
        "fps_fh": (188.0, 146.0, 101.0),
        "fps_cs": (465.0, 370.0, 235.0)
    },
    {
        "chipset_key": "rtx_4070_ti",
        "keywords": ["4070 Ti", "4070Ti", "4070 TI"],
        "name": "GeForce RTX 4070 Ti",
        "relative_performance_pct": 0.96,
        "synthetic": {
            "time_spy_graphics": 22900,
            "time_spy_extreme": 11200,
            "steel_nomad": 5350
        },
        "fps_cp": (120.0, 89.0, 50.0),
        "fps_fh": (180.0, 138.0, 95.0),
        "fps_cs": (440.0, 350.0, 220.0)
    },
    {
        "chipset_key": "rx_7900_gre",
        "keywords": ["7900 GRE", "7900GRE"],
        "name": "Radeon RX 7900 GRE",
        "relative_performance_pct": 0.94,
        "synthetic": {
            "time_spy_graphics": 22400,
            "time_spy_extreme": 11000,
            "steel_nomad": 5200
        },
        "fps_cp": (118.0, 88.0, 49.0),
        "fps_fh": (176.0, 135.0, 92.0),
        "fps_cs": (430.0, 340.0, 215.0)
    },
    {
        "chipset_key": "rtx_4070_super",
        "keywords": ["4070 SUPER", "4070Super", "4070 super"],
        "name": "GeForce RTX 4070 SUPER",
        "relative_performance_pct": 0.91,
        "synthetic": {
            "time_spy_graphics": 21200,
            "time_spy_extreme": 10200,
            "steel_nomad": 4920
        },
        "fps_cp": (115.0, 85.0, 47.0),
        "fps_fh": (172.0, 132.0, 89.0),
        "fps_cs": (420.0, 330.0, 205.0)
    },
    {
        "chipset_key": "rx_7800_xt",
        "keywords": ["7800 XT", "7800XT"],
        "name": "Radeon RX 7800 XT",
        "relative_performance_pct": 0.85,
        "synthetic": {
            "time_spy_graphics": 19800,
            "time_spy_extreme": 9600,
            "steel_nomad": 4480
        },
        "fps_cp": (109.0, 80.0, 44.0),
        "fps_fh": (165.0, 126.0, 84.0),
        "fps_cs": (405.0, 315.0, 195.0)
    },
    {
        "chipset_key": "rtx_4070",
        "keywords": ["4070"],
        "name": "GeForce RTX 4070",
        "relative_performance_pct": 0.81,
        "synthetic": {
            "time_spy_graphics": 18200,
            "time_spy_extreme": 8800,
            "steel_nomad": 4180
        },
        "fps_cp": (102.0, 75.0, 41.0),
        "fps_fh": (155.0, 118.0, 78.0),
        "fps_cs": (380.0, 295.0, 185.0)
    },
    {
        "chipset_key": "rx_6800_xt",
        "keywords": ["6800 XT", "6800XT"],
        "name": "Radeon RX 6800 XT",
        "relative_performance_pct": 0.80,
        "synthetic": {
            "time_spy_graphics": 18100,
            "time_spy_extreme": 8700,
            "steel_nomad": 4100
        },
        "fps_cp": (100.0, 74.0, 40.0),
        "fps_fh": (152.0, 116.0, 77.0),
        "fps_cs": (375.0, 290.0, 180.0)
    },
    {
        "chipset_key": "rtx_3080",
        "keywords": ["3080"],
        "name": "GeForce RTX 3080",
        "relative_performance_pct": 0.78,
        "synthetic": {
            "time_spy_graphics": 17800,
            "time_spy_extreme": 8600,
            "steel_nomad": 4050
        },
        "fps_cp": (98.0, 72.0, 39.0),
        "fps_fh": (150.0, 114.0, 75.0),
        "fps_cs": (370.0, 285.0, 178.0)
    },
    {
        "chipset_key": "rx_7700_xt",
        "keywords": ["7700 XT", "7700XT"],
        "name": "Radeon RX 7700 XT",
        "relative_performance_pct": 0.72,
        "synthetic": {
            "time_spy_graphics": 17000,
            "time_spy_extreme": 8100,
            "steel_nomad": 3820
        },
        "fps_cp": (92.0, 67.0, 36.0),
        "fps_fh": (140.0, 106.0, 70.0),
        "fps_cs": (345.0, 265.0, 165.0)
    },
    {
        "chipset_key": "rtx_3070",
        "keywords": ["3070"],
        "name": "GeForce RTX 3070",
        "relative_performance_pct": 0.65,
        "synthetic": {
            "time_spy_graphics": 14200,
            "time_spy_extreme": 6800,
            "steel_nomad": 3250
        },
        "fps_cp": (84.0, 60.0, 32.0),
        "fps_fh": (130.0, 98.0, 63.0),
        "fps_cs": (320.0, 245.0, 150.0)
    },
    {
        "chipset_key": "rtx_4060_ti",
        "keywords": ["4060 Ti", "4060Ti", "4060 TI"],
        "name": "GeForce RTX 4060 Ti",
        "relative_performance_pct": 0.64,
        "synthetic": {
            "time_spy_graphics": 13600,
            "time_spy_extreme": 6400,
            "steel_nomad": 3120
        },
        "fps_cp": (82.0, 58.0, 30.0),
        "fps_fh": (126.0, 94.0, 60.0),
        "fps_cs": (310.0, 235.0, 142.0)
    },
    {
        "chipset_key": "rx_6700_xt",
        "keywords": ["6700 XT", "6700XT"],
        "name": "Radeon RX 6700 XT",
        "relative_performance_pct": 0.58,
        "synthetic": {
            "time_spy_graphics": 12800,
            "time_spy_extreme": 5900,
            "steel_nomad": 2850
        },
        "fps_cp": (76.0, 53.0, 27.0),
        "fps_fh": (118.0, 87.0, 55.0),
        "fps_cs": (290.0, 218.0, 132.0)
    },
    {
        "chipset_key": "rtx_4060",
        "keywords": ["4060"],
        "name": "GeForce RTX 4060",
        "relative_performance_pct": 0.51,
        "synthetic": {
            "time_spy_graphics": 10800,
            "time_spy_extreme": 4900,
            "steel_nomad": 2420
        },
        "fps_cp": (68.0, 46.0, 23.0),
        "fps_fh": (106.0, 77.0, 48.0),
        "fps_cs": (260.0, 195.0, 118.0)
    },
    {
        "chipset_key": "rx_7600",
        "keywords": ["7600"],
        "name": "Radeon RX 7600",
        "relative_performance_pct": 0.49,
        "synthetic": {
            "time_spy_graphics": 10600,
            "time_spy_extreme": 4800,
            "steel_nomad": 2380
        },
        "fps_cp": (66.0, 44.0, 22.0),
        "fps_fh": (102.0, 74.0, 46.0),
        "fps_cs": (250.0, 188.0, 114.0)
    },
    {
        "chipset_key": "rtx_3060",
        "keywords": ["3060"],
        "name": "GeForce RTX 3060",
        "relative_performance_pct": 0.39,
        "synthetic": {
            "time_spy_graphics": 8850,
            "time_spy_extreme": 4100,
            "steel_nomad": 1980
        },
        "fps_cp": (54.0, 36.0, 18.0),
        "fps_fh": (84.0, 60.0, 36.0),
        "fps_cs": (205.0, 150.0, 90.0)
    },
    {
        "chipset_key": "rx_6600",
        "keywords": ["6600"],
        "name": "Radeon RX 6600",
        "relative_performance_pct": 0.41,
        "synthetic": {
            "time_spy_graphics": 8400,
            "time_spy_extreme": 3900,
            "steel_nomad": 1850
        },
        "fps_cp": (56.0, 37.0, 19.0),
        "fps_fh": (86.0, 62.0, 38.0),
        "fps_cs": (212.0, 155.0, 94.0)
    }
]


def match_entry(name: str, db: list):
    for entry in db:
        for kw in entry["keywords"]:
            if kw.lower() in name.lower():
                return entry
    return None


def build_benchmarks():
    result = {
        "cpus": {},
        "gpus": {}
    }

    # CPU katalog dosyasını tara
    cpu_file = os.path.join(FILES_DIR, "cpu.json")
    if os.path.exists(cpu_file):
        with open(cpu_file, "r", encoding="utf-8") as f:
            cpus = json.load(f)
        for c in cpus:
            name = c.get("name", "")
            if not name:
                continue
            pid = f"cpu_{sanitize(name)}"
            m = match_entry(name, CPU_DB)
            if m:
                result["cpus"][pid] = {
                    "gaming_relative_index": m["gaming_relative_index"],
                    "synthetic": m["synthetic"],
                    "games": {
                        "cyberpunk_2077": {"p1080": m["fps_cp"][0], "p1440": m["fps_cp"][1], "p4k": m["fps_cp"][2]},
                        "forza_horizon_5": {"p1080": m["fps_fh"][0], "p1440": m["fps_fh"][1], "p4k": m["fps_fh"][2]},
                        "counter_strike_2": {"p1080": m["fps_cs"][0], "p1440": m["fps_cs"][1], "p4k": m["fps_cs"][2]}
                    }
                }

    # 1. GPU Çip Benchmarklarını doğrudan ekle (Tüm alt modellerin eşleşmesi için)
    for entry in GPU_DB:
        ck = entry.get("chipset_key")
        if ck:
            bench_data = {
                "relative_performance_pct": entry["relative_performance_pct"],
                "synthetic": entry["synthetic"],
                "games": {
                    "cyberpunk_2077": {"p1080": entry["fps_cp"][0], "p1440": entry["fps_cp"][1], "p4k": entry["fps_cp"][2]},
                    "forza_horizon_5": {"p1080": entry["fps_fh"][0], "p1440": entry["fps_fh"][1], "p4k": entry["fps_fh"][2]},
                    "counter_strike_2": {"p1080": entry["fps_cs"][0], "p1440": entry["fps_cs"][1], "p4k": entry["fps_cs"][2]}
                }
            }
            result["gpus"][f"gpu_{ck}"] = bench_data
            result["gpus"][ck] = bench_data

    # 2. GPU katalog dosyasını tara ve alt model ID'lerini de eşleştir
    gpu_file = os.path.join(FILES_DIR, "video-card.json")
    if os.path.exists(gpu_file):
        with open(gpu_file, "r", encoding="utf-8") as f:
            gpus = json.load(f)
        for g in gpus:
            name = g.get("name", "")
            if not name:
                continue
            pid = f"gpu_{sanitize(name)}"
            m = match_entry(name, GPU_DB)
            if m:
                result["gpus"][pid] = {
                    "relative_performance_pct": m["relative_performance_pct"],
                    "synthetic": m["synthetic"],
                    "games": {
                        "cyberpunk_2077": {"p1080": m["fps_cp"][0], "p1440": m["fps_cp"][1], "p4k": m["fps_cp"][2]},
                        "forza_horizon_5": {"p1080": m["fps_fh"][0], "p1440": m["fps_fh"][1], "p4k": m["fps_fh"][2]},
                        "counter_strike_2": {"p1080": m["fps_cs"][0], "p1440": m["fps_cs"][1], "p4k": m["fps_cs"][2]}
                    }
                }

    cpu_count = len(result["cpus"])
    gpu_count = len(result["gpus"])
    print(f"Toplam doğrulanmış CPU: {cpu_count}, GPU: {gpu_count}")

    for target in TARGETS:
        with open(target, "w", encoding="utf-8") as f:
            json.dump(result, f, indent=2, ensure_ascii=False)
        print(f"Genişletilmiş benchmark verisi kaydedildi: {target}")


if __name__ == "__main__":
    build_benchmarks()

#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
scripts/generate_missing_reference_images.py
Generates the 19 ultra-clean, high-resolution dark-theme studio reference images
for internal PC hardware (CPU, GPU, RAM, Motherboard, Storage, PSU, Cooler).
"""

import os
import sys

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8")

from PIL import Image, ImageDraw, ImageFont, ImageFilter

ROOT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
IMAGES_DIR = os.path.join(ROOT_DIR, "data", "images")

# Ensure target directories exist
for sub in ["cpu", "gpu", "motherboard", "ram", "storage", "psu", "cooler", "case"]:
    os.makedirs(os.path.join(IMAGES_DIR, sub), exist_ok=True)

WIDTH, HEIGHT = 500, 500

def get_font(size, bold=False):
    # Try common Windows fonts, fallback to default
    font_names = [
        "C:\\Windows\\Fonts\\segoeuib.ttf" if bold else "C:\\Windows\\Fonts\\segoeui.ttf",
        "C:\\Windows\\Fonts\\consola.ttf",
        "C:\\Windows\\Fonts\\arial.ttf"
    ]
    for fn in font_names:
        if os.path.exists(fn):
            try:
                return ImageFont.truetype(fn, size)
            except Exception:
                pass
    return ImageFont.load_default()

def create_base_canvas(accent_color=(0, 229, 255)):
    # Dark modern studio slate gradient background
    img = Image.new("RGBA", (WIDTH, HEIGHT), (14, 18, 27, 255))
    draw = ImageDraw.Draw(img)
    
    # Subtle radial glow from center
    for r in range(240, 40, -20):
        alpha = int((1 - r / 240.0) * 28)
        color = (accent_color[0], accent_color[1], accent_color[2], alpha)
        draw.ellipse([WIDTH//2 - r, HEIGHT//2 - r, WIDTH//2 + r, HEIGHT//2 + r], fill=color)
        
    # Subtle studio floor grid lines
    grid_col = (255, 255, 255, 12)
    for y in range(320, HEIGHT, 25):
        draw.line([(30, y), (WIDTH - 30, y)], fill=grid_col, width=1)
    for x in range(50, WIDTH, 40):
        draw.line([(x, 320), (x + (x - WIDTH//2)//2, HEIGHT)], fill=grid_col, width=1)
        
    return img, draw

def add_studio_badge(draw, title, subtitle, accent_hex="#00e5ff"):
    # Header badge
    title_font = get_font(18, bold=True)
    sub_font = get_font(13, bold=False)
    tech_font = get_font(10, bold=False)
    
    draw.text((250, 420), title, font=title_font, fill=(245, 248, 255, 255), anchor="mm")
    draw.text((250, 445), subtitle, font=sub_font, fill=(148, 163, 184, 255), anchor="mm")
    draw.text((250, 470), "HARDWARE WIZARD • ULTRA 4K STUDIO VIEW", font=tech_font, fill=(0, 229, 255, 160), anchor="mm")

# 1. AMD AM5 IHS Studio
def gen_amd_am5_ihs():
    img, draw = create_base_canvas(accent_color=(255, 107, 0))
    # AM5 Distinctive Octopus/Cutout IHS
    # Center Nickel plate with 8 cutout wings
    # Base PCB (Dark Green / Black Substrate)
    draw.rounded_rectangle([130, 90, 370, 330], radius=14, fill=(18, 24, 20), outline=(212, 175, 55), width=2)
    # Nickel IHS Body
    draw.rounded_rectangle([150, 110, 350, 310], radius=18, fill=(175, 182, 192), outline=(220, 226, 235), width=3)
    # 8 Cutout notches on AM5 IHS
    notches = [
        [145, 140, 162, 180], [145, 240, 162, 280], # Left
        [338, 140, 355, 180], [338, 240, 355, 280], # Right
        [180, 105, 220, 122], [280, 105, 320, 122], # Top
        [180, 298, 220, 315], [280, 298, 320, 315], # Bottom
    ]
    for n in notches:
        draw.rounded_rectangle(n, radius=4, fill=(18, 24, 20), outline=(212, 175, 55), width=1)
        
    # Laser engraving
    font_brand = get_font(26, bold=True)
    font_model = get_font(18, bold=True)
    font_spec = get_font(11, bold=False)
    draw.text((250, 175), "AMD Ryzen™", font=font_brand, fill=(50, 55, 65), anchor="mm")
    draw.text((250, 210), "SOCKET AM5", font=font_model, fill=(70, 75, 88), anchor="mm")
    draw.text((250, 240), "ZEN 5 / ZEN 4 ARCHITECTURE", font=font_spec, fill=(90, 95, 108), anchor="mm")
    draw.text((250, 260), "DIFFUSED IN TAIWAN • MADE IN MALAYSIA", font=font_spec, fill=(100, 105, 118), anchor="mm")
    
    # Gold corner triangle
    draw.polygon([(136, 100), (150, 100), (136, 114)], fill=(255, 215, 0))
    add_studio_badge(draw, "AMD AM5 Nikel Isı Dağıtıcı (IHS)", "Hassas CNC Kesimli Entegre Isı Yayıcı Açı")
    return img

# 2. AMD AM4 IHS Studio
def gen_amd_am4_ihs():
    img, draw = create_base_canvas(accent_color=(255, 107, 0))
    # AM4 Rectangular IHS on green substrate
    draw.rectangle([135, 95, 365, 325], fill=(20, 32, 22), outline=(212, 175, 55), width=2)
    draw.rounded_rectangle([155, 115, 345, 305], radius=6, fill=(178, 185, 195), outline=(225, 230, 238), width=3)
    
    font_brand = get_font(26, bold=True)
    font_model = get_font(18, bold=True)
    font_spec = get_font(11, bold=False)
    draw.text((250, 175), "AMD Ryzen™", font=font_brand, fill=(50, 55, 65), anchor="mm")
    draw.text((250, 210), "SOCKET AM4", font=font_model, fill=(70, 75, 88), anchor="mm")
    draw.text((250, 240), "ZEN 3 / ZEN 2 • PGA 1331", font=font_spec, fill=(90, 95, 108), anchor="mm")
    draw.text((250, 260), "HIGH THERMAL CONDUCTIVITY IHS", font=font_spec, fill=(100, 105, 118), anchor="mm")
    
    draw.polygon([(140, 102), (154, 102), (140, 116)], fill=(255, 215, 0))
    add_studio_badge(draw, "AMD AM4 Klasik Isı Dağıtıcı (IHS)", "Saf Bakır Üzeri Nikel Kaplama Referans Açı")
    return img

# 3. AMD AM5 LGA1718 Bottom
def gen_amd_am5_bottom():
    img, draw = create_base_canvas(accent_color=(255, 214, 0))
    # Substrate
    draw.rounded_rectangle([130, 90, 370, 330], radius=14, fill=(15, 28, 20), outline=(255, 214, 0), width=2)
    # Gold Contact Pad Grid (Dense dots)
    for x in range(145, 356, 7):
        for y in range(105, 316, 7):
            # Skip center capacitor cutout cavity
            if 205 <= x <= 295 and 165 <= y <= 255:
                continue
            draw.point((x, y), fill=(255, 215, 0, 220))
            draw.point((x+1, y), fill=(255, 190, 0, 180))
            
    # Center MLCC capacitor bay
    draw.rectangle([205, 165, 295, 255], fill=(35, 38, 45), outline=(100, 110, 125), width=2)
    # Capacitors inside cavity
    for cx in range(215, 290, 15):
        for cy in range(175, 250, 15):
            draw.rounded_rectangle([cx, cy, cx+10, cy+8], radius=2, fill=(185, 145, 110), outline=(220, 220, 220), width=1)
            
    draw.polygon([(136, 100), (150, 100), (136, 114)], fill=(255, 215, 0))
    add_studio_badge(draw, "AMD AM5 LGA1718 Alt Temas Yüzeyi", "1718 Altın Temas Pedi ve Merkez Dekuplaj Kapasitörleri")
    return img

# 4. Intel LGA1700 IHS Studio
def gen_intel_lga1700_ihs():
    img, draw = create_base_canvas(accent_color=(0, 150, 255))
    # Intel elongated rectangular substrate (LGA 1700: 37.5 x 45.0 mm)
    draw.rounded_rectangle([145, 75, 355, 345], radius=8, fill=(22, 28, 38), outline=(0, 150, 255), width=2)
    # Nickel IHS
    draw.rounded_rectangle([160, 95, 340, 325], radius=6, fill=(175, 182, 192), outline=(225, 230, 238), width=3)
    
    font_brand = get_font(26, bold=True)
    font_model = get_font(17, bold=True)
    font_spec = get_font(11, bold=False)
    draw.text((250, 165), "intel®", font=font_brand, fill=(0, 102, 204), anchor="mm")
    draw.text((250, 200), "CORE™ PROCESSOR", font=font_model, fill=(45, 55, 68), anchor="mm")
    draw.text((250, 230), "LGA1700 • HYBRID ARCHITECTURE", font=font_spec, fill=(70, 80, 95), anchor="mm")
    draw.text((250, 255), "INTEL 7 PROCESS • S-SPEC SRMBH", font=font_spec, fill=(90, 100, 115), anchor="mm")
    
    draw.polygon([(150, 82), (164, 82), (150, 96)], fill=(255, 215, 0))
    add_studio_badge(draw, "Intel LGA1700 Dikdörtgen IHS", "12/13/14. Nesil Hibrit İşlemci Isı Yayıcı Açı")
    return img

# 5. Intel LGA1851 IHS Studio
def gen_intel_lga1851_ihs():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # Intel LGA1851 Core Ultra Next-Gen IHS
    draw.rounded_rectangle([145, 75, 355, 345], radius=8, fill=(18, 26, 38), outline=(0, 229, 255), width=2)
    draw.rounded_rectangle([160, 95, 340, 325], radius=6, fill=(178, 185, 196), outline=(230, 236, 245), width=3)
    
    font_brand = get_font(24, bold=True)
    font_model = get_font(17, bold=True)
    font_spec = get_font(11, bold=False)
    draw.text((250, 165), "intel® Core™", font=font_brand, fill=(0, 102, 204), anchor="mm")
    draw.text((250, 200), "ULTRA PROCESSOR", font=font_model, fill=(45, 55, 68), anchor="mm")
    draw.text((250, 230), "LGA1851 • ARROW LAKE ARCHITECTURE", font=font_spec, fill=(70, 80, 95), anchor="mm")
    draw.text((250, 255), "FUSION PACKAGING • NPU ENGINE", font=font_spec, fill=(90, 100, 115), anchor="mm")
    
    draw.polygon([(150, 82), (164, 82), (150, 96)], fill=(0, 229, 255))
    add_studio_badge(draw, "Intel LGA1851 Core Ultra IHS", "Yeni Nesil Arrow Lake Entegre NPU Isı Yayıcı Açı")
    return img

# 6. Intel LGA1700 Bottom
def gen_intel_lga1700_bottom():
    img, draw = create_base_canvas(accent_color=(255, 214, 0))
    # LGA 1700 substrate bottom
    draw.rounded_rectangle([145, 75, 355, 345], radius=8, fill=(18, 28, 22), outline=(255, 214, 0), width=2)
    # Dense LGA gold pad array
    for x in range(156, 345, 6):
        for y in range(86, 335, 6):
            if 210 <= x <= 290 and 150 <= y <= 270:
                continue
            draw.point((x, y), fill=(255, 215, 0, 220))
            draw.point((x+1, y), fill=(255, 185, 0, 180))
            
    # Center Capacitor Bank
    draw.rectangle([210, 150, 290, 270], fill=(32, 36, 42), outline=(120, 130, 145), width=2)
    for cx in range(218, 285, 14):
        for cy in range(160, 260, 12):
            draw.rounded_rectangle([cx, cy, cx+9, cy+6], radius=2, fill=(175, 135, 95), outline=(210, 210, 210), width=1)
            
    draw.polygon([(150, 82), (164, 82), (150, 96)], fill=(255, 215, 0))
    add_studio_badge(draw, "Intel LGA1700 Alt Temas Pedi", "1700 Altın Temas Pini Matrisi & Merkezi Güç Kapasitörleri")
    return img

# 7. Generic CPU IHS
def gen_generic_cpu_ihs():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    draw.rounded_rectangle([135, 85, 365, 335], radius=12, fill=(22, 28, 38), outline=(0, 229, 255), width=2)
    draw.rounded_rectangle([155, 105, 345, 315], radius=10, fill=(180, 188, 198), outline=(235, 240, 248), width=3)
    
    font_brand = get_font(24, bold=True)
    font_model = get_font(18, bold=True)
    font_spec = get_font(11, bold=False)
    draw.text((250, 175), "HIGH PERFORMANCE", font=font_brand, fill=(45, 55, 68), anchor="mm")
    draw.text((250, 210), "DESKTOP PROCESSOR", font=font_model, fill=(65, 75, 88), anchor="mm")
    draw.text((250, 245), "PRECISION NICKEL-PLATED COPPER IHS", font=font_spec, fill=(90, 100, 115), anchor="mm")
    
    draw.polygon([(140, 92), (154, 92), (140, 106)], fill=(0, 229, 255))
    add_studio_badge(draw, "Yüksek Performanslı İşlemci IHS", "Saf Bakır Üzeri Nikel Kaplama Entegre Isı Yayıcı")
    return img

# 8. GPU Angle Studio Reference
def gen_gpu_angle():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # 45-degree angle GPU perspective: shroud, fans, fin stack, RGB
    # Metal Shroud Body
    draw.polygon([(100, 200), (370, 130), (420, 230), (150, 310)], fill=(28, 33, 44), outline=(0, 229, 255), width=2)
    # Aluminum Fin Stack (horizontal lines)
    for i in range(15):
        y_off = i * 6
        draw.line([(140 + i*2, 210 + y_off), (370 + i*2, 145 + y_off)], fill=(120, 135, 155), width=2)
    # Triple Fans (angled ellipses)
    fan_centers = [(180, 240), (270, 210), (355, 185)]
    for fc in fan_centers:
        draw.ellipse([fc[0]-35, fc[1]-25, fc[0]+35, fc[1]+25], fill=(18, 22, 30), outline=(80, 95, 115), width=2)
        draw.ellipse([fc[0]-12, fc[1]-8, fc[0]+12, fc[1]+8], fill=(0, 229, 255), outline=(255, 255, 255), width=1)
    # RGB light strip
    draw.line([(120, 215), (385, 148)], fill=(0, 229, 255), width=3)
    add_studio_badge(draw, "Ekran Kartı Açılı Stüdyo Çekimi", "Üçlü Fan Soğutma Bloğu & Aerodinamik Metal Gövde")
    return img

# 9. GPU Backplate & I/O
def gen_gpu_backplate():
    img, draw = create_base_canvas(accent_color=(41, 121, 255))
    # Full metal backplate with geometric ventilation cutouts & PCIe bracket
    draw.rounded_rectangle([110, 120, 410, 290], radius=8, fill=(24, 28, 38), outline=(60, 75, 95), width=2)
    # Airflow pass-through cutouts
    for x in range(320, 390, 15):
        draw.rounded_rectangle([x, 140, x+8, 270], radius=3, fill=(10, 14, 20), outline=(0, 229, 255), width=1)
    # PCIe gold fingers bottom
    draw.rectangle([130, 290, 260, 305], fill=(212, 175, 55), outline=(160, 130, 40), width=1)
    # Steel I/O Bracket left
    draw.rounded_rectangle([85, 100, 110, 310], radius=4, fill=(195, 205, 215), outline=(230, 235, 240), width=2)
    # DisplayPort & HDMI ports
    for y in [130, 170, 210, 250]:
        draw.rectangle([92, y, 103, y+18], fill=(30, 35, 45), outline=(120, 130, 140), width=1)
    # Brushed texture lines
    for y in range(130, 280, 8):
        draw.line([(120, y), (300, y)], fill=(32, 38, 50), width=1)
        
    add_studio_badge(draw, "Ekran Kartı Arka Plaka & Çıkışlar", "Metal Güçlendirilmiş Backplate & DisplayPort / HDMI Portları")
    return img

# 10. Motherboard VRM Angle
def gen_motherboard_vrm():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # Angled Motherboard PCB
    draw.polygon([(80, 230), (330, 110), (430, 230), (180, 330)], fill=(18, 22, 30), outline=(0, 229, 255), width=2)
    # Heavy VRM Aluminum Heatsink Block Top
    draw.polygon([(140, 180), (280, 115), (310, 145), (170, 210)], fill=(45, 52, 65), outline=(180, 195, 210), width=2)
    # Finned Heatpipe
    draw.line([(150, 195), (290, 130)], fill=(212, 145, 55), width=4)
    # CPU Socket Center
    draw.polygon([(210, 225), (290, 185), (330, 225), (250, 265)], fill=(120, 125, 135), outline=(255, 215, 0), width=2)
    # PCIe 5.0 Steel Armor Slot
    draw.polygon([(120, 270), (240, 215), (255, 225), (135, 280)], fill=(210, 220, 230), outline=(255, 255, 255), width=1)
    add_studio_badge(draw, "Anakart VRM Güç Devresi & Zırh", "Ağır Alüminyum VRM Blokları & PCIe 5.0 Çelik Zırhlı Yuva")
    return img

# 11. Motherboard Rear I/O
def gen_motherboard_rear_io():
    img, draw = create_base_canvas(accent_color=(0, 230, 118))
    # Rear I/O Shield Panel
    draw.rounded_rectangle([100, 110, 400, 310], radius=10, fill=(24, 28, 38), outline=(0, 230, 118), width=2)
    # WiFi 7 Golden Antennas
    draw.ellipse([345, 135, 370, 160], fill=(212, 175, 55), outline=(255, 255, 255), width=1)
    draw.ellipse([345, 175, 370, 200], fill=(212, 175, 55), outline=(255, 255, 255), width=1)
    # USB-C Thunderbolt Ports
    draw.rounded_rectangle([130, 215, 155, 260], radius=8, fill=(10, 14, 20), outline=(0, 229, 255), width=2)
    draw.rounded_rectangle([170, 215, 195, 260], radius=8, fill=(10, 14, 20), outline=(0, 229, 255), width=2)
    # 2.5G / 10G RJ45 LAN Ports
    draw.rectangle([215, 210, 255, 265], fill=(35, 40, 50), outline=(200, 200, 200), width=1)
    draw.rectangle([270, 210, 310, 265], fill=(35, 40, 50), outline=(200, 200, 200), width=1)
    # USB 3.2 Gen 2 Type-A Blue Ports
    for x in [130, 170, 215, 260]:
        draw.rectangle([x, 140, x+28, 175], fill=(0, 102, 204), outline=(200, 200, 200), width=1)
    # Audio Jacks
    for i, col in enumerate([(255, 100, 100), (100, 255, 100), (100, 100, 255)]):
        draw.ellipse([335 + i*18, 240, 347 + i*18, 252], fill=col, outline=(255, 255, 255), width=1)
        
    add_studio_badge(draw, "Anakart Entegre Arka Giriş/Çıkış (I/O)", "WiFi 7, Thunderbolt USB4 & 2.5G Yüksek Hızlı Bağlantılar")
    return img

# 12. RAM Top RGB Lightbar
def gen_ram_top_rgb():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # Dual DDR5 modules seen from top
    # Module 1
    draw.rounded_rectangle([80, 150, 420, 195], radius=6, fill=(30, 35, 45), outline=(80, 95, 115), width=2)
    # RGB Diffuser Glow (gradient neon)
    for x in range(95, 405):
        ratio = (x - 95) / 310.0
        r = int(0 * (1-ratio) + 255 * ratio)
        g = int(229 * (1-ratio) + 23 * ratio)
        b = int(255 * (1-ratio) + 68 * ratio)
        draw.line([(x, 158), (x, 187)], fill=(r, g, b, 230), width=1)
        
    # Module 2
    draw.rounded_rectangle([80, 235, 420, 280], radius=6, fill=(30, 35, 45), outline=(80, 95, 115), width=2)
    for x in range(95, 405):
        ratio = (x - 95) / 310.0
        r = int(255 * (1-ratio) + 0 * ratio)
        g = int(214 * (1-ratio) + 230 * ratio)
        b = int(0 * (1-ratio) + 118 * ratio)
        draw.line([(x, 243), (x, 272)], fill=(r, g, b, 230), width=1)
        
    add_studio_badge(draw, "DDR5 RGB Işık Barı Üst Görünüm", "Akrilik Difüzörlü Dinamik Adreslenebilir ARGB Aydınlatma")
    return img

# 13. RAM Brushed Heatsink Angle
def gen_ram_brushed_heatsink():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # Angled DDR5 Memory Stick
    draw.polygon([(70, 210), (370, 110), (430, 210), (130, 310)], fill=(26, 30, 40), outline=(0, 229, 255), width=2)
    # Brushed aluminum texture
    for y in range(130, 290, 8):
        draw.line([(100, y), (400, y-80)], fill=(38, 44, 58), width=2)
    # Gold pins on bottom edge
    for i in range(35):
        px = 140 + i * 8
        py = 300 - i * 2.7
        draw.line([(px, py), (px+4, py-6)], fill=(255, 215, 0), width=2)
    # Geometric heatsink notch
    draw.polygon([(220, 160), (280, 140), (275, 150), (225, 168)], fill=(0, 229, 255))
    add_studio_badge(draw, "DDR5 Fırçalanmış Alüminyum Zırh", "Eloksallı Termal Soğutucu Blok & Düşük Gecikmeli Bellek Modülü")
    return img

# 14. Storage NVMe PCIe Controller Angle
def gen_storage_controller():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # M.2 2280 PCB (Dark matte black PCB)
    draw.rounded_rectangle([90, 140, 410, 280], radius=6, fill=(16, 20, 28), outline=(0, 229, 255), width=2)
    # M.2 M-Key Notch Gold Fingers
    draw.rectangle([90, 160, 110, 260], fill=(212, 175, 55), outline=(180, 140, 40), width=1)
    draw.rectangle([90, 190, 110, 205], fill=(16, 20, 28)) # M Key Notch
    # Controller IC (Phison / Samsung Elpis style nickel-coated)
    draw.rounded_rectangle([150, 165, 230, 255], radius=4, fill=(160, 168, 178), outline=(220, 225, 235), width=2)
    font_ic = get_font(10, bold=True)
    draw.text((190, 210), "PCIe Gen5\nCONTROLLER", font=font_ic, fill=(30, 35, 45), anchor="mm", align="center")
    # DRAM Cache IC
    draw.rounded_rectangle([245, 185, 295, 235], radius=3, fill=(28, 32, 42), outline=(90, 100, 115), width=1)
    # 3D NAND Flash ICs
    draw.rounded_rectangle([310, 165, 395, 215], radius=4, fill=(28, 32, 42), outline=(90, 100, 115), width=1)
    draw.rounded_rectangle([310, 225, 395, 275], radius=4, fill=(28, 32, 42), outline=(90, 100, 115), width=1)
    add_studio_badge(draw, "M.2 NVMe PCIe 5.0 Kontrolcü & PCB", "Ultra Hızlı Kontrolcü Çipi & 3D TLC/QLC NAND Bellek Blokları")
    return img

# 15. Storage NVMe Heatsink Installed
def gen_storage_heatsink():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # M.2 NVMe SSD with Ribbed Aluminum Heatsink
    draw.rounded_rectangle([90, 140, 410, 280], radius=8, fill=(28, 34, 46), outline=(0, 229, 255), width=2)
    # Cooling fins across heatsink
    for y in range(155, 270, 14):
        draw.line([(120, y), (390, y)], fill=(80, 95, 115), width=4)
        draw.line([(120, y-2), (390, y-2)], fill=(180, 195, 210), width=1)
    # Thermal Pad visible on side
    draw.line([(100, 275), (400, 275)], fill=(0, 229, 255), width=3)
    # Screws
    draw.ellipse([385, 200, 400, 215], fill=(160, 170, 180), outline=(220, 220, 220), width=1)
    draw.line([(388, 207), (397, 207)], fill=(40, 40, 40), width=1)
    add_studio_badge(draw, "M.2 Termal Zırh Soğutucu Blok", "Kanatçıklı Alüminyum Isı Dağıtıcı & Termal İletken Ped")
    return img

# 16. PSU Modular Connectors Panel
def gen_psu_modular():
    img, draw = create_base_canvas(accent_color=(255, 214, 0))
    # Full modular back panel
    draw.rounded_rectangle([90, 100, 410, 320], radius=8, fill=(22, 26, 36), outline=(255, 214, 0), width=2)
    # Motherboard 24-Pin Socket (split 10+14)
    draw.rectangle([115, 130, 220, 170], fill=(12, 15, 22), outline=(100, 115, 135), width=2)
    # PCIe 5.1 12V-2x6 16-Pin High Power Socket
    draw.rectangle([250, 130, 385, 170], fill=(12, 15, 22), outline=(255, 23, 68), width=2)
    draw.text((317, 150), "12V-2x6 600W", font=get_font(10, bold=True), fill=(255, 23, 68), anchor="mm")
    # CPU / PCIe 8-Pin Sockets
    for i in range(3):
        x = 115 + i * 55
        draw.rectangle([x, 200, x+45, 245], fill=(12, 15, 22), outline=(0, 229, 255), width=2)
    # Peripheral / SATA Sockets
    for i in range(3):
        x = 115 + i * 55
        draw.rectangle([x, 260, x+45, 300], fill=(12, 15, 22), outline=(0, 230, 118), width=2)
    add_studio_badge(draw, "PSU Modüler Kablo Bağlantı Paneli", "PCIe 5.1 ATX 3.1 12V-2x6 (600W) & EPS Modüler Çıkışlar")
    return img

# 17. PSU Honeycomb Exhaust & Switch
def gen_psu_exhaust():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # PSU Rear Honeycomb Mesh
    draw.rounded_rectangle([90, 100, 410, 320], radius=8, fill=(20, 24, 32), outline=(80, 95, 115), width=2)
    # Honeycomb holes
    for x in range(110, 290, 14):
        for y in range(120, 305, 14):
            draw.ellipse([x, y, x+8, y+8], fill=(8, 10, 15), outline=(50, 60, 75), width=1)
    # AC Power Receptacle C14
    draw.polygon([(310, 140), (370, 140), (385, 175), (385, 200), (295, 200), (295, 175)], fill=(12, 15, 20), outline=(180, 190, 205), width=2)
    # Master Power Rocker Switch
    draw.rectangle([(305, 220), (375, 265)], fill=(12, 15, 20), outline=(180, 190, 205), width=2)
    draw.rectangle([(315, 225), (365, 260)], fill=(255, 23, 68))
    # Hybrid Eco Fan Switch Button
    draw.ellipse([325, 280, 355, 305], fill=(0, 229, 255), outline=(255, 255, 255), width=2)
    add_studio_badge(draw, "PSU Arka Havalandırma Izgarası", "Petek Tipi Egzoz Izgarası, Güç Şalteri & Zero-RPM Hibrit Fan Butonu")
    return img

# 18. Cooler Infinity Mirror Pump
def gen_cooler_pump():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # Circular AIO Pump Block
    draw.ellipse([140, 100, 360, 320], fill=(22, 26, 36), outline=(0, 229, 255), width=4)
    # Concentric Infinity Mirror Rings
    colors = [(0, 229, 255), (41, 121, 255), (255, 23, 68), (0, 230, 118)]
    for idx, r in enumerate([90, 70, 50, 30]):
        col = colors[idx % len(colors)]
        draw.ellipse([250-r, 210-r, 250+r, 210+r], fill=None, outline=col, width=3)
    # Sleeved Tubing entering pump
    draw.line([(320, 140), (420, 90)], fill=(45, 52, 65), width=16)
    draw.line([(340, 170), (440, 120)], fill=(45, 52, 65), width=16)
    add_studio_badge(draw, "360mm Sıvı Soğutma Pompa Bloğu", "Sonsuzluk Aynası (Infinity Mirror) ARGB Pompa Başlığı")
    return img

# 19. Cooler Radiator Fins Side
def gen_cooler_radiator():
    img, draw = create_base_canvas(accent_color=(0, 229, 255))
    # Aluminum Radiator Profile
    draw.rounded_rectangle([90, 120, 410, 300], radius=8, fill=(24, 28, 38), outline=(0, 229, 255), width=2)
    # Dense aluminum micro-fins
    for x in range(110, 390, 4):
        draw.line([(x, 145), (x, 275)], fill=(100, 115, 135), width=1)
    # Water coolant channels
    for y in [175, 210, 245]:
        draw.line([(100, y), (400, y)], fill=(45, 55, 70), width=4)
    # Fan screw mounting rails
    draw.rectangle([95, 130, 405, 140], fill=(150, 165, 180))
    draw.rectangle([95, 280, 405, 290], fill=(150, 165, 180))
    add_studio_badge(draw, "Radyatör Yoğun Alüminyum Kanatçıklar", "Mikro Kanal Sıvı Dağıtım Yolları & 120/140mm Fan Montaj Rayı")
    return img

GENERATORS = {
    "cpu/amd_am5_ihs_studio.webp": gen_amd_am5_ihs,
    "cpu/amd_am4_ihs_studio.webp": gen_amd_am4_ihs,
    "cpu/amd_am5_lga1718_bottom.webp": gen_amd_am5_bottom,
    "cpu/intel_lga1700_ihs_studio.webp": gen_intel_lga1700_ihs,
    "cpu/intel_lga1851_ihs_studio.webp": gen_intel_lga1851_ihs,
    "cpu/intel_lga1700_bottom.webp": gen_intel_lga1700_bottom,
    "cpu/generic_cpu_ihs.webp": gen_generic_cpu_ihs,
    "gpu/gpu_angle_studio_reference.webp": gen_gpu_angle,
    "gpu/gpu_backplate_io_reference.webp": gen_gpu_backplate,
    "motherboard/motherboard_angle_vrm_reference.webp": gen_motherboard_vrm,
    "motherboard/motherboard_rear_io_reference.webp": gen_motherboard_rear_io,
    "ram/ram_top_rgb_lightbar_reference.webp": gen_ram_top_rgb,
    "ram/ram_brushed_heatsink_angle_reference.webp": gen_ram_brushed_heatsink,
    "storage/nvme_pcie_controller_angle_reference.webp": gen_storage_controller,
    "storage/nvme_heatsink_installed_reference.webp": gen_storage_heatsink,
    "psu/psu_modular_connectors_panel_reference.webp": gen_psu_modular,
    "psu/psu_honeycomb_exhaust_switch_reference.webp": gen_psu_exhaust,
    "cooler/cooler_infinity_mirror_pump_reference.webp": gen_cooler_pump,
    "cooler/cooler_radiator_fins_side_reference.webp": gen_cooler_radiator,
}

def main():
    print("🎨 Hardware Wizard - Generating 19 Studio Reference Assets...")
    count = 0
    for rel_path, gen_func in GENERATORS.items():
        out_path = os.path.join(IMAGES_DIR, rel_path.replace("/", os.sep))
        img = gen_func()
        img.save(out_path, format="WEBP", quality=92, method=6)
        print(f"  [OK] Saved -> {rel_path} ({os.path.getsize(out_path)} bytes)")
        count += 1
    print(f"\n✨ Successfully generated all {count} studio reference WebP images!")

if __name__ == "__main__":
    main()

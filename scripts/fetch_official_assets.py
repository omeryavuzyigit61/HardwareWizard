import urllib.request
import os
import io
from PIL import Image

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
IMAGES_DIR = os.path.join(ROOT, "data", "images")

HEADERS = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36",
    "Accept": "image/avif,image/webp,image/apng,image/png,image/*,*/*;q=0.8",
}

OFFICIAL_ASSETS = {
    # 1. NZXT N9 Motherboards (No more Itopya store logo!)
    os.path.join(IMAGES_DIR, "motherboard", "nzxt_n9_x870e_8000mhz_oc_ddr5_soket_am5_wifi7_m_2_rgb_atx_anakart.webp"): 
        "https://nzxt.com/cdn/shop/files/n9-x870e-hero-front-angled-white-1_88becd96-3ce6-4714-85e6-58db4da4abe8.png",
    os.path.join(IMAGES_DIR, "motherboard", "nzxt_n9_z890_8000mhz_oc_ddr5_soket_lga_1851_m_2_wifi7_hdmi_rgb_atx_anakart.webp"): 
        "https://nzxt.com/cdn/shop/files/1744171946-7wgabw.png",
    os.path.join(IMAGES_DIR, "motherboard", "nzxt_n7_b650_matte_black.webp"):
        "https://nzxt.com/cdn/shop/products/N7-B65XT-B1-front-hero.png",
    os.path.join(IMAGES_DIR, "motherboard", "nzxt_n7_b650_matte_white.webp"):
        "https://nzxt.com/cdn/shop/products/N7-B65XT-W1-front-hero.png",
    os.path.join(IMAGES_DIR, "motherboard", "nzxt_n9_x870e_matte_white.webp"):
        "https://nzxt.com/cdn/shop/files/n9-x870e-hero-front-angled-white-1_88becd96-3ce6-4714-85e6-58db4da4abe8.png",
    os.path.join(IMAGES_DIR, "motherboard", "nzxt_n9_z890_matte_white.webp"):
        "https://nzxt.com/cdn/shop/files/1744171946-7wgabw.png",

    # 2. Lian Li Cases (High-Res, Clean Renders, No Naive Flood Fill!)
    os.path.join(IMAGES_DIR, "case", "lian_li_o11_dynamic_evo_xl.webp"):
        "https://lian-li.com/wp-content/uploads/2023/09/O11DEXLX-002.jpg",
    os.path.join(IMAGES_DIR, "case", "lian_li_o11_dynamic_evo_xl_white.webp"):
        "https://lian-li.com/wp-content/uploads/2023/09/O11DEXLW-002.jpg",
    os.path.join(IMAGES_DIR, "case", "lian_li_o11_dynamic_evo_rgb.webp"):
        "https://lian-li.com/wp-content/uploads/2023/12/O11DERGB-000.jpg",
    os.path.join(IMAGES_DIR, "case", "lian_li_o11_dynamic_evo_rgb_white.webp"):
        "https://lian-li.com/wp-content/uploads/2023/12/O11DERGBW-000.jpg",
    os.path.join(IMAGES_DIR, "case", "lian_li_o11_vision.webp"):
        "https://lian-li.com/wp-content/uploads/2023/11/O11VX-001.jpg",
    os.path.join(IMAGES_DIR, "case", "lian_li_o11_vision_white.webp"):
        "https://lian-li.com/wp-content/uploads/2023/11/O11VW-001.jpg",

    # 3. Kioxia Exceria Family (Official Kioxia Product PNGs)
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_pro_nvme_m_2_1tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-pro_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_pro_nvme_m_2_2tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-pro_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_pro_1tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-pro_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_pro_2tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-pro_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_g2_nvme_m_2_1tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-g2_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_g2_nvme_m_2_2tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-g2_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_g2_1tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-g2_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_g2_2tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-g2_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_plus_g3_nvme_m_2_1tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-plus-g3_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_plus_g3_nvme_m_2_2tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-plus-g3_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_nvme_m_2_1tb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_nvme_m_2_500gb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_sata_480gb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-sata_img_prd001.png",
    os.path.join(IMAGES_DIR, "storage", "kioxia_exceria_sata_960gb.webp"):
        "https://europe.kioxia.com/content/dam/kioxia/shared/personal/ssd/img/exceria-sata_img_prd001.png",

    # 4. NZXT Cases (Official NZXT CDN Transparent PNGs)
    os.path.join(IMAGES_DIR, "case", "nzxt_h9_flow.webp"):
        "https://nzxt.com/cdn/shop/products/H9-Flow-Black-Hero.png",
    os.path.join(IMAGES_DIR, "case", "nzxt_h9_flow_white.webp"):
        "https://nzxt.com/cdn/shop/products/H9-Flow-White-Hero.png",
    os.path.join(IMAGES_DIR, "case", "nzxt_h6_flow.webp"):
        "https://nzxt.com/cdn/shop/files/H6-Flow-Black-Hero.png",
    os.path.join(IMAGES_DIR, "case", "nzxt_h6_flow_white.webp"):
        "https://nzxt.com/cdn/shop/files/H6-Flow-White-Hero.png",
    os.path.join(IMAGES_DIR, "case", "nzxt_h5_flow_2024.webp"):
        "https://nzxt.com/cdn/shop/files/H5-Flow-Black-Hero.png",
    os.path.join(IMAGES_DIR, "case", "nzxt_h5_flow_2024_white.webp"):
        "https://nzxt.com/cdn/shop/files/H5-Flow-White-Hero.png",
}

def download_and_save(path, url):
    try:
        os.makedirs(os.path.dirname(path), exist_ok=True)
        req = urllib.request.Request(url, headers=HEADERS)
        with urllib.request.urlopen(req, timeout=15) as resp:
            data = resp.read()
        img = Image.open(io.BytesIO(data))
        img = img.convert("RGBA")
        # Save as high-res WebP
        img.save(path, "WEBP", quality=92, method=6)
        print(f"[OK] Saved {os.path.basename(path)} ({img.size[0]}x{img.size[1]}, {os.path.getsize(path)} bytes)")
        return True
    except Exception as e:
        print(f"[ERR] Failed {os.path.basename(path)} from {url}: {e}")
        return False

success = 0
for save_path, url in OFFICIAL_ASSETS.items():
    if download_and_save(save_path, url):
        success += 1

print(f"Total downloaded and converted: {success}/{len(OFFICIAL_ASSETS)}")

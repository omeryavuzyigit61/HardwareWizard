-- ==============================================================================
-- HARDWARE WIZARD - SUPABASE MERKEZİ DONANIM KATALOĞU & DELTA SYNC ŞEMASI (v5.0)
-- ==============================================================================
-- KURULUM:
-- 1. Supabase Dashboard -> SQL Editor -> "New query" butonuna tıklayın.
-- 2. Bu dosyanın TAMAMINI yapıştırın ve sağ alttaki "Run" butonuna basın.
-- 3. Başarılı ("Success. No rows returned") mesajını görmeniz yeterlidir.
--
-- MİMARİ GARANTİLER:
-- - Tek Merkezi Gerçek Kaynak (Single Source of Truth): public.hardware_catalog
-- - Değişmez Teknik Özellikler: spec_locked = TRUE olan kayıtlar crawler tarafından ezilmez
-- - Gerçek Delta Sync: Mobil uygulama sadece catalog_revision > local_revision satırlarını çeker
-- - Sıfır Güven (Zero-Trust) RLS: Herkes okuyabilir (SELECT), YALNIZCA service_role (GitHub Actions) yazabilir
-- ==============================================================================

-- 1. ANA DONANIM KATALOĞU TABLOSU (28 Kategori Tek Çatı Altında)
CREATE TABLE IF NOT EXISTS public.hardware_catalog (
    id TEXT PRIMARY KEY,
    category TEXT NOT NULL,
    brand TEXT NOT NULL,
    model TEXT NOT NULL,
    canonical_key TEXT NOT NULL,
    spec_table TEXT NOT NULL,
    spec_payload JSONB NOT NULL DEFAULT '{}'::jsonb,
    benchmark_score INTEGER,
    benchmark_tier TEXT,
    benchmark_test_suite TEXT,
    benchmark_gaming_index INTEGER,
    cpu_synthetic JSONB,
    gpu_synthetic JSONB,
    game_fps JSONB,
    image_url TEXT,
    images JSONB NOT NULL DEFAULT '[]'::jsonb,
    extra_specs JSONB NOT NULL DEFAULT '{}'::jsonb,
    source_provenance JSONB NOT NULL DEFAULT '[]'::jsonb,
    spec_verified_at TEXT,
    spec_locked BOOLEAN NOT NULL DEFAULT TRUE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    catalog_revision BIGINT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_hardware_catalog_cat_canonical UNIQUE (category, canonical_key)
);

CREATE INDEX IF NOT EXISTS idx_hw_catalog_revision
    ON public.hardware_catalog (catalog_revision)
    WHERE is_active = TRUE;

CREATE INDEX IF NOT EXISTS idx_hw_catalog_category
    ON public.hardware_catalog (category);

CREATE INDEX IF NOT EXISTS idx_hw_catalog_brand
    ON public.hardware_catalog (brand);

-- 2. KATALOG META TABLOSU (Mobil Uygulamanın 200 Byte'lık Hızlı Sürüm Kontrolü)
CREATE TABLE IF NOT EXISTS public.catalog_meta (
    id SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    latest_revision BIGINT NOT NULL DEFAULT 0,
    total_items INTEGER NOT NULL DEFAULT 0,
    last_delta_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    last_checked_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    schema_version TEXT NOT NULL DEFAULT '5.0.0'
);

INSERT INTO public.catalog_meta (id, latest_revision, total_items, schema_version)
VALUES (1, 0, 0, '5.0.0')
ON CONFLICT (id) DO NOTHING;

-- 3. İZOLE FİYATLANDIRMA TABLOSU (İleride IS_PRICING_ENABLED = true olduğunda kullanılır)
-- Teknik özellik tablosunu (hardware_catalog) asla kirletmeden sadece fiyat değişimlerini taşır.
CREATE TABLE IF NOT EXISTS public.hardware_prices (
    hardware_id TEXT PRIMARY KEY REFERENCES public.hardware_catalog(id) ON DELETE CASCADE,
    current_price_try NUMERIC(12, 2),
    lowest_price_try NUMERIC(12, 2),
    highest_price_try NUMERIC(12, 2),
    offers_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    price_history_json JSONB NOT NULL DEFAULT '[]'::jsonb,
    price_revision BIGINT NOT NULL DEFAULT 1,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_hw_prices_revision
    ON public.hardware_prices (price_revision);

-- 4. OTONOM DELTA CRAWLER DENETİM LOGU (Her Günlük Çalışmanın Özeti)
CREATE TABLE IF NOT EXISTS public.crawler_runs (
    id BIGSERIAL PRIMARY KEY,
    run_mode TEXT NOT NULL DEFAULT 'delta',
    new_items_added INTEGER NOT NULL DEFAULT 0,
    sources_checked INTEGER NOT NULL DEFAULT 0,
    catalog_revision BIGINT NOT NULL DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'OK',
    notes TEXT,
    ran_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- 5. ATOMİK REVİZYON & META GÜNCELLEME FONKSİYONU (Sadece service_role çağırabilir)
CREATE OR REPLACE FUNCTION public.refresh_catalog_meta(
    p_new_revision BIGINT DEFAULT NULL
)
RETURNS TABLE(latest_revision BIGINT, total_items INTEGER)
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_total INTEGER;
    v_rev BIGINT;
BEGIN
    SELECT COUNT(*)::INTEGER INTO v_total
    FROM public.hardware_catalog
    WHERE is_active = TRUE;

    SELECT COALESCE(p_new_revision, GREATEST(COALESCE(MAX(catalog_revision), 0), 0))
    INTO v_rev
    FROM public.hardware_catalog;

    INSERT INTO public.catalog_meta (id, latest_revision, total_items, last_delta_at, last_checked_at, schema_version)
    VALUES (1, v_rev, v_total, NOW(), NOW(), '5.0.0')
    ON CONFLICT (id) DO UPDATE SET
        latest_revision = GREATEST(public.catalog_meta.latest_revision, EXCLUDED.latest_revision),
        total_items = EXCLUDED.total_items,
        last_delta_at = CASE
            WHEN EXCLUDED.latest_revision > public.catalog_meta.latest_revision THEN NOW()
            ELSE public.catalog_meta.last_delta_at
        END,
        last_checked_at = NOW();

    RETURN QUERY
    SELECT m.latest_revision, m.total_items
    FROM public.catalog_meta m
    WHERE m.id = 1;
END;
$$;

REVOKE ALL ON FUNCTION public.refresh_catalog_meta(BIGINT) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.refresh_catalog_meta(BIGINT) FROM anon;
REVOKE ALL ON FUNCTION public.refresh_catalog_meta(BIGINT) FROM authenticated;
GRANT EXECUTE ON FUNCTION public.refresh_catalog_meta(BIGINT) TO service_role;

-- 6. SIKI ROW LEVEL SECURITY (RLS) POLİTİKALARI
-- Katalog tabloları herkese SADECE OKUMA (SELECT) açıktır.
-- INSERT / UPDATE / DELETE politikası tanımlanmadığından anon ve authenticated roller
-- hiçbir ürünü değiştiremez veya silemez; yalnızca GitHub Actions (service_role) yazabilir.
ALTER TABLE public.hardware_catalog ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.catalog_meta ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.hardware_prices ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.crawler_runs ENABLE ROW LEVEL SECURITY;

DROP POLICY IF EXISTS "Public read active hardware catalog" ON public.hardware_catalog;
CREATE POLICY "Public read active hardware catalog"
    ON public.hardware_catalog
    FOR SELECT
    USING (is_active = TRUE);

DROP POLICY IF EXISTS "Public read catalog meta" ON public.catalog_meta;
CREATE POLICY "Public read catalog meta"
    ON public.catalog_meta
    FOR SELECT
    USING (TRUE);

DROP POLICY IF EXISTS "Public read hardware prices" ON public.hardware_prices;
CREATE POLICY "Public read hardware prices"
    ON public.hardware_prices
    FOR SELECT
    USING (TRUE);

-- crawler_runs sadece yönetici / service_role tarafından okunur ve yazılır (anon erişimi kapalı)
DROP POLICY IF EXISTS "Public read crawler runs" ON public.crawler_runs;

-- ==============================================================================
-- HARDWARE WIZARD - TOPLULUK EKOSİSTEMİ & TUNING VAULT SUPABASE ŞEMASI
-- ==============================================================================
-- Bu betiği Supabase Dashboard -> SQL Editor alanına yapıştırıp "RUN" butonuna basınız.
-- 
-- KAPSAM:
-- 1. user_profiles genişletmesi (İtibar puanı, rozet ve sayaçlar)
-- 2. tuning_profiles (OC / UV ayarları, kanıt görseli, moderasyon)
-- 3. tuning_comments & tuning_reactions (Yorumlar ve 5 farklı reaksiyon)
-- 4. hardware_pro_tips & pro_tip_upvotes (Donanım detay mikro ipuçları ve oylama)
-- 5. price_drop_alerts (Yalnızca perakende/kampanya fiyat alarmları)
-- ==============================================================================

-- 1. KULLANICI PROFİLİ GENİŞLETMESİ
ALTER TABLE IF EXISTS public.user_profiles 
ADD COLUMN IF NOT EXISTS reputation_score INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_deals_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_tunings_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS total_tips_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS badge_title TEXT DEFAULT 'Lehim Çırağı';

-- 2. TOPLULUK OC / UV PROFİLLERİ (TUNING VAULT)
CREATE TABLE IF NOT EXISTS public.tuning_profiles (
    id TEXT PRIMARY KEY,
    hardware_id TEXT NOT NULL,
    hardware_name TEXT NOT NULL,
    hardware_category TEXT NOT NULL, -- CPU, GPU
    tuning_type TEXT NOT NULL,       -- UNDERVOLT, OVERCLOCK, PBO, MEMORY
    profile_title TEXT NOT NULL,
    language_code TEXT NOT NULL DEFAULT 'tr', -- Zorunlu dil seçimi
    voltage_offset_mv NUMERIC,
    clock_mhz NUMERIC,
    power_limit_watt NUMERIC,
    temp_before_c NUMERIC,
    temp_after_c NUMERIC,
    score_before NUMERIC,
    score_after NUMERIC,
    benchmark_name TEXT,             -- Cinebench R23, 3DMark Time Spy vb.
    proof_image_url TEXT,            -- Supabase Storage 100-150KB WebP kanıt linki
    guide_notes TEXT,
    user_id TEXT NOT NULL,
    user_display_name TEXT NOT NULL,
    user_badge TEXT DEFAULT 'Lehim Çırağı',
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    rejection_reason TEXT,
    reaction_hot_count INTEGER DEFAULT 0,
    reaction_cold_count INTEGER DEFAULT 0,
    reaction_efficient_count INTEGER DEFAULT 0,
    reaction_stable_count INTEGER DEFAULT 0,
    reaction_unstable_count INTEGER DEFAULT 0,
    comments_count INTEGER DEFAULT 0,
    created_at_epoch_ms BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tuning_hardware ON public.tuning_profiles(hardware_id);
CREATE INDEX IF NOT EXISTS idx_tuning_status ON public.tuning_profiles(status);
CREATE INDEX IF NOT EXISTS idx_tuning_user ON public.tuning_profiles(user_id);

-- 3. OC/UV YORUMLARI
CREATE TABLE IF NOT EXISTS public.tuning_comments (
    id TEXT PRIMARY KEY,
    tuning_id TEXT NOT NULL REFERENCES public.tuning_profiles(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    user_display_name TEXT NOT NULL,
    user_badge TEXT DEFAULT 'Lehim Çırağı',
    user_reputation INTEGER DEFAULT 0,
    comment_text TEXT NOT NULL,
    created_at_epoch_ms BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_comments_tuning ON public.tuning_comments(tuning_id);

-- 4. OC/UV REAKSİYONLARI (Tekil oy kontrolü)
CREATE TABLE IF NOT EXISTS public.tuning_reactions (
    id TEXT PRIMARY KEY, -- ör: tuning_123_usr_456_HOT
    tuning_id TEXT NOT NULL REFERENCES public.tuning_profiles(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    reaction_type TEXT NOT NULL CHECK (reaction_type IN ('HOT', 'COLD', 'EFFICIENT', 'STABLE', 'UNSTABLE')),
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_reactions_tuning ON public.tuning_reactions(tuning_id);

-- 5. DONANIM MİKRO İPUÇLARI (PRO-TIPS)
CREATE TABLE IF NOT EXISTS public.hardware_pro_tips (
    id TEXT PRIMARY KEY,
    hardware_id TEXT NOT NULL,
    hardware_name TEXT NOT NULL,
    user_id TEXT NOT NULL,
    user_display_name TEXT NOT NULL,
    user_badge TEXT DEFAULT 'Lehim Çırağı',
    tip_text TEXT NOT NULL,          -- Max 200 karakter pratik hap bilgi
    upvotes_count INTEGER DEFAULT 0,
    status TEXT NOT NULL DEFAULT 'APPROVED' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    created_at_epoch_ms BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_tips_hardware ON public.hardware_pro_tips(hardware_id);

-- 6. MİKRO İPUCU OYLAMALARI (Tekil oy)
CREATE TABLE IF NOT EXISTS public.pro_tip_upvotes (
    id TEXT PRIMARY KEY, -- tip_123_usr_456
    tip_id TEXT NOT NULL REFERENCES public.hardware_pro_tips(id) ON DELETE CASCADE,
    user_id TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

-- 7. HEDEF FİYAT ALARMLARI (Yalnızca Sıfır/Perakende Mağaza Alımı)
CREATE TABLE IF NOT EXISTS public.price_drop_alerts (
    id TEXT PRIMARY KEY,
    user_id TEXT NOT NULL,
    hardware_id TEXT NOT NULL,
    hardware_name TEXT NOT NULL,
    target_price_try NUMERIC(12, 2) NOT NULL CHECK (target_price_try > 0),
    only_retail_campaign BOOLEAN DEFAULT TRUE,
    is_triggered BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMPTZ DEFAULT NOW() NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alerts_user ON public.price_drop_alerts(user_id);
CREATE INDEX IF NOT EXISTS idx_alerts_hardware ON public.price_drop_alerts(hardware_id);

-- 8. ROW LEVEL SECURITY (RLS) POLİTİKALARI
ALTER TABLE public.tuning_profiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tuning_comments ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.tuning_reactions ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.hardware_pro_tips ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pro_tip_upvotes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.price_drop_alerts ENABLE ROW LEVEL SECURITY;

-- Herkes onaylanmış içerikleri okuyabilir
CREATE POLICY "Tuning profilleri okuma" ON public.tuning_profiles FOR SELECT USING (true);
CREATE POLICY "Tuning profilleri ekleme" ON public.tuning_profiles FOR INSERT WITH CHECK (true);
CREATE POLICY "Tuning profilleri guncelleme" ON public.tuning_profiles FOR UPDATE USING (true);

CREATE POLICY "Tuning yorumlari okuma" ON public.tuning_comments FOR SELECT USING (true);
CREATE POLICY "Tuning yorumlari ekleme" ON public.tuning_comments FOR INSERT WITH CHECK (true);

CREATE POLICY "Tuning reaksiyon okuma" ON public.tuning_reactions FOR SELECT USING (true);
CREATE POLICY "Tuning reaksiyon ekleme" ON public.tuning_reactions FOR INSERT WITH CHECK (true);
CREATE POLICY "Tuning reaksiyon silme" ON public.tuning_reactions FOR DELETE USING (true);

CREATE POLICY "Mikro ipuclari okuma" ON public.hardware_pro_tips FOR SELECT USING (true);
CREATE POLICY "Mikro ipuclari ekleme" ON public.hardware_pro_tips FOR INSERT WITH CHECK (true);
CREATE POLICY "Mikro ipuclari guncelleme" ON public.hardware_pro_tips FOR UPDATE USING (true);

CREATE POLICY "Ipucu oylama okuma" ON public.pro_tip_upvotes FOR SELECT USING (true);
CREATE POLICY "Ipucu oylama ekleme" ON public.pro_tip_upvotes FOR INSERT WITH CHECK (true);
CREATE POLICY "Ipucu oylama silme" ON public.pro_tip_upvotes FOR DELETE USING (true);

CREATE POLICY "Fiyat alarmlari okuma" ON public.price_drop_alerts FOR SELECT USING (true);
CREATE POLICY "Fiyat alarmlari ekleme" ON public.price_drop_alerts FOR INSERT WITH CHECK (true);
CREATE POLICY "Fiyat alarmlari silme" ON public.price_drop_alerts FOR DELETE USING (true);

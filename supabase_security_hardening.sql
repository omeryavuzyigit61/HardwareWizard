-- ==============================================================================
-- HARDWARE WIZARD - CATALOG CLEANUP & COMMUNITY RLS SECURITY HARDENING (v5.0.1)
-- ==============================================================================
-- 1. Removes retail-only GamingGen SEO records (rev >= 2) lacking structured specs
--    while preserving all verified PassMark / pc-part-dataset / catalog items.
-- 2. Recomputes catalog_meta (total_items & latest_revision) and logs the cleanup.
-- 3. Hardens legacy community RLS policies & adds anon privilege-escalation guards
--    so anonymous REST calls cannot self-approve deals/tunings, edit others' tips,
--    or tamper with user reputation / ban status / password_hash.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PART 1: Remove GamingGen Store API SEO-only rows from rev >= 2
-- ------------------------------------------------------------------------------
DELETE FROM public.hardware_catalog
WHERE catalog_revision >= 2
  AND source_provenance @> '["GamingGen Store API"]'::jsonb;

UPDATE public.catalog_meta
SET
    total_items = (SELECT COUNT(*)::INTEGER FROM public.hardware_catalog WHERE is_active = TRUE),
    latest_revision = GREATEST(
        1,
        COALESCE((SELECT MAX(catalog_revision) FROM public.hardware_catalog WHERE is_active = TRUE), 1)
    ),
    last_checked_at = NOW()
WHERE id = 1;

INSERT INTO public.crawler_runs (run_mode, new_items_added, sources_checked, catalog_revision, status, notes)
SELECT
    'cleanup',
    0,
    0,
    latest_revision,
    'OK',
    'Removed GamingGen SEO-only records without structured specs; hardened community RLS policies.'
FROM public.catalog_meta
WHERE id = 1;

-- ------------------------------------------------------------------------------
-- PART 2: Harden community_deals RLS (block anonymous moderation / tampering)
-- ------------------------------------------------------------------------------
DO $$
BEGIN
    IF to_regclass('public.community_deals') IS NOT NULL THEN
        ALTER TABLE public.community_deals ENABLE ROW LEVEL SECURITY;

        DROP POLICY IF EXISTS "Yonetici kayitlari guncelleyebilir" ON public.community_deals;
        DROP POLICY IF EXISTS "Kullanicilar yeni fiyat bildirimi yapabilir" ON public.community_deals;

        CREATE POLICY "Kullanicilar yeni fiyat bildirimi yapabilir"
            ON public.community_deals
            FOR INSERT
            WITH CHECK (status = 'PENDING');
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 3: Harden tuning_profiles RLS & block anonymous status/content tampering
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.guard_tuning_profiles_anon_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF current_user IN ('anon', 'authenticated') THEN
        IF NEW.status IS DISTINCT FROM OLD.status
            OR NEW.user_id IS DISTINCT FROM OLD.user_id
            OR NEW.hardware_id IS DISTINCT FROM OLD.hardware_id
            OR NEW.profile_title IS DISTINCT FROM OLD.profile_title
            OR NEW.guide_notes IS DISTINCT FROM OLD.guide_notes
            OR NEW.proof_image_url IS DISTINCT FROM OLD.proof_image_url
            OR NEW.rejection_reason IS DISTINCT FROM OLD.rejection_reason THEN
            RAISE EXCEPTION 'Unauthorized modification of protected tuning_profiles fields';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

DO $$
BEGIN
    IF to_regclass('public.tuning_profiles') IS NOT NULL THEN
        ALTER TABLE public.tuning_profiles ENABLE ROW LEVEL SECURITY;

        DROP POLICY IF EXISTS "Tuning profilleri ekleme" ON public.tuning_profiles;
        CREATE POLICY "Tuning profilleri ekleme"
            ON public.tuning_profiles
            FOR INSERT
            WITH CHECK (status = 'PENDING');

        DROP TRIGGER IF EXISTS trg_guard_tuning_profiles_anon ON public.tuning_profiles;
        CREATE TRIGGER trg_guard_tuning_profiles_anon
            BEFORE UPDATE ON public.tuning_profiles
            FOR EACH ROW
            EXECUTE FUNCTION public.guard_tuning_profiles_anon_update();
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 4: Harden hardware_pro_tips (block anonymous content/status tampering)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.guard_pro_tips_anon_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF current_user IN ('anon', 'authenticated') THEN
        IF NEW.tip_text IS DISTINCT FROM OLD.tip_text
            OR NEW.user_id IS DISTINCT FROM OLD.user_id
            OR NEW.hardware_id IS DISTINCT FROM OLD.hardware_id
            OR NEW.status IS DISTINCT FROM OLD.status THEN
            RAISE EXCEPTION 'Unauthorized modification of protected hardware_pro_tips fields';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

DO $$
BEGIN
    IF to_regclass('public.hardware_pro_tips') IS NOT NULL THEN
        ALTER TABLE public.hardware_pro_tips ENABLE ROW LEVEL SECURITY;

        DROP TRIGGER IF EXISTS trg_guard_pro_tips_anon ON public.hardware_pro_tips;
        CREATE TRIGGER trg_guard_pro_tips_anon
            BEFORE UPDATE ON public.hardware_pro_tips
            FOR EACH ROW
            EXECUTE FUNCTION public.guard_pro_tips_anon_update();
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 5: Harden user_profiles (block privilege / reputation / ban / hash tampering)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.guard_user_profiles_anon_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF current_user IN ('anon', 'authenticated') THEN
        IF NEW.password_hash IS DISTINCT FROM OLD.password_hash
            OR NEW.reputation_score IS DISTINCT FROM OLD.reputation_score
            OR NEW.total_deals_count IS DISTINCT FROM OLD.total_deals_count
            OR NEW.total_tunings_count IS DISTINCT FROM OLD.total_tunings_count
            OR NEW.total_tips_count IS DISTINCT FROM OLD.total_tips_count
            OR NEW.badge_title IS DISTINCT FROM OLD.badge_title
            OR NEW.warning_count IS DISTINCT FROM OLD.warning_count
            OR NEW.is_banned IS DISTINCT FROM OLD.is_banned
            OR NEW.banned_until_epoch_ms IS DISTINCT FROM OLD.banned_until_epoch_ms
            OR NEW.ban_reason IS DISTINCT FROM OLD.ban_reason
            OR NEW.ban_escalation_tier IS DISTINCT FROM OLD.ban_escalation_tier THEN
            RAISE EXCEPTION 'Unauthorized modification of protected user_profiles fields';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

DO $$
BEGIN
    IF to_regclass('public.user_profiles') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_guard_user_profiles_anon ON public.user_profiles;
        CREATE TRIGGER trg_guard_user_profiles_anon
            BEFORE UPDATE ON public.user_profiles
            FOR EACH ROW
            EXECUTE FUNCTION public.guard_user_profiles_anon_update();
    END IF;
END $$;

-- Verification output
SELECT id, latest_revision, total_items, last_delta_at, last_checked_at
FROM public.catalog_meta
WHERE id = 1;

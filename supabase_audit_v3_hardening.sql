-- ==============================================================================
-- HARDWARE WIZARD - ROUND 3 SCHEMA, REPUTATION TRIGGERS & CATALOG CLEANUP (v5.0.3)
-- ==============================================================================
-- 1. Adds missing `rating` column to public.hardware_pro_tips (fixes HTTP 400 on
--    every user product review submission) + adds DELETE RLS policies for tips/showcases.
-- 2. Adds tamper-proof SECURITY DEFINER PostgreSQL triggers to atomically update
--    user_profiles.reputation_score, contribution counters, and badge_title on
--    tuning profiles, comments, reactions, pro tips, and upvotes.
-- 3. Enables delta-sync tombstones (`is_active = FALSE` readable by delta clients),
--    deactivates 108 polluted laptop_gaminggen_* rows, 2 legacy 2018 Quadro GPUs,
--    and 7 duplicate hardware rows; normalizes GeForce/Radeon/Laptop bogus brands;
--    fills official height_mm on 14 AIR coolers; bumps catalog_meta.latest_revision.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PART 1: Fix hardware_pro_tips schema (`rating` column) & DELETE policies
-- ------------------------------------------------------------------------------
ALTER TABLE IF EXISTS public.hardware_pro_tips
    ADD COLUMN IF NOT EXISTS rating INTEGER NOT NULL DEFAULT 5 CHECK (rating BETWEEN 1 AND 5);

DO $$
BEGIN
    IF to_regclass('public.hardware_pro_tips') IS NOT NULL THEN
        DROP POLICY IF EXISTS "Mikro ipuclari silme" ON public.hardware_pro_tips;
        CREATE POLICY "Mikro ipuclari silme"
            ON public.hardware_pro_tips
            FOR DELETE
            USING (TRUE);
    END IF;

    IF to_regclass('public.community_rig_showcases') IS NOT NULL THEN
        DROP POLICY IF EXISTS "Vitrin silme" ON public.community_rig_showcases;
        CREATE POLICY "Vitrin silme"
            ON public.community_rig_showcases
            FOR DELETE
            USING (TRUE);
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 2: Server-side tamper-proof reputation & badge triggers (SECURITY DEFINER)
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.apply_user_reputation_delta(
    p_user_id TEXT,
    p_points_delta INTEGER DEFAULT 0,
    p_deals_delta INTEGER DEFAULT 0,
    p_tunings_delta INTEGER DEFAULT 0,
    p_tips_delta INTEGER DEFAULT 0
)
RETURNS VOID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_new_score INTEGER;
    v_new_badge TEXT;
BEGIN
    IF p_user_id IS NULL OR LENGTH(TRIM(p_user_id)) = 0 OR p_user_id LIKE 'guest%' OR p_user_id = 'anon' THEN
        RETURN;
    END IF;

    UPDATE public.user_profiles
    SET
        reputation_score = GREATEST(0, COALESCE(reputation_score, 0) + p_points_delta),
        total_deals_count = GREATEST(0, COALESCE(total_deals_count, 0) + p_deals_delta),
        total_tunings_count = GREATEST(0, COALESCE(total_tunings_count, 0) + p_tunings_delta),
        total_tips_count = GREATEST(0, COALESCE(total_tips_count, 0) + p_tips_delta)
    WHERE id = p_user_id
    RETURNING reputation_score INTO v_new_score;

    IF FOUND THEN
        v_new_badge := CASE
            WHEN v_new_score >= 500 THEN 'Silikon Efsanesi'
            WHEN v_new_score >= 250 THEN 'Overclock Ustası'
            WHEN v_new_score >= 100 THEN 'Donanım Gurusu'
            WHEN v_new_score >= 40 THEN 'Silikon Meraklısı'
            ELSE 'Lehim Çırağı'
        END;

        UPDATE public.user_profiles
        SET badge_title = v_new_badge
        WHERE id = p_user_id;
    END IF;
END;
$$;

REVOKE ALL ON FUNCTION public.apply_user_reputation_delta(TEXT, INTEGER, INTEGER, INTEGER, INTEGER) FROM PUBLIC, anon, authenticated;

CREATE OR REPLACE FUNCTION public.trg_fn_rep_tuning_profile()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM public.apply_user_reputation_delta(NEW.user_id, 15, 0, 1, 0);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM public.apply_user_reputation_delta(OLD.user_id, -15, 0, -1, 0);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE OR REPLACE FUNCTION public.trg_fn_rep_tuning_comment()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM public.apply_user_reputation_delta(NEW.user_id, 2, 0, 0, 0);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM public.apply_user_reputation_delta(OLD.user_id, -2, 0, 0, 0);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE OR REPLACE FUNCTION public.trg_fn_rep_tuning_reaction()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_author TEXT;
BEGIN
    IF TG_OP = 'INSERT' THEN
        SELECT user_id INTO v_author FROM public.tuning_profiles WHERE id = NEW.tuning_id;
        IF v_author IS NOT NULL AND v_author <> NEW.user_id THEN
            PERFORM public.apply_user_reputation_delta(v_author, 1, 0, 0, 0);
        END IF;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        SELECT user_id INTO v_author FROM public.tuning_profiles WHERE id = OLD.tuning_id;
        IF v_author IS NOT NULL AND v_author <> OLD.user_id THEN
            PERFORM public.apply_user_reputation_delta(v_author, -1, 0, 0, 0);
        END IF;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE OR REPLACE FUNCTION public.trg_fn_rep_pro_tip()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        PERFORM public.apply_user_reputation_delta(NEW.user_id, 10, 0, 0, 1);
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        PERFORM public.apply_user_reputation_delta(OLD.user_id, -10, 0, 0, -1);
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

CREATE OR REPLACE FUNCTION public.trg_fn_rep_tip_upvote()
RETURNS TRIGGER LANGUAGE plpgsql SECURITY DEFINER SET search_path = public AS $$
DECLARE
    v_author TEXT;
BEGIN
    IF TG_OP = 'INSERT' THEN
        SELECT user_id INTO v_author FROM public.hardware_pro_tips WHERE id = NEW.tip_id;
        IF v_author IS NOT NULL AND v_author <> NEW.user_id THEN
            PERFORM public.apply_user_reputation_delta(v_author, 1, 0, 0, 0);
        END IF;
        RETURN NEW;
    ELSIF TG_OP = 'DELETE' THEN
        SELECT user_id INTO v_author FROM public.hardware_pro_tips WHERE id = OLD.tip_id;
        IF v_author IS NOT NULL AND v_author <> OLD.user_id THEN
            PERFORM public.apply_user_reputation_delta(v_author, -1, 0, 0, 0);
        END IF;
        RETURN OLD;
    END IF;
    RETURN NULL;
END;
$$;

DO $$
BEGIN
    IF to_regclass('public.tuning_profiles') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_rep_tuning_profile ON public.tuning_profiles;
        CREATE TRIGGER trg_rep_tuning_profile AFTER INSERT OR DELETE ON public.tuning_profiles
            FOR EACH ROW EXECUTE FUNCTION public.trg_fn_rep_tuning_profile();
    END IF;
    IF to_regclass('public.tuning_comments') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_rep_tuning_comment ON public.tuning_comments;
        CREATE TRIGGER trg_rep_tuning_comment AFTER INSERT OR DELETE ON public.tuning_comments
            FOR EACH ROW EXECUTE FUNCTION public.trg_fn_rep_tuning_comment();
    END IF;
    IF to_regclass('public.tuning_reactions') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_rep_tuning_reaction ON public.tuning_reactions;
        CREATE TRIGGER trg_rep_tuning_reaction AFTER INSERT OR DELETE ON public.tuning_reactions
            FOR EACH ROW EXECUTE FUNCTION public.trg_fn_rep_tuning_reaction();
    END IF;
    IF to_regclass('public.hardware_pro_tips') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_rep_pro_tip ON public.hardware_pro_tips;
        CREATE TRIGGER trg_rep_pro_tip AFTER INSERT OR DELETE ON public.hardware_pro_tips
            FOR EACH ROW EXECUTE FUNCTION public.trg_fn_rep_pro_tip();
    END IF;
    IF to_regclass('public.pro_tip_upvotes') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_rep_tip_upvote ON public.pro_tip_upvotes;
        CREATE TRIGGER trg_rep_tip_upvote AFTER INSERT OR DELETE ON public.pro_tip_upvotes
            FOR EACH ROW EXECUTE FUNCTION public.trg_fn_rep_tip_upvote();
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 3: Delta-sync tombstone policy + clean polluted/duplicate rows & air cooler heights
-- ------------------------------------------------------------------------------
DROP POLICY IF EXISTS "Public read active hardware catalog" ON public.hardware_catalog;
CREATE POLICY "Public read active hardware catalog"
    ON public.hardware_catalog
    FOR SELECT
    USING (TRUE);

DO $$
DECLARE
    v_new_rev BIGINT;
    v_deactivated INTEGER := 0;
BEGIN
    UPDATE public.catalog_meta
    SET
        latest_revision = latest_revision + 1,
        last_delta_at = NOW(),
        last_checked_at = NOW()
    WHERE id = 1
    RETURNING latest_revision INTO v_new_rev;

    -- 3a. Tombstone 108 polluted TR GamingGen laptops, 2 legacy 2018 Quadro GPUs, and 7 duplicate rows
    UPDATE public.hardware_catalog
    SET
        is_active = FALSE,
        catalog_revision = v_new_rev,
        updated_at = NOW()
    WHERE is_active = TRUE
      AND (
          id LIKE 'laptop_gaminggen_%'
          OR id IN (
              'gpu_pny_vcqrtx5000_pb',
              'gpu_pny_vcqrtx6000_pb',
              'gpu_amd_100_300000069',
              'gpu_asrock_taichi_oc',
              'gpu_gigabyte_aero_oc',
              'gpu_msi_gaming_x_slim',
              'gpu_xfx_speedster_merc_310',
              'phone-samsung-galaxy-m14-128gb',
              'phone-xiaomi-redmi-note-13-128gb'
          )
      );
    GET DIAGNOSTICS v_deactivated = ROW_COUNT;

    -- 3b. Fix bogus GPU brands ("GeForce" -> "NVIDIA", "Radeon" -> "AMD") on rev=2 rows
    UPDATE public.hardware_catalog
    SET
        brand = 'NVIDIA',
        model = CASE WHEN model ILIKE 'GeForce %' THEN model ELSE 'GeForce ' || model END,
        catalog_revision = v_new_rev,
        updated_at = NOW()
    WHERE category = 'GPU' AND brand = 'GeForce' AND is_active = TRUE;

    UPDATE public.hardware_catalog
    SET
        brand = 'AMD',
        model = CASE WHEN model ILIKE 'Radeon %' THEN model ELSE 'Radeon ' || model END,
        catalog_revision = v_new_rev,
        updated_at = NOW()
    WHERE category = 'GPU' AND brand = 'Radeon' AND is_active = TRUE;

    -- 3c. Fix bogus brand = 'Laptop' on remaining active laptop rows
    UPDATE public.hardware_catalog
    SET
        brand = CASE
            WHEN model ILIKE 'Casper%' THEN 'Casper'
            WHEN model ILIKE 'Game Garaj%' OR model ILIKE 'GameGaraj%' THEN 'GameGaraj'
            WHEN model ILIKE 'GameRaider%' THEN 'GameRaider'
            WHEN model ILIKE 'Thunderobot%' THEN 'Thunderobot'
            ELSE brand
        END,
        catalog_revision = v_new_rev,
        updated_at = NOW()
    WHERE category = 'LAPTOP' AND brand = 'Laptop' AND is_active = TRUE;

    -- 3d. Populate official manufacturer height_mm on the 14 AIR coolers that had NULL height_mm
    UPDATE public.hardware_catalog AS h
    SET
        spec_payload = jsonb_set(h.spec_payload, '{height_mm}', to_jsonb(v.height_mm)),
        catalog_revision = v_new_rev,
        updated_at = NOW()
    FROM (
        VALUES
            ('cooler_arctic_freezer_36', 159),
            ('cooler_cooler_master_hyper_212_2023', 154),
            ('cooler_cooler_master_hyper_212_black_edition', 159),
            ('cooler_cooler_master_hyper_212_spectrum_v3', 152),
            ('cooler_deepcool_ak400', 155),
            ('cooler_deepcool_ak400_digital', 156),
            ('cooler_deepcool_ak620', 160),
            ('cooler_deepcool_ak620_digital', 162),
            ('cooler_noctua_nh_d15_chromax_black', 165),
            ('cooler_thermalright_assassin_x_120_r_digital_argb', 151),
            ('cooler_thermalright_assassin_x_120_refined_se', 148),
            ('cooler_thermalright_peerless_assassin_120_se', 155),
            ('cooler_thermalright_peerless_assassin_120_se_argb', 155),
            ('cooler_thermalright_phantom_spirit_120_se', 154)
    ) AS v(item_id, height_mm)
    WHERE h.id = v.item_id
      AND h.is_active = TRUE;

    -- Recompute authoritative active total_items in catalog_meta
    UPDATE public.catalog_meta
    SET
        total_items = (SELECT COUNT(*)::INTEGER FROM public.hardware_catalog WHERE is_active = TRUE),
        last_checked_at = NOW()
    WHERE id = 1;

    INSERT INTO public.crawler_runs (run_mode, new_items_added, sources_checked, catalog_revision, status, notes)
    VALUES (
        'audit_v3_cleanup',
        0,
        0,
        v_new_rev,
        'OK',
        'Round-3 cleanup: deactivated ' || v_deactivated || ' polluted/legacy/duplicate rows; fixed GPU/Laptop brands & 14 AIR cooler heights; added pro_tips.rating & reputation triggers.'
    );
END $$;

NOTIFY pgrst, 'reload schema';

-- Verification summary
SELECT
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE) AS active_catalog_items,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE AND id LIKE 'laptop_gaminggen_%') AS remaining_gaminggen_laptops,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE AND brand IN ('GeForce', 'Radeon', 'Laptop')) AS remaining_bogus_brands,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE AND category = 'COOLER' AND spec_payload->>'type' = 'AIR' AND COALESCE((spec_payload->>'height_mm')::INT, 0) <= 0) AS remaining_null_air_coolers,
    (SELECT latest_revision FROM public.catalog_meta WHERE id = 1) AS latest_catalog_revision,
    (SELECT column_name FROM information_schema.columns WHERE table_schema = 'public' AND table_name = 'hardware_pro_tips' AND column_name = 'rating') AS pro_tips_rating_col;

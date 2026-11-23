-- ==============================================================================
-- HARDWARE WIZARD - ROUND 4 SECURITY HARDENING & GLOBAL LAPTOP CLEANUP (v5.0.4)
-- ==============================================================================
-- 1. Revokes open DELETE privileges & drops open `FOR DELETE USING (TRUE)` RLS
--    policies on `public.hardware_pro_tips` and `public.community_rig_showcases`.
--    Replaces them with ownership-verified SECURITY DEFINER RPCs:
--      - public.delete_own_pro_tip(p_tip_id TEXT, p_user_id TEXT) RETURNS BOOLEAN
--      - public.delete_own_rig_showcase(p_showcase_id TEXT, p_user_id TEXT) RETURNS BOOLEAN
-- 2. Adds BEFORE UPDATE guard trigger `trg_guard_rig_showcases_anon` on
--    `public.community_rig_showcases` (blocks anonymous hijacking of title,
--    description, photos, specs, user metadata, and absurd likes_count jumps).
--    Also extends `guard_pro_tips_anon_update` to protect `rating` and `user_display_name`.
-- 3. Tombstones (`is_active = FALSE`, `catalog_revision = v_new_rev`) all 141
--    TR Hepsiburada (`laptop_hb_%`) rows so the Laptop category is 100% global
--    (237 global models), updates `catalog_meta`, and logs a `crawler_runs` audit row.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PART 1: Replace open DELETE RLS with ownership-verified SECURITY DEFINER RPCs
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_pol RECORD;
BEGIN
    IF to_regclass('public.hardware_pro_tips') IS NOT NULL THEN
        FOR v_pol IN
            SELECT policyname
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'hardware_pro_tips'
              AND cmd = 'DELETE'
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.hardware_pro_tips', v_pol.policyname);
        END LOOP;
        REVOKE DELETE ON public.hardware_pro_tips FROM anon, authenticated;
    END IF;

    IF to_regclass('public.community_rig_showcases') IS NOT NULL THEN
        FOR v_pol IN
            SELECT policyname
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'community_rig_showcases'
              AND cmd = 'DELETE'
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.community_rig_showcases', v_pol.policyname);
        END LOOP;
        REVOKE DELETE ON public.community_rig_showcases FROM anon, authenticated;
    END IF;
END $$;

CREATE OR REPLACE FUNCTION public.delete_own_pro_tip(
    p_tip_id TEXT,
    p_user_id TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF p_tip_id IS NULL OR LENGTH(TRIM(p_tip_id)) = 0 THEN
        RETURN FALSE;
    END IF;
    IF p_user_id IS NULL OR LENGTH(TRIM(p_user_id)) = 0 OR p_user_id LIKE 'guest%' OR p_user_id = 'anon' THEN
        RETURN FALSE;
    END IF;

    DELETE FROM public.hardware_pro_tips
    WHERE id = p_tip_id
      AND user_id = p_user_id;

    RETURN FOUND;
END;
$$;

REVOKE ALL ON FUNCTION public.delete_own_pro_tip(TEXT, TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.delete_own_pro_tip(TEXT, TEXT) TO anon, authenticated;

CREATE OR REPLACE FUNCTION public.delete_own_rig_showcase(
    p_showcase_id TEXT,
    p_user_id TEXT
)
RETURNS BOOLEAN
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF p_showcase_id IS NULL OR LENGTH(TRIM(p_showcase_id)) = 0 THEN
        RETURN FALSE;
    END IF;
    IF p_user_id IS NULL OR LENGTH(TRIM(p_user_id)) = 0 OR p_user_id LIKE 'guest%' OR p_user_id = 'anon' THEN
        RETURN FALSE;
    END IF;

    DELETE FROM public.community_rig_showcases
    WHERE id = p_showcase_id
      AND user_id = p_user_id;

    RETURN FOUND;
END;
$$;

REVOKE ALL ON FUNCTION public.delete_own_rig_showcase(TEXT, TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.delete_own_rig_showcase(TEXT, TEXT) TO anon, authenticated;

-- ------------------------------------------------------------------------------
-- PART 2: Guard triggers blocking anonymous content hijacking on showcases & tips
-- ------------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.guard_rig_showcases_anon_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF current_user IN ('anon', 'authenticated') THEN
        IF NEW.id IS DISTINCT FROM OLD.id
            OR NEW.user_id IS DISTINCT FROM OLD.user_id
            OR NEW.user_display_name IS DISTINCT FROM OLD.user_display_name
            OR NEW.user_badge IS DISTINCT FROM OLD.user_badge
            OR NEW.title IS DISTINCT FROM OLD.title
            OR NEW.description IS DISTINCT FROM OLD.description
            OR NEW.photo_rig_url IS DISTINCT FROM OLD.photo_rig_url
            OR NEW.photo_setup_url IS DISTINCT FROM OLD.photo_setup_url
            OR NEW.hardware_specs_json IS DISTINCT FROM OLD.hardware_specs_json
            OR NEW.created_at_epoch_ms IS DISTINCT FROM OLD.created_at_epoch_ms THEN
            RAISE EXCEPTION 'Unauthorized modification of protected community_rig_showcases fields';
        END IF;

        IF COALESCE(NEW.likes_count, 0) < 0
            OR ABS(COALESCE(NEW.likes_count, 0) - COALESCE(OLD.likes_count, 0)) > 1 THEN
            RAISE EXCEPTION 'Invalid likes_count update delta';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.guard_pro_tips_anon_update()
RETURNS TRIGGER
LANGUAGE plpgsql
AS $$
BEGIN
    IF current_user IN ('anon', 'authenticated') THEN
        IF NEW.tip_text IS DISTINCT FROM OLD.tip_text
            OR NEW.user_id IS DISTINCT FROM OLD.user_id
            OR NEW.user_display_name IS DISTINCT FROM OLD.user_display_name
            OR NEW.user_badge IS DISTINCT FROM OLD.user_badge
            OR NEW.hardware_id IS DISTINCT FROM OLD.hardware_id
            OR NEW.status IS DISTINCT FROM OLD.status
            OR NEW.rating IS DISTINCT FROM OLD.rating THEN
            RAISE EXCEPTION 'Unauthorized modification of protected hardware_pro_tips fields';
        END IF;

        IF COALESCE(NEW.upvotes_count, 0) < 0
            OR ABS(COALESCE(NEW.upvotes_count, 0) - COALESCE(OLD.upvotes_count, 0)) > 1 THEN
            RAISE EXCEPTION 'Invalid upvotes_count update delta';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;

DO $$
BEGIN
    IF to_regclass('public.community_rig_showcases') IS NOT NULL THEN
        ALTER TABLE public.community_rig_showcases ENABLE ROW LEVEL SECURITY;

        DROP TRIGGER IF EXISTS trg_guard_rig_showcases_anon ON public.community_rig_showcases;
        CREATE TRIGGER trg_guard_rig_showcases_anon
            BEFORE UPDATE ON public.community_rig_showcases
            FOR EACH ROW
            EXECUTE FUNCTION public.guard_rig_showcases_anon_update();
    END IF;

    IF to_regclass('public.hardware_pro_tips') IS NOT NULL THEN
        DROP TRIGGER IF EXISTS trg_guard_pro_tips_anon ON public.hardware_pro_tips;
        CREATE TRIGGER trg_guard_pro_tips_anon
            BEFORE UPDATE ON public.hardware_pro_tips
            FOR EACH ROW
            EXECUTE FUNCTION public.guard_pro_tips_anon_update();
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 3: Tombstone all 141 TR Hepsiburada (`laptop_hb_%`) rows -> 100% Global Laptops
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_hb_count INTEGER := 0;
    v_new_rev BIGINT;
    v_deactivated INTEGER := 0;
BEGIN
    SELECT COUNT(*)
    INTO v_hb_count
    FROM public.hardware_catalog
    WHERE is_active = TRUE
      AND id LIKE 'laptop_hb_%';

    IF v_hb_count > 0 THEN
        UPDATE public.catalog_meta
        SET
            latest_revision = latest_revision + 1,
            last_delta_at = NOW(),
            last_checked_at = NOW()
        WHERE id = 1
        RETURNING latest_revision INTO v_new_rev;

        UPDATE public.hardware_catalog
        SET
            is_active = FALSE,
            catalog_revision = v_new_rev,
            updated_at = NOW()
        WHERE is_active = TRUE
          AND id LIKE 'laptop_hb_%';
        GET DIAGNOSTICS v_deactivated = ROW_COUNT;

        UPDATE public.catalog_meta
        SET
            total_items = (SELECT COUNT(*)::INTEGER FROM public.hardware_catalog WHERE is_active = TRUE),
            last_checked_at = NOW()
        WHERE id = 1;

        INSERT INTO public.crawler_runs (run_mode, new_items_added, sources_checked, catalog_revision, status, notes)
        VALUES (
            'audit_v4_hardening',
            0,
            0,
            v_new_rev,
            'OK',
            'Round-4 hardening: tombstoned ' || v_deactivated || ' TR Hepsiburada (laptop_hb_*) rows for 100% global laptops; locked down DELETE via ownership-checked RPCs & added rig showcase UPDATE guard.'
        );
    END IF;
END $$;

NOTIFY pgrst, 'reload schema';

-- Verification summary
SELECT
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE) AS active_catalog_items,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE AND category = 'LAPTOP') AS active_global_laptops,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE AND id LIKE 'laptop_hb_%') AS remaining_hb_laptops,
    (SELECT latest_revision FROM public.catalog_meta WHERE id = 1) AS latest_catalog_revision,
    (SELECT total_items FROM public.catalog_meta WHERE id = 1) AS meta_total_items,
    (SELECT COUNT(*) FROM pg_policies WHERE schemaname = 'public' AND tablename IN ('hardware_pro_tips', 'community_rig_showcases') AND cmd = 'DELETE') AS open_delete_policies_left,
    (SELECT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'delete_own_pro_tip')) AS has_delete_own_pro_tip_rpc,
    (SELECT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'delete_own_rig_showcase')) AS has_delete_own_rig_showcase_rpc,
    (SELECT EXISTS (SELECT 1 FROM pg_trigger WHERE tgname = 'trg_guard_rig_showcases_anon')) AS has_rig_showcase_guard_trigger;

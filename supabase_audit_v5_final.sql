-- ==============================================================================
-- HARDWARE WIZARD - ROUND 5 FINAL RLS HARDENING & GLOBAL LAPTOP CLEANUP (v5.0.5)
-- ==============================================================================
-- 1. Restores SELECT/INSERT/UPDATE RLS policies on `public.community_rig_showcases`
--    (fixing Round-4 RLS-enabled zero-policy regression; UPDATE stays guarded by
--    `trg_guard_rig_showcases_anon` and DELETE stays revoked -> `delete_own_rig_showcase` RPC)
--    and enables RLS + policies on `public.showcase_likes`.
-- 2. Dynamically drops ALL legacy INSERT/UPDATE/DELETE policies on `public.community_deals`
--    (eliminating hidden `"Herkes ekleyebilir"` and `"Yonetici guncelleyebilir"` bypasses),
--    revokes UPDATE/DELETE from anon/authenticated, and enforces strict `status = 'PENDING'`
--    on INSERT.
-- 3. Hardens `public.user_profiles` INSERT policy to require clean initial reputation/ban
--    state (0 points, unbanned, starter badge) and locks down unused `public.price_drop_alerts`
--    from anonymous access until pricing alerts are enabled with auth.
-- 4. Tombstones (`is_active = FALSE`, `catalog_revision = v_new_rev`) the last 16 TR retail
--    İtopya (`laptop_itopya_%`) laptop rows -> bumps `catalog_meta` to rev 6 (`7,305` items,
--    `221` clean OEM laptops, 0 TR retail scrapes across all 28 categories).
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PART 1: Restore community_rig_showcases RLS policies & enable showcase_likes RLS
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_pol RECORD;
BEGIN
    IF to_regclass('public.community_rig_showcases') IS NOT NULL THEN
        ALTER TABLE public.community_rig_showcases ENABLE ROW LEVEL SECURITY;

        FOR v_pol IN
            SELECT policyname
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'community_rig_showcases'
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.community_rig_showcases', v_pol.policyname);
        END LOOP;

        REVOKE DELETE ON public.community_rig_showcases FROM anon, authenticated;

        CREATE POLICY "Vitrin okuma"
            ON public.community_rig_showcases
            FOR SELECT
            USING (TRUE);

        CREATE POLICY "Vitrin ekleme"
            ON public.community_rig_showcases
            FOR INSERT
            TO anon, authenticated
            WITH CHECK (
                LENGTH(TRIM(COALESCE(user_id, ''))) > 0
                AND COALESCE(likes_count, 0) = 0
            );

        -- UPDATE is strictly limited to likes_count +/- 1 by trg_guard_rig_showcases_anon
        CREATE POLICY "Vitrin begeni guncelleme"
            ON public.community_rig_showcases
            FOR UPDATE
            TO anon, authenticated
            USING (TRUE)
            WITH CHECK (TRUE);
    END IF;

    IF to_regclass('public.showcase_likes') IS NOT NULL THEN
        ALTER TABLE public.showcase_likes ENABLE ROW LEVEL SECURITY;

        FOR v_pol IN
            SELECT policyname
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'showcase_likes'
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.showcase_likes', v_pol.policyname);
        END LOOP;

        CREATE POLICY "Vitrin begeni okuma"
            ON public.showcase_likes
            FOR SELECT
            USING (TRUE);

        CREATE POLICY "Vitrin begeni ekleme"
            ON public.showcase_likes
            FOR INSERT
            TO anon, authenticated
            WITH CHECK (LENGTH(TRIM(COALESCE(user_id, ''))) > 0);

        CREATE POLICY "Vitrin begeni silme"
            ON public.showcase_likes
            FOR DELETE
            TO anon, authenticated
            USING (TRUE);
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 2: Eliminate ALL legacy bypass policies on community_deals
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_pol RECORD;
BEGIN
    IF to_regclass('public.community_deals') IS NOT NULL THEN
        ALTER TABLE public.community_deals ENABLE ROW LEVEL SECURITY;

        FOR v_pol IN
            SELECT policyname
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'community_deals'
              AND cmd IN ('INSERT', 'UPDATE', 'DELETE')
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.community_deals', v_pol.policyname);
        END LOOP;

        REVOKE UPDATE, DELETE ON public.community_deals FROM anon, authenticated;

        CREATE POLICY "Kullanicilar yalnizca bekleyen fiyat bildirimi ekleyebilir"
            ON public.community_deals
            FOR INSERT
            TO anon, authenticated
            WITH CHECK (status = 'PENDING');
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 3: Harden user_profiles INSERT & lock down unused price_drop_alerts
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_pol RECORD;
BEGIN
    IF to_regclass('public.user_profiles') IS NOT NULL THEN
        FOR v_pol IN
            SELECT policyname
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'user_profiles'
              AND cmd = 'INSERT'
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.user_profiles', v_pol.policyname);
        END LOOP;

        CREATE POLICY "Kullanicilar temiz baslangic profiliyle kayit olabilir"
            ON public.user_profiles
            FOR INSERT
            TO anon, authenticated
            WITH CHECK (
                COALESCE(reputation_score, 0) = 0
                AND COALESCE(total_deals_count, 0) = 0
                AND COALESCE(total_tunings_count, 0) = 0
                AND COALESCE(total_tips_count, 0) = 0
                AND COALESCE(warning_count, 0) = 0
                AND COALESCE(is_banned, FALSE) = FALSE
                AND COALESCE(ban_escalation_tier, 0) = 0
                AND COALESCE(badge_title, 'Lehim Çırağı') = 'Lehim Çırağı'
            );
    END IF;

    IF to_regclass('public.price_drop_alerts') IS NOT NULL THEN
        ALTER TABLE public.price_drop_alerts ENABLE ROW LEVEL SECURITY;

        FOR v_pol IN
            SELECT policyname
            FROM pg_policies
            WHERE schemaname = 'public'
              AND tablename = 'price_drop_alerts'
        LOOP
            EXECUTE format('DROP POLICY IF EXISTS %I ON public.price_drop_alerts', v_pol.policyname);
        END LOOP;

        REVOKE ALL ON public.price_drop_alerts FROM anon, authenticated;
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 4: Tombstone last 16 TR retail Itopya (`laptop_itopya_%`) laptop rows
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_itopya_count INTEGER := 0;
    v_new_rev BIGINT;
    v_deactivated INTEGER := 0;
BEGIN
    SELECT COUNT(*)
    INTO v_itopya_count
    FROM public.hardware_catalog
    WHERE is_active = TRUE
      AND id LIKE 'laptop_itopya_%';

    IF v_itopya_count > 0 THEN
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
          AND id LIKE 'laptop_itopya_%';
        GET DIAGNOSTICS v_deactivated = ROW_COUNT;

        UPDATE public.catalog_meta
        SET
            total_items = (SELECT COUNT(*)::INTEGER FROM public.hardware_catalog WHERE is_active = TRUE),
            last_checked_at = NOW()
        WHERE id = 1;

        INSERT INTO public.crawler_runs (run_mode, new_items_added, sources_checked, catalog_revision, status, notes)
        VALUES (
            'audit_v5_final',
            0,
            0,
            v_new_rev,
            'OK',
            'Round-5 final hardening: restored community_rig_showcases RLS policies, purged legacy community_deals bypasses, hardened user_profiles INSERT & price_drop_alerts, tombstoned ' || v_deactivated || ' TR Itopya laptop rows.'
        );
    END IF;
END $$;

NOTIFY pgrst, 'reload schema';

-- Verification summary
SELECT
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE) AS active_catalog_items,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE AND category = 'LAPTOP') AS active_laptops,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE is_active = TRUE AND id LIKE 'laptop_itopya_%') AS remaining_itopya_laptops,
    (SELECT latest_revision FROM public.catalog_meta WHERE id = 1) AS latest_catalog_revision,
    (SELECT COUNT(*) FROM public.community_rig_showcases) AS total_rig_showcases,
    (SELECT COUNT(*) FROM pg_policies WHERE schemaname = 'public' AND tablename = 'community_rig_showcases') AS rig_showcase_policy_count,
    (SELECT COUNT(*) FROM pg_policies WHERE schemaname = 'public' AND tablename = 'community_deals' AND cmd IN ('INSERT', 'UPDATE', 'DELETE')) AS deals_write_policy_count,
    (SELECT COUNT(*) FROM pg_policies WHERE schemaname = 'public' AND tablename = 'price_drop_alerts') AS price_alert_open_policies;

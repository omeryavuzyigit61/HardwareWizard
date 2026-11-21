-- ==============================================================================
-- HARDWARE WIZARD - ROUND 2 SECURITY, PRIVACY & SOCKET DATA HARDENING (v5.0.2)
-- ==============================================================================
-- 1. Blocks anonymous/public SELECT of password_hash, email, birth_date_iso,
--    and gender on public.user_profiles (keeps authenticate_user RPC & stats working).
-- 2. Blocks anonymous/public SELECT of user_email on public.community_deals.
-- 3. Creates dedicated public.content_reports table with strict privileges & RLS
--    (INSERT-only for clients, SELECT/UPDATE/DELETE service_role-only) and migrates
--    any legacy REPORT:* rows out of public.hardware_pro_tips.
-- 4. Normalizes Intel numeric CPU sockets ('1700' -> 'LGA1700', '1851' -> 'LGA1851',
--    '1200' -> 'LGA1200') across public.hardware_catalog and bumps catalog_revision
--    so existing installed mobile clients receive the fix automatically via Delta Sync.
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- PART 1: Column-level SELECT lockdown on user_profiles (prevent password_hash & PII dump)
-- ------------------------------------------------------------------------------
DO $$
BEGIN
    IF to_regclass('public.user_profiles') IS NOT NULL THEN
        REVOKE SELECT ON public.user_profiles FROM anon, authenticated;
        GRANT SELECT (
            id,
            username,
            spirit_hardware,
            created_at_epoch_ms,
            reputation_score,
            total_deals_count,
            total_tunings_count,
            total_tips_count,
            badge_title,
            warning_count,
            is_banned,
            banned_until_epoch_ms,
            ban_reason,
            ban_escalation_tier
        ) ON public.user_profiles TO anon, authenticated;
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 2: Column-level SELECT lockdown on community_deals (hide user_email PII)
-- ------------------------------------------------------------------------------
DO $$
BEGIN
    IF to_regclass('public.community_deals') IS NOT NULL THEN
        REVOKE SELECT ON public.community_deals FROM anon, authenticated;
        GRANT SELECT (
            id,
            hardware_id,
            hardware_name,
            price_try,
            deal_type,
            note,
            proof_url,
            user_display_name,
            created_at_epoch_ms,
            status,
            review_note,
            reviewed_by,
            reviewed_at_epoch_ms,
            created_at
        ) ON public.community_deals TO anon, authenticated;
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 3: Dedicated content_reports table + migrate legacy REPORT:* rows from hardware_pro_tips
-- ------------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS public.content_reports (
    id TEXT PRIMARY KEY,
    target_type TEXT NOT NULL,
    target_id TEXT NOT NULL,
    target_title TEXT NOT NULL,
    reported_user_id TEXT NOT NULL DEFAULT '',
    reported_user_display_name TEXT NOT NULL DEFAULT '',
    reporter_user_id TEXT NOT NULL DEFAULT '',
    reporter_user_display_name TEXT NOT NULL DEFAULT 'Kullanıcı',
    is_self_report BOOLEAN NOT NULL DEFAULT FALSE,
    reason TEXT NOT NULL DEFAULT 'OFFENSIVE_OR_INAPPROPRIATE',
    details TEXT NOT NULL DEFAULT '',
    status TEXT NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'RESOLVED_REMOVED', 'RESOLVED_DISMISSED')),
    created_at_epoch_ms BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_content_reports_status ON public.content_reports(status);
CREATE INDEX IF NOT EXISTS idx_content_reports_created ON public.content_reports(created_at_epoch_ms DESC);

ALTER TABLE public.content_reports ENABLE ROW LEVEL SECURITY;

REVOKE ALL ON public.content_reports FROM anon, authenticated;
GRANT INSERT ON public.content_reports TO anon, authenticated;

DROP POLICY IF EXISTS "Users can submit pending content reports" ON public.content_reports;
CREATE POLICY "Users can submit pending content reports"
    ON public.content_reports
    FOR INSERT
    TO anon, authenticated
    WITH CHECK (status = 'PENDING');

-- Note: No SELECT/UPDATE/DELETE privilege or policy for anon/authenticated -> strictly service_role only!

DO $$
BEGIN
    IF to_regclass('public.hardware_pro_tips') IS NOT NULL THEN
        INSERT INTO public.content_reports (
            id,
            target_type,
            target_id,
            target_title,
            reported_user_id,
            reported_user_display_name,
            reporter_user_id,
            reporter_user_display_name,
            is_self_report,
            reason,
            details,
            status,
            created_at_epoch_ms
        )
        SELECT
            t.id,
            COALESCE(NULLIF(split_part(t.hardware_id, ':', 2), ''), 'COMMENT'),
            COALESCE(NULLIF(split_part(t.hardware_id, ':', 3), ''), t.hardware_id),
            COALESCE(t.hardware_name, 'İçerik'),
            '',
            '',
            COALESCE(t.user_id, ''),
            COALESCE(t.user_display_name, 'Kullanıcı'),
            FALSE,
            COALESCE(NULLIF(t.user_badge, ''), 'OFFENSIVE_OR_INAPPROPRIATE'),
            COALESCE(t.tip_text, ''),
            CASE
                WHEN UPPER(COALESCE(t.status, 'PENDING')) = 'APPROVED' THEN 'RESOLVED_DISMISSED'
                WHEN UPPER(COALESCE(t.status, 'PENDING')) = 'REJECTED' THEN 'RESOLVED_REMOVED'
                ELSE 'PENDING'
            END,
            COALESCE(t.created_at_epoch_ms, 0)
        FROM public.hardware_pro_tips t
        WHERE t.hardware_id LIKE 'REPORT:%'
        ON CONFLICT (id) DO NOTHING;

        DELETE FROM public.hardware_pro_tips
        WHERE hardware_id LIKE 'REPORT:%';
    END IF;
END $$;

-- ------------------------------------------------------------------------------
-- PART 4: Normalize Intel numeric CPU sockets ('1700'/'1851'/'1200' -> 'LGA*')
--         and bump catalog_revision so existing devices pick it up via Delta Sync
-- ------------------------------------------------------------------------------
DO $$
DECLARE
    v_bare_count INTEGER;
    v_new_rev BIGINT;
BEGIN
    SELECT COUNT(*)
    INTO v_bare_count
    FROM public.hardware_catalog
    WHERE category = 'CPU'
      AND (spec_payload->>'socket') IN ('1700', '1851', '1200', '1151', '1150', '1155', '2066');

    IF v_bare_count > 0 THEN
        UPDATE public.catalog_meta
        SET
            latest_revision = latest_revision + 1,
            last_delta_at = NOW(),
            last_checked_at = NOW()
        WHERE id = 1
        RETURNING latest_revision INTO v_new_rev;

        UPDATE public.hardware_catalog
        SET
            spec_payload = jsonb_set(
                spec_payload,
                '{socket}',
                to_jsonb('LGA' || (spec_payload->>'socket'))
            ),
            catalog_revision = COALESCE(v_new_rev, catalog_revision),
            updated_at = NOW()
        WHERE category = 'CPU'
          AND (spec_payload->>'socket') IN ('1700', '1851', '1200', '1151', '1150', '1155', '2066');

        INSERT INTO public.crawler_runs (run_mode, new_items_added, sources_checked, catalog_revision, status, notes)
        VALUES (
            'socket_normalization',
            v_bare_count,
            0,
            COALESCE(v_new_rev, 1),
            'OK',
            'Normalized Intel bare numeric CPU sockets (1700/1851/1200) to LGA* format and hardened PII/reports RLS.'
        );
    END IF;
END $$;

NOTIFY pgrst, 'reload schema';

-- Verification summary
SELECT
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE category = 'CPU' AND (spec_payload->>'socket') IN ('1700', '1851', '1200')) AS remaining_bare_intel_sockets,
    (SELECT COUNT(*) FROM public.hardware_catalog WHERE category = 'CPU' AND (spec_payload->>'socket') IN ('LGA1700', 'LGA1851', 'LGA1200')) AS normalized_lga_cpus,
    (SELECT COUNT(*) FROM public.hardware_pro_tips WHERE hardware_id LIKE 'REPORT:%') AS remaining_leaked_reports,
    (SELECT COUNT(*) FROM public.content_reports) AS migrated_content_reports,
    (SELECT latest_revision FROM public.catalog_meta WHERE id = 1) AS latest_catalog_revision;

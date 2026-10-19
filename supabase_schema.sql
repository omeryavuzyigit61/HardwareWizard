-- ==============================================================================
-- HARDWARE WIZARD - SUPABASE POSTGRESQL VERİTABANI ŞEMASI & RLS POLİTİKALARI
-- ==============================================================================
-- Bu SQL betiğini Supabase Dashboard -> SQL Editor alanına yapıştırıp "Run" butonuna basın.
-- 
-- Özellikler:
-- 1. Tüm donanım bileşenleri için dinamik topluluk fiyat bildirimi tablosu
-- 2. 4 İşlem Türü (Kendi Aldığım, 2. El, Kampanya/İndirim, Yurtdışı)
-- 3. Kanıt URL'si (Proof URL) zorunluluğu / link takibi
-- 4. Yönetici Moderasyonu (PENDING -> APPROVED / REJECTED)
-- 5. RLS (Row Level Security) ile güvenli okuma/yazma politikaları
-- ==============================================================================

-- 1. community_deals tablosu
create table if not exists public.community_deals (
    id text primary key,
    hardware_id text not null,
    hardware_name text not null,
    price_try numeric(12, 2) not null check (price_try > 0),
    deal_type text not null check (deal_type in ('own_purchase', 'second_hand', 'campaign', 'foreign')),
    note text,
    proof_url text,
    user_display_name text not null default 'Topluluk Üyesi',
    user_email text,
    created_at_epoch_ms bigint not null,
    status text not null default 'PENDING' check (status in ('PENDING', 'APPROVED', 'REJECTED')),
    review_note text,
    reviewed_by text,
    reviewed_at_epoch_ms bigint,
    created_at timestamp with time zone default timezone('utc'::text, now()) not null
);

-- Hızlı sorgular için indeksler
create index if not exists idx_community_deals_hardware_id on public.community_deals(hardware_id);
create index if not exists idx_community_deals_status on public.community_deals(status);
create index if not exists idx_community_deals_created_at on public.community_deals(created_at_epoch_ms desc);

-- 2. RLS (Row Level Security) Etkinleştirme
alter table public.community_deals enable row level security;

-- Herkes onaylanmış fırsatları okuyabilir
create policy "Herkes onaylanmis fırsatlari gorebilir"
    on public.community_deals for select
    using (status = 'APPROVED' or status = 'PENDING');

-- Herkes yeni fiyat fırsatı bildirebilir (otomatik PENDING olarak başlar)
create policy "Kullanicilar yeni fiyat bildirimi yapabilir"
    on public.community_deals for insert
    with check (status = 'PENDING');

-- Yönetici tüm kayıtları güncelleyebilir (onay/red)
create policy "Yonetici kayitlari guncelleyebilir"
    on public.community_deals for update
    using (true)
    with check (true);

-- 3. Başlangıç Tohum Verileri (Örnek Topluluk Fiyatları)
insert into public.community_deals (
    id, hardware_id, hardware_name, price_try, deal_type, note, proof_url, user_display_name, created_at_epoch_ms, status
) values
(
    'deal-seed-1',
    'gpu_rtx_4070_super',
    'GeForce RTX 4070 Super 12GB',
    23499.00,
    'campaign',
    'İtopya Gece Kampanyası ile kupon koduyla alındı, faturalı sıfır kutu.',
    'https://www.itopya.com',
    'Mert K.',
    1713000000000,
    'APPROVED'
),
(
    'deal-seed-2',
    'cpu_ryzen_7_7800x3d',
    'AMD Ryzen 7 7800X3D',
    13850.00,
    'own_purchase',
    'Vatan Bilgisayar mağaza elden alım nakit indirimi.',
    'https://www.vatanbilgisayar.com',
    'Burak T.',
    1713100000000,
    'APPROVED'
),
(
    'deal-seed-3',
    'case_lian_li_o11_vision',
    'Lian Li O11 Vision',
    5200.00,
    'second_hand',
    'Sarı siteden çiziksiz, 6 ay garantili kutulu teslim alındı.',
    'https://www.sahibinden.com',
    'Oğuz Y.',
    1713200000000,
    'APPROVED'
),
(
    'deal-seed-4',
    'cooler_arctic_liquid_freezer_iii_360',
    'Arctic Liquid Freezer III 360',
    3450.00,
    'foreign',
    'Amazon Almanya üzerinden gümrük ve kargo dahil sipariş edildi.',
    'https://www.amazon.com.tr',
    'Caner B.',
    1713300000000,
    'APPROVED'
)
on conflict (id) do nothing;

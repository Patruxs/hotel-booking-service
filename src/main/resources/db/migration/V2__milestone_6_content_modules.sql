alter table commission_packages
    add column if not exists description text;

alter table banners
    add column if not exists link_type varchar(32) not null default 'URL';

alter table banners
    add constraint banners_link_type_check check (link_type in ('URL', 'HOTEL', 'NEWS'));

alter table contact_messages
    add column if not exists handled_by_account_id uuid references accounts(id) on delete set null,
    add column if not exists note text,
    add column if not exists ip_address varchar(64),
    add column if not exists user_agent text;

alter table reviews
    add column if not exists deleted_at timestamptz;

alter table hotel_policies
    add column if not exists enabled boolean not null default true,
    alter column title type varchar(200);

insert into api_actions (id, key, http_method, path, description, enabled, is_system)
values ('00000000-0000-4002-8000-000000000018', 'reports.hotel.view', 'GET', '/api/v1/dashboard?hotelId={hotelId}', 'View hotel-scoped reports.', true, true)
on conflict (key) do nothing;

insert into action_policies (id, action_id, permission_id, scope)
select seed.id, a.id, p.id, seed.scope
from (
    values
        ('10000000-0000-4003-8000-000000000024'::uuid, 'reports.hotel.view', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000025'::uuid, 'reports.hotel.view', 'reports.view', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000026'::uuid, 'reports.hotel.view', 'reports.view', 'HOTEL_MEMBER')
) as seed(id, action_key, permission_key, scope)
join api_actions a on a.key = seed.action_key
join permissions p on p.key = seed.permission_key
on conflict (action_id, permission_id, scope) do nothing;

create table if not exists banner_images (
    id uuid primary key,
    banner_id uuid not null references banners(id) on delete cascade,
    image_asset_id uuid references image_assets(id) on delete set null,
    url text not null,
    sort_order integer not null default 0,
    constraint banner_images_order_unique unique (banner_id, sort_order)
);

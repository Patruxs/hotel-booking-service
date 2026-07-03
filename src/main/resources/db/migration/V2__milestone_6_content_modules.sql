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
    add column if not exists deleted_at timestamptz;

alter table hotel_policies
    drop constraint if exists hotel_policies_type_unique,
    drop constraint if exists hotel_policies_order_unique;

create unique index if not exists hotel_policies_type_active_uidx
    on hotel_policies(hotel_id, type)
    where deleted_at is null;

create unique index if not exists hotel_policies_order_active_uidx
    on hotel_policies(hotel_id, sort_order)
    where deleted_at is null;

create table if not exists banner_images (
    id uuid primary key,
    banner_id uuid not null references banners(id) on delete cascade,
    image_asset_id uuid references image_assets(id) on delete set null,
    url text not null,
    sort_order integer not null default 0,
    constraint banner_images_order_unique unique (banner_id, sort_order)
);

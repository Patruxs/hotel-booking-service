create table accounts (
    id uuid primary key,
    email varchar(320) not null unique,
    password_hash varchar(255),
    first_name varchar(50) not null,
    last_name varchar(50) not null,
    phone varchar(32),
    date_of_birth date,
    email_verified boolean not null default false,
    auth_provider varchar(32) not null default 'LOCAL',
    avatar_url text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint accounts_auth_provider_check check (auth_provider in ('LOCAL', 'GOOGLE'))
);

create table roles (
    id uuid primary key,
    name varchar(64) not null unique,
    display_name varchar(120) not null,
    description text,
    is_system boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table permissions (
    id uuid primary key,
    key varchar(120) not null unique,
    name varchar(120) not null,
    description text,
    is_system boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table api_actions (
    id uuid primary key,
    key varchar(160) not null unique,
    http_method varchar(12) not null,
    path varchar(255) not null,
    description text,
    enabled boolean not null default true,
    is_system boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint api_actions_http_method_check check (http_method in ('GET', 'POST', 'PUT', 'PATCH', 'DELETE'))
);

create table role_permissions (
    role_id uuid not null references roles(id) on delete cascade,
    permission_id uuid not null references permissions(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (role_id, permission_id)
);

create table action_policies (
    id uuid primary key,
    action_id uuid not null references api_actions(id) on delete cascade,
    permission_id uuid not null references permissions(id) on delete restrict,
    scope varchar(32) not null default 'GLOBAL',
    mode varchar(16) not null default 'ANY',
    created_at timestamptz not null default now(),
    constraint action_policies_scope_check check (scope in ('GLOBAL', 'HOTEL_OWNER', 'HOTEL_MEMBER', 'SELF')),
    constraint action_policies_mode_check check (mode in ('ANY', 'ALL')),
    constraint action_policies_unique unique (action_id, permission_id, scope)
);

create table account_roles (
    account_id uuid not null references accounts(id) on delete cascade,
    role_id uuid not null references roles(id) on delete restrict,
    created_at timestamptz not null default now(),
    primary key (account_id, role_id)
);

create table auth_sessions (
    id uuid primary key,
    account_id uuid not null references accounts(id) on delete cascade,
    jti varchar(120) not null unique,
    refresh_token_hash varchar(255) not null,
    provider varchar(32) not null default 'LOCAL',
    ip_address varchar(64),
    user_agent text,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null,
    revoked_at timestamptz,
    constraint auth_sessions_provider_check check (provider in ('LOCAL', 'GOOGLE'))
);

create index auth_sessions_account_id_idx on auth_sessions(account_id);

create table email_verification_tokens (
    id uuid primary key,
    account_id uuid not null references accounts(id) on delete cascade,
    token_hash varchar(255) not null unique,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null,
    used_at timestamptz
);

create table password_reset_tokens (
    id uuid primary key,
    account_id uuid not null references accounts(id) on delete cascade,
    token_hash varchar(255) not null unique,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null,
    used_at timestamptz
);

create table commission_packages (
    id uuid primary key,
    code varchar(64) not null unique,
    name varchar(120) not null,
    commission_rate numeric(5, 4) not null,
    active boolean not null default true,
    is_system boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint commission_packages_rate_check check (commission_rate >= 0 and commission_rate <= 1)
);

create table hotels (
    id uuid primary key,
    owner_id uuid not null references accounts(id) on delete restrict,
    name varchar(160) not null,
    slug varchar(180) not null unique,
    description text,
    address text,
    city varchar(120),
    country varchar(120) not null default 'Vietnam',
    email varchar(320),
    phone varchar(32),
    status varchar(32) not null default 'DRAFT',
    star_rating numeric(2, 1),
    deleted_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint hotels_status_check check (status in ('DRAFT', 'ACTIVE', 'SUSPENDED', 'ARCHIVED')),
    constraint hotels_star_rating_check check (star_rating is null or (star_rating >= 0 and star_rating <= 5))
);

create index hotels_owner_id_idx on hotels(owner_id);
create index hotels_public_idx on hotels(status, deleted_at);

create table hotel_members (
    hotel_id uuid not null references hotels(id) on delete cascade,
    account_id uuid not null references accounts(id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (hotel_id, account_id)
);

create table hotel_commission_packages (
    hotel_id uuid primary key references hotels(id) on delete cascade,
    commission_package_id uuid not null references commission_packages(id) on delete restrict,
    assigned_at timestamptz not null default now()
);

create table amenities (
    id uuid primary key,
    key varchar(120) not null unique,
    name varchar(120) not null,
    type varchar(64) not null,
    active boolean not null default true,
    is_system boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table room_types (
    id uuid primary key,
    hotel_id uuid not null references hotels(id) on delete cascade,
    name varchar(160) not null,
    description text,
    price_per_night numeric(14, 2) not null,
    max_guests integer not null,
    number_of_bedrooms integer,
    active boolean not null default true,
    deleted_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint room_types_price_check check (price_per_night >= 0),
    constraint room_types_max_guests_check check (max_guests > 0),
    constraint room_types_bedrooms_check check (number_of_bedrooms is null or number_of_bedrooms >= 0)
);

create unique index room_types_hotel_name_active_uidx on room_types(hotel_id, lower(name)) where deleted_at is null;
create index room_types_hotel_id_idx on room_types(hotel_id);

create table room_type_amenities (
    room_type_id uuid not null references room_types(id) on delete cascade,
    amenity_id uuid not null references amenities(id) on delete restrict,
    created_at timestamptz not null default now(),
    primary key (room_type_id, amenity_id)
);

create table rooms (
    id uuid primary key,
    hotel_id uuid not null references hotels(id) on delete cascade,
    room_type_id uuid not null references room_types(id) on delete cascade,
    room_number varchar(40) not null,
    condition varchar(32) not null default 'CLEAN',
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint rooms_condition_check check (condition in ('CLEAN', 'DIRTY', 'MAINTENANCE', 'OUT_OF_SERVICE')),
    constraint rooms_hotel_room_number_unique unique (hotel_id, room_number)
);

create index rooms_room_type_id_idx on rooms(room_type_id);

create table inventories (
    id uuid primary key,
    hotel_id uuid not null references hotels(id) on delete cascade,
    room_type_id uuid not null references room_types(id) on delete cascade,
    stay_date date not null,
    total_rooms integer not null,
    available_rooms integer not null,
    stop_sell boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint inventories_counts_check check (total_rooms >= 0 and available_rooms >= 0 and available_rooms <= total_rooms),
    constraint inventories_room_type_stay_date_unique unique (room_type_id, stay_date)
);

create index inventories_hotel_stay_date_idx on inventories(hotel_id, stay_date);

create table promotions (
    id uuid primary key,
    hotel_id uuid references hotels(id) on delete cascade,
    code varchar(64) not null unique,
    name varchar(160) not null,
    discount_type varchar(16) not null,
    discount_value numeric(14, 2) not null,
    max_discount numeric(14, 2),
    min_booking_amount numeric(14, 2),
    total_usage_limit integer,
    per_user_usage_limit integer,
    used_count integer not null default 0,
    starts_at timestamptz,
    ends_at timestamptz,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint promotions_discount_type_check check (discount_type in ('PERCENT', 'FIXED')),
    constraint promotions_discount_value_check check (discount_value >= 0),
    constraint promotions_usage_check check (
        used_count >= 0
        and (total_usage_limit is null or total_usage_limit >= 0)
        and (per_user_usage_limit is null or per_user_usage_limit >= 0)
    )
);

create table bookings (
    id uuid primary key,
    account_id uuid not null references accounts(id) on delete restrict,
    hotel_id uuid not null references hotels(id) on delete restrict,
    promotion_id uuid references promotions(id) on delete set null,
    booking_reference varchar(80) not null unique,
    status varchar(32) not null default 'PENDING',
    check_in date not null,
    check_out date not null,
    guest_name varchar(120) not null,
    guest_email varchar(320) not null,
    guest_phone varchar(32) not null,
    note varchar(1000),
    subtotal_amount numeric(14, 2) not null,
    discount_amount numeric(14, 2) not null default 0,
    total_amount numeric(14, 2) not null,
    commission_package_code varchar(64),
    commission_rate numeric(5, 4) not null default 0,
    commission_amount numeric(14, 2) not null default 0,
    pending_expires_at timestamptz,
    cancelled_at timestamptz,
    completed_at timestamptz,
    no_show_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint bookings_status_check check (status in ('PENDING', 'CONFIRMED', 'CHECKED_IN', 'COMPLETED', 'CANCELLED', 'NO_SHOW')),
    constraint bookings_stay_range_check check (check_out > check_in),
    constraint bookings_amounts_check check (
        subtotal_amount >= 0
        and discount_amount >= 0
        and total_amount >= 0
        and commission_rate >= 0
        and commission_rate <= 1
        and commission_amount >= 0
    )
);

create index bookings_account_id_idx on bookings(account_id);
create index bookings_hotel_status_idx on bookings(hotel_id, status);
create index bookings_check_in_out_idx on bookings(check_in, check_out);

create table booking_items (
    id uuid primary key,
    booking_id uuid not null references bookings(id) on delete cascade,
    room_type_id uuid not null references room_types(id) on delete restrict,
    room_type_name varchar(160) not null,
    quantity integer not null,
    unit_price numeric(14, 2) not null,
    max_guests integer not null,
    line_total numeric(14, 2) not null,
    created_at timestamptz not null default now(),
    constraint booking_items_quantity_check check (quantity > 0),
    constraint booking_items_amounts_check check (unit_price >= 0 and line_total >= 0 and max_guests > 0),
    constraint booking_items_one_room_type_per_booking unique (booking_id, room_type_id)
);

create table check_ins (
    id uuid primary key,
    booking_id uuid not null unique references bookings(id) on delete cascade,
    checked_in_by_account_id uuid references accounts(id) on delete set null,
    checked_in_at timestamptz not null default now(),
    checked_out_at timestamptz,
    note varchar(1000)
);

create table booking_guests (
    id uuid primary key,
    check_in_id uuid not null references check_ins(id) on delete cascade,
    full_name varchar(120) not null,
    identity_number varchar(40),
    phone varchar(32),
    is_primary boolean not null default false,
    guest_order integer not null,
    constraint booking_guests_order_check check (guest_order >= 0),
    constraint booking_guests_order_unique unique (check_in_id, guest_order)
);

create table payments (
    id uuid primary key,
    booking_id uuid not null references bookings(id) on delete restrict,
    provider varchar(32) not null default 'VNPAY',
    status varchar(32) not null default 'INIT',
    amount numeric(14, 2) not null,
    currency varchar(8) not null default 'VND',
    merchant_txn_ref varchar(160) not null unique,
    payment_url text,
    provider_transaction_no varchar(160),
    paid_at timestamptz,
    expires_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint payments_provider_check check (provider in ('VNPAY')),
    constraint payments_status_check check (status in ('INIT', 'PENDING', 'SUCCEEDED', 'FAILED', 'CANCELED', 'REFUND_PENDING', 'REFUNDED', 'LATE_SUCCEEDED')),
    constraint payments_amount_check check (amount >= 0)
);

create index payments_booking_id_idx on payments(booking_id);

create table payment_events (
    id uuid primary key,
    payment_id uuid not null references payments(id) on delete cascade,
    event_type varchar(64) not null,
    payload jsonb not null,
    created_at timestamptz not null default now()
);

create index payment_events_payment_id_idx on payment_events(payment_id);
create index payment_events_payload_gin_idx on payment_events using gin (payload);

create table image_assets (
    id uuid primary key,
    owner_account_id uuid references accounts(id) on delete set null,
    provider varchar(32) not null,
    public_id varchar(255),
    url text not null,
    secure_url text,
    width integer,
    height integer,
    bytes bigint,
    created_at timestamptz not null default now(),
    constraint image_assets_provider_check check (provider in ('LOCAL', 'CLOUDINARY'))
);

create table gallery_folders (
    id uuid primary key,
    owner_account_id uuid not null references accounts(id) on delete cascade,
    folder_name varchar(120) not null,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint gallery_folders_owner_name_unique unique (owner_account_id, folder_name)
);

create table gallery_images (
    id uuid primary key,
    folder_id uuid not null references gallery_folders(id) on delete cascade,
    image_asset_id uuid not null references image_assets(id) on delete cascade,
    created_at timestamptz not null default now(),
    constraint gallery_images_unique unique (folder_id, image_asset_id)
);

create table hotel_images (
    id uuid primary key,
    hotel_id uuid not null references hotels(id) on delete cascade,
    image_asset_id uuid references image_assets(id) on delete set null,
    url text not null,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    constraint hotel_images_order_unique unique (hotel_id, sort_order)
);

create table room_type_images (
    id uuid primary key,
    room_type_id uuid not null references room_types(id) on delete cascade,
    image_asset_id uuid references image_assets(id) on delete set null,
    url text not null,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    constraint room_type_images_order_unique unique (room_type_id, sort_order)
);

create table reviews (
    id uuid primary key,
    booking_id uuid not null unique references bookings(id) on delete restrict,
    hotel_id uuid not null references hotels(id) on delete restrict,
    account_id uuid not null references accounts(id) on delete restrict,
    rating numeric(2, 1) not null,
    comment text,
    visible boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint reviews_rating_check check (rating >= 1 and rating <= 5)
);

create table review_images (
    id uuid primary key,
    review_id uuid not null references reviews(id) on delete cascade,
    image_asset_id uuid references image_assets(id) on delete set null,
    url text not null,
    sort_order integer not null default 0,
    constraint review_images_order_unique unique (review_id, sort_order)
);

create table news (
    id uuid primary key,
    author_account_id uuid references accounts(id) on delete set null,
    title varchar(180) not null,
    slug varchar(220) not null unique,
    summary varchar(500),
    content text not null,
    status varchar(32) not null default 'DRAFT',
    published_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint news_status_check check (status in ('DRAFT', 'PUBLISHED', 'ARCHIVED'))
);

create table news_images (
    id uuid primary key,
    news_id uuid not null references news(id) on delete cascade,
    image_asset_id uuid references image_assets(id) on delete set null,
    url text not null,
    sort_order integer not null default 0,
    constraint news_images_order_unique unique (news_id, sort_order)
);

create table banners (
    id uuid primary key,
    title varchar(180) not null,
    subtitle varchar(300),
    image_url text not null,
    link_url text,
    position integer not null,
    active boolean not null default true,
    starts_at timestamptz,
    ends_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint banners_position_unique unique (position)
);

create table contact_messages (
    id uuid primary key,
    account_id uuid references accounts(id) on delete set null,
    name varchar(120) not null,
    email varchar(320) not null,
    phone varchar(32),
    subject varchar(180),
    message text not null,
    status varchar(32) not null default 'NEW',
    metadata jsonb,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint contact_messages_status_check check (status in ('NEW', 'IN_PROGRESS', 'RESOLVED', 'SPAM'))
);

create table notifications (
    id uuid primary key,
    recipient_account_id uuid not null references accounts(id) on delete cascade,
    type varchar(64) not null,
    title varchar(180) not null,
    body text,
    link_url text,
    read_at timestamptz,
    created_at timestamptz not null default now()
);

create index notifications_recipient_created_idx on notifications(recipient_account_id, created_at desc);

create table hotel_policies (
    id uuid primary key,
    hotel_id uuid not null references hotels(id) on delete cascade,
    type varchar(64) not null,
    title varchar(160) not null,
    content text not null,
    sort_order integer not null default 0,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint hotel_policies_type_check check (type in ('CHECK_IN', 'CANCELLATION', 'PAYMENT', 'CHILDREN', 'PET', 'SMOKING', 'GENERAL')),
    constraint hotel_policies_type_unique unique (hotel_id, type),
    constraint hotel_policies_order_unique unique (hotel_id, sort_order)
);

insert into roles (id, name, display_name, description, is_system) values
    ('00000000-0000-4000-8000-000000000001', 'ADMIN', 'Admin', 'Platform administrator with all system actions.', true),
    ('00000000-0000-4000-8000-000000000002', 'OWNER', 'Hotel Owner', 'Hotel owner with hotel-scoped management actions.', true),
    ('00000000-0000-4000-8000-000000000003', 'MANAGER', 'Manager', 'Hotel manager with operational hotel actions.', true),
    ('00000000-0000-4000-8000-000000000004', 'RECEPTIONIST', 'Receptionist', 'Front-desk operator for bookings and stays.', true),
    ('00000000-0000-4000-8000-000000000005', 'STAFF', 'Staff', 'Assigned hotel staff with read-oriented access.', true),
    ('00000000-0000-4000-8000-000000000006', 'CUSTOMER', 'Customer', 'Customer account role without admin sidebar actions.', true);

insert into permissions (id, key, name, description, is_system) values
    ('00000000-0000-4001-8000-000000000001', 'security.manage', 'Manage security catalog', 'Manage roles, permissions, and API actions.', true),
    ('00000000-0000-4001-8000-000000000002', 'hotels.manage', 'Manage hotels', 'Create and manage hotel records.', true),
    ('00000000-0000-4001-8000-000000000003', 'hotels.operate', 'Operate hotels', 'Manage assigned hotel operations.', true),
    ('00000000-0000-4001-8000-000000000004', 'bookings.manage', 'Manage bookings', 'Manage hotel booking workflows.', true),
    ('00000000-0000-4001-8000-000000000005', 'bookings.check_in', 'Check in guests', 'Record check-in, checkout, and no-show operations.', true),
    ('00000000-0000-4001-8000-000000000006', 'content.manage', 'Manage content', 'Manage public content and customer contact workflows.', true),
    ('00000000-0000-4001-8000-000000000007', 'reports.view', 'View reports', 'View dashboard, commission, and hotel reports.', true),
    ('00000000-0000-4001-8000-000000000008', 'bookings.create', 'Create bookings', 'Create customer bookings.', true);

insert into api_actions (id, key, http_method, path, description, enabled, is_system) values
    ('00000000-0000-4002-8000-000000000001', 'roles.list', 'GET', '/api/v1/roles', 'List roles.', true, true),
    ('00000000-0000-4002-8000-000000000002', 'roles.assign', 'POST', '/api/v1/roles/assign-to-user', 'Assign roles to accounts.', true, true),
    ('00000000-0000-4002-8000-000000000003', 'permissions.list', 'GET', '/api/v1/permissions', 'List permissions.', true, true),
    ('00000000-0000-4002-8000-000000000004', 'actions.list', 'GET', '/api/v1/actions', 'List API actions.', true, true),
    ('00000000-0000-4002-8000-000000000005', 'hotels.create', 'POST', '/api/v1/hotels', 'Create hotels.', true, true),
    ('00000000-0000-4002-8000-000000000006', 'hotels.manage', 'PATCH', '/api/v1/hotels/{hotelId}', 'Manage hotel details.', true, true),
    ('00000000-0000-4002-8000-000000000007', 'bookings.create', 'POST', '/api/v1/hotels/{hotelId}/bookings', 'Create a customer booking.', true, true),
    ('00000000-0000-4002-8000-000000000008', 'bookings.list.hotel', 'GET', '/api/v1/hotels/{hotelId}/bookings', 'List hotel bookings.', true, true),
    ('00000000-0000-4002-8000-000000000009', 'bookings.status.update', 'PATCH', '/api/v1/hotels/{hotelId}/bookings/{bookingId}/status', 'Update hotel booking status.', true, true),
    ('00000000-0000-4002-8000-000000000010', 'bookings.check_in', 'POST', '/api/v1/hotels/{hotelId}/bookings/{bookingId}/check-in', 'Record booking check-in.', true, true),
    ('00000000-0000-4002-8000-000000000011', 'reports.dashboard', 'GET', '/api/v1/dashboard', 'View platform dashboard.', true, true),
    ('00000000-0000-4002-8000-000000000012', 'content.manage', 'POST', '/api/v1/admin/news', 'Manage public content.', true, true),
    ('00000000-0000-4002-8000-000000000013', 'hotels.status.update', 'PATCH', '/api/v1/hotels/{hotelId}/status', 'Update hotel lifecycle status.', true, true),
    ('00000000-0000-4002-8000-000000000014', 'hotel.members.manage', 'POST', '/api/v1/hotels/{hotelId}/members', 'Manage hotel membership.', true, true),
    ('00000000-0000-4002-8000-000000000015', 'room_types.manage', 'POST', '/api/v1/hotels/{hotelId}/room-types', 'Manage hotel room types.', true, true),
    ('00000000-0000-4002-8000-000000000016', 'rooms.manage', 'POST', '/api/v1/hotels/{hotelId}/rooms', 'Manage hotel physical rooms.', true, true),
    ('00000000-0000-4002-8000-000000000017', 'inventory.manage', 'PUT', '/api/v1/hotels/{hotelId}/room-types/{roomTypeId}/inventory', 'Manage room type inventory.', true, true);

insert into role_permissions (role_id, permission_id)
select r.id, p.id
from roles r
cross join permissions p
where r.name = 'ADMIN';

insert into role_permissions (role_id, permission_id)
select '00000000-0000-4000-8000-000000000002'::uuid, id
from permissions
where key in ('hotels.manage', 'hotels.operate', 'bookings.manage', 'bookings.check_in', 'content.manage', 'reports.view');

insert into role_permissions (role_id, permission_id)
select '00000000-0000-4000-8000-000000000003'::uuid, id
from permissions
where key in ('hotels.operate', 'bookings.manage', 'bookings.check_in', 'content.manage', 'reports.view');

insert into role_permissions (role_id, permission_id)
select '00000000-0000-4000-8000-000000000004'::uuid, id
from permissions
where key in ('bookings.manage', 'bookings.check_in');

insert into role_permissions (role_id, permission_id)
select '00000000-0000-4000-8000-000000000005'::uuid, id
from permissions
where key in ('hotels.operate', 'bookings.manage');

insert into role_permissions (role_id, permission_id)
select '00000000-0000-4000-8000-000000000006'::uuid, id
from permissions
where key = 'bookings.create';

insert into action_policies (id, action_id, permission_id, scope)
select seed.id, a.id, p.id, seed.scope
from (
    values
        ('10000000-0000-4003-8000-000000000001'::uuid, 'actions.list', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000002'::uuid, 'bookings.check_in', 'bookings.check_in', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000003'::uuid, 'bookings.create', 'bookings.create', 'SELF'),
        ('10000000-0000-4003-8000-000000000004'::uuid, 'bookings.list.hotel', 'bookings.manage', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000005'::uuid, 'bookings.status.update', 'bookings.manage', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000006'::uuid, 'content.manage', 'content.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000007'::uuid, 'hotels.create', 'hotels.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000008'::uuid, 'hotels.manage', 'hotels.operate', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000009'::uuid, 'permissions.list', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000010'::uuid, 'reports.dashboard', 'reports.view', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000011'::uuid, 'roles.assign', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000012'::uuid, 'roles.list', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000013'::uuid, 'hotels.status.update', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000014'::uuid, 'hotels.status.update', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000015'::uuid, 'hotel.members.manage', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000016'::uuid, 'hotel.members.manage', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000017'::uuid, 'room_types.manage', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000018'::uuid, 'room_types.manage', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000019'::uuid, 'rooms.manage', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000020'::uuid, 'rooms.manage', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000021'::uuid, 'inventory.manage', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000022'::uuid, 'inventory.manage', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000023'::uuid, 'hotels.manage', 'security.manage', 'GLOBAL')
) as seed(id, action_key, permission_key, scope)
join api_actions a on a.key = seed.action_key
join permissions p on p.key = seed.permission_key;

insert into commission_packages (id, code, name, commission_rate, active, is_system) values
    ('00000000-0000-4004-8000-000000000001', 'STANDARD', 'Standard Commission', 0.1000, true, true),
    ('00000000-0000-4004-8000-000000000002', 'PREMIUM', 'Premium Commission', 0.1500, true, true);

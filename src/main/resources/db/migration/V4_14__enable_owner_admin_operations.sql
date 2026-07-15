-- Extend the restored OWNER role only for the requested administration modules.
-- Historical migrations remain immutable; all inserts are idempotent.

insert into permissions (id, key, name, description, is_system)
values (
    '00000000-0000-4001-8000-000000000009',
    'reviews.manage',
    'Manage hotel reviews',
    'Moderate and remove reviews for manageable hotels.',
    true
)
on conflict (key) do update
set name = excluded.name,
    description = excluded.description,
    is_system = excluded.is_system,
    updated_at = now();

insert into api_actions (id, key, http_method, path, description, enabled, is_system)
values
    (
        '00000000-0000-4002-8000-000000000019',
        'inventory.delete',
        'DELETE',
        '/api/v1/hotels/{hotelId}/room-types/{roomTypeId}/inventory/{inventoryId}',
        'Delete an eligible room type inventory record.',
        true,
        true
    ),
    (
        '00000000-0000-4002-8000-000000000020',
        'reviews.manage',
        'GET',
        '/api/v1/admin/hotels/{hotelId}/reviews',
        'List and moderate reviews for a hotel.',
        true,
        true
    )
on conflict (key) do update
set http_method = excluded.http_method,
    path = excluded.path,
    description = excluded.description,
    enabled = excluded.enabled,
    is_system = excluded.is_system,
    updated_at = now();

insert into role_permissions (role_id, permission_id)
select role.id, permission.id
from roles role
cross join permissions permission
where role.name = 'OWNER'
  and permission.key = 'reviews.manage'
on conflict do nothing;

insert into action_policies (id, action_id, permission_id, scope)
select seed.id, action.id, permission.id, seed.scope
from (
    values
        ('10000000-0000-4003-8000-000000000027'::uuid, 'inventory.delete', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000028'::uuid, 'inventory.delete', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000029'::uuid, 'reviews.manage', 'reviews.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000030'::uuid, 'reviews.manage', 'bookings.manage', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000031'::uuid, 'reviews.manage', 'security.manage', 'GLOBAL')
) as seed(id, action_key, permission_key, scope)
join api_actions action on action.key = seed.action_key
join permissions permission on permission.key = seed.permission_key
on conflict (action_id, permission_id, scope) do nothing;

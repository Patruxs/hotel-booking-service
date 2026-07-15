-- Grant receptionist front-desk actions without granting room lifecycle management.

insert into permissions (id, key, name, description, is_system)
values
    (
        '00000000-0000-4001-8000-000000000010',
        'rooms.view',
        'View physical rooms',
        'View physical rooms in assigned hotels.',
        true
    ),
    (
        '00000000-0000-4001-8000-000000000011',
        'rooms.condition.update',
        'Update room condition',
        'Update physical room condition in assigned hotels.',
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
        '00000000-0000-4002-8000-000000000021',
        'rooms.view',
        'GET',
        '/api/v1/hotels/{hotelId}/rooms',
        'View physical rooms in a hotel.',
        true,
        true
    ),
    (
        '00000000-0000-4002-8000-000000000022',
        'rooms.condition.update',
        'PATCH',
        '/api/v1/hotels/{hotelId}/rooms/{roomId}/condition',
        'Update a physical room condition.',
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
where role.name = 'RECEPTIONIST'
  and permission.key in ('rooms.view', 'rooms.condition.update')
on conflict do nothing;

insert into action_policies (id, action_id, permission_id, scope)
select seed.id, action.id, permission.id, seed.scope
from (
    values
        ('10000000-0000-4003-8000-000000000032'::uuid, 'bookings.create', 'bookings.manage', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000033'::uuid, 'rooms.view', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000034'::uuid, 'rooms.view', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000035'::uuid, 'rooms.view', 'hotels.operate', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000036'::uuid, 'rooms.view', 'rooms.view', 'HOTEL_MEMBER'),
        ('10000000-0000-4003-8000-000000000037'::uuid, 'rooms.condition.update', 'security.manage', 'GLOBAL'),
        ('10000000-0000-4003-8000-000000000038'::uuid, 'rooms.condition.update', 'hotels.manage', 'HOTEL_OWNER'),
        ('10000000-0000-4003-8000-000000000039'::uuid, 'rooms.condition.update', 'rooms.condition.update', 'HOTEL_MEMBER')
) as seed(id, action_key, permission_key, scope)
join api_actions action on action.key = seed.action_key
join permissions permission on permission.key = seed.permission_key
on conflict (action_id, permission_id, scope) do nothing;

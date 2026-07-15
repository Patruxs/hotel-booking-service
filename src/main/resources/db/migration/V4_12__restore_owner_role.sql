-- Restore OWNER as a hotel-scoped authorization role after V4_11 retired it.
-- Existing MANAGER assignments are preserved because their original provenance
-- cannot be recovered after the retirement migration.

insert into roles (id, name, display_name, description, is_system)
values (
    '00000000-0000-4000-8000-000000000002',
    'OWNER',
    'Hotel Owner',
    'Hotel owner with hotel-scoped management actions.',
    true
)
on conflict (name) do update
set display_name = excluded.display_name,
    description = excluded.description,
    is_system = excluded.is_system,
    updated_at = now();

insert into role_permissions (role_id, permission_id)
select role.id, permission.id
from roles role
cross join permissions permission
where role.name = 'OWNER'
  and permission.key in (
      'hotels.manage',
      'hotels.operate',
      'bookings.manage',
      'bookings.check_in',
      'content.manage',
      'reports.view'
  )
on conflict do nothing;

insert into account_roles (account_id, role_id)
select distinct hotel.owner_id, role.id
from hotels hotel
cross join roles role
where role.name = 'OWNER'
on conflict do nothing;

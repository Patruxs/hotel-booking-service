-- Retire OWNER as an authorization role while preserving hotel ownership records.
-- Existing owner accounts become managers and retain their hotel memberships.

insert into account_roles (account_id, role_id)
select ar.account_id, manager.id
from account_roles ar
join roles owner_role on owner_role.id = ar.role_id and owner_role.name = 'OWNER'
cross join roles manager
where manager.name = 'MANAGER'
on conflict do nothing;

insert into role_permissions (role_id, permission_id)
select manager.id, permission.id
from roles manager
cross join permissions permission
where manager.name = 'MANAGER'
  and permission.key in ('hotels.manage', 'hotels.operate', 'bookings.manage', 'bookings.check_in', 'reports.view')
on conflict do nothing;

delete from account_roles
where role_id = (select id from roles where name = 'OWNER');

delete from role_permissions
where role_id = (select id from roles where name = 'OWNER');

delete from roles where name = 'OWNER';

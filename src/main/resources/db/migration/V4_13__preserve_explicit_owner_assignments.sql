-- V4_12 restored OWNER for historical hotel owners before the explicit
-- reassignment policy was adopted. Remove only those transitional grants.
-- The role itself, manager assignments, memberships, and hotel ownership
-- references remain unchanged. Existing deployments must assign OWNER
-- explicitly to the reviewed accounts that should manage each hotel.

delete from account_roles inferred_owner
using roles owner_role, roles manager_role
where inferred_owner.role_id = owner_role.id
  and owner_role.name = 'OWNER'
  and exists (
      select 1
      from account_roles manager_assignment
      where manager_assignment.account_id = inferred_owner.account_id
        and manager_assignment.role_id = manager_role.id
  )
  and exists (
      select 1
      from hotels hotel
      where hotel.owner_id = inferred_owner.account_id
  )
  and manager_role.name = 'MANAGER';

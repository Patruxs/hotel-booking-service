-- Review moderation is an explicit ADMIN or hotel OWNER capability.
-- Booking management must not imply access to review moderation.
delete from action_policies policy
using api_actions action, permissions permission
where policy.action_id = action.id
  and policy.permission_id = permission.id
  and action.key = 'reviews.manage'
  and permission.key = 'bookings.manage'
  and policy.scope = 'HOTEL_MEMBER';

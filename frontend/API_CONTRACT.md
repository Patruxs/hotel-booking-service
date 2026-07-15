# API Contract

This frontend was migrated from the Kinyias `apps/web` source into the Vite React app. The backend for this repository is Spring Boot, not the Kinyias/NestJS backend. The tables below preserve the Kinyias frontend contract vocabulary while documenting which parts are currently backed by Spring and which remain frontend-visible but mock/TODO-safe.

Base URL: `VITE_API_BASE_URL`, default `http://localhost:8080/api/v1`.

Auth transport: `withCredentials: true`, `Authorization: Bearer <accessToken>` from the `accessToken` cookie.

Mock mode: live Spring API mode is the default when env flags are absent. Set
`VITE_USE_MOCKS=true` only for explicit local UI inspection. Set
`VITE_BYPASS_AUTH=true` only for explicit local auth bypass. Production-like
runs use `VITE_USE_MOCKS=false` and `VITE_BYPASS_AUTH=false`.
`mockOnly` adapters now throw outside explicit mock mode so live runs cannot
silently render fixtures.

OWNER administration uses the same live Spring contract as ADMIN for the
requested modules, but hotel-derived requests must target a hotel returned by
`/hotels/manageable`. Global OWNER exceptions are limited to `/amenities` and
`/admin/news`; review moderation remains hotel-qualified under `/admin/hotels`.
MANAGER may update manageable hotel profile fields, but hotel creation and
archive/delete actions are restricted to OWNER and ADMIN and are hidden in the
MANAGER interface.

Active route pages call Kinyias-compatible feature API functions from `src/features/*/api.ts`. Live-mode adapters call Spring requests through `src/lib/axios.ts`; routed pages must not read `mockApi` or fixture modules directly.

Any remaining `mockOnly` adapter is explicit development-only behavior. A route
or action that is reachable from normal public, account, or admin navigation
must be connected to Spring or hidden before it can be treated as live product
surface.

Task group 1 inventory evidence lives in
`docs/stories/FRONTEND-API-001-live-spring-api-integration/route-inventory.md`.
That file maps every public/account/admin reachable route to page, adapter,
Spring endpoint target, auth requirement, and proof target.

Hidden or unsupported user-facing entries:

- `/me/sale` was removed from normal account navigation.
- Mobile "My Offers" was removed because no product route or Spring contract
  exists.
- Login/register Google OAuth buttons were hidden because Spring has no
  completed OAuth contract in this change slice.
- `/auth/callback` remains mounted for direct compatibility but is not linked
  from normal navigation and is excluded from live proof until OAuth is
  implemented.

## Current Spring Boot connection

`VITE_USE_MOCKS=false` connects the migrated frontend to Spring Boot `/api/v1`
for the reachable product surface:

| Frontend feature | Connected Spring Boot endpoint |
|---|---|
| auth and session | `/auth/login`, `/auth/register`, `/auth/logout`, `/auth/refresh`, `/auth/forgot-password`, `/auth/reset-password`, `/auth/verify-email`, `/auth/resend`, `/users/me` |
| public hotels and availability | `/hotels`, `/hotels/:hotelId`, `/hotels/:hotelId/room-types`, `/hotels/:hotelId/room-types/available`, `/hotels/:hotelId/room-types/:roomTypeId`, `/hotels/:hotelId/policies`, `/hotels/:hotelId/reviews`, `/banners`, `/news`, `/contacts` |
| booking and payment | `/hotels/:hotelId/bookings`, `/hotels/:hotelId/bookings/:bookingId`, `/bookings/me`, `/bookings/me/:bookingId`, `/bookings/:bookingId/payments/vnpay` |
| account | `/users/me`, `/users/me/change-password`, `/uploads/avatar`, `/bookings/me`, `/bookings/me/:bookingId`, `/reviews/mine`, `/reviews/:id/mine` |
| owner/admin hotel operations | `/hotels`, `/hotels/manageable`, `/hotels/:hotelId/manage`, `/hotels/:hotelId`, `/hotels/:hotelId/status`, `/hotels/:hotelId/members`, `/hotels/:hotelId/member-candidates`, `/hotels/:hotelId/room-types`, `/hotels/:hotelId/rooms`, `/hotels/:hotelId/rooms/:roomId`, `/hotels/:hotelId/room-types/:roomTypeId/inventory`, `/hotels/:hotelId/room-types/:roomTypeId/inventory/bulk`, and `/hotels/:hotelId/room-types/:roomTypeId/inventory/:inventoryId` delete |
| admin content and commercial | `/admin/news`, `/admin/banners`, `/admin/contacts`, `/notifications`, `/admin/commission-packages`, `/hotels/:hotelId/commission-package/:packageId`, `/admin/hotels/:hotelId/policies`, `/admin/promotions`, `/promotions/public` |
| admin identity and RBAC | `/users`, `/users/:userId`, `/roles`, `/roles/assign-to-user`, `/permissions`, `/actions` |
| media and gallery | `/uploads`, `/uploads/avatar`, `/uploads/avatar` delete, `/upload/db-folders`, `/upload/db-folders/:folderId/images`, `/upload/create-folder`, `/upload/image/:folderName` |
| dashboard and reviews | `/dashboard/stats`, `/dashboard/revenue-chart`, `/dashboard/latest-reviews`, `/dashboard/newest-bookings`, `/admin/hotels/:hotelId/reviews`, `/admin/hotels/:hotelId/reviews/:reviewId/moderation` |

Spring audit notes:

- Spring wraps responses as `{ status, message, data }`; frontend adapters unwrap this envelope.
- Feature adapters map Spring DTO names and pagination shapes into the existing
  frontend view types. Page components consume the mapped view models.
- Spring auth returns an access token and refresh cookie transport. The
  frontend stores the access token, calls `/users/me`, and treats
  `allowedActions` as a visibility hint only.
- Spring refresh is available at `/auth/refresh`. The Axios client performs one
  shared refresh, retries the original request, and clears auth state when
  refresh fails.
- Spring remains authoritative for authorization, booking totals, promotion
  eligibility, usage consumption, payment state, and user-visible errors.
- Fixture fallback is not valid in live mode. Explicit mock mode may return
  source-shaped placeholders for development inspection, but it must not be used
  as product proof.

Kinyias source paths below are migration references only. The active runtime
contract is the Spring `/api/v1` path listed in each table.

## auth

Source: `apps/web/src/features/auth/api.ts`, `apps/web/src/providers/AuthProvider.tsx`, `apps/web/src/lib/axios.ts`.

| Method | Path | Notes |
|---|---|---|
| POST | `/auth/login` | `{ email, password }` -> `{ accessToken, jti, tokenType }` |
| POST | `/auth/logout` | End session |
| POST | `/auth/register` | Register payload |
| POST | `/auth/forgot-password` | `{ email }` |
| POST | `/auth/reset-password` | `{ token, newPassword, confirmPassword }` |
| POST | `/auth/verify-email` | `{ token }` |
| POST | `/auth/resend` | `{ email }` |
| POST | `/auth/refresh` | Refresh access token |
| GET | `/auth/google` | OAuth redirect, TODO-safe in Vite |
| GET | `/auth/sessions` | Defined in constants, backend TODO |

## users

Source: `apps/web/src/features/user/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/users` | Query `UsersQueryParams`, paginated users |
| PATCH | `/users/:id` | Admin partial user update |
| DELETE | `/users/:id` | Delete user |
| GET | `/users/me` | Current user |
| PATCH | `/users/me` | Profile update |
| POST | `/users/me/change-password` | Password change |
| POST | `/uploads/avatar` | Multipart avatar upload |
| DELETE | `/uploads/avatar` | Delete avatar |
| POST | `/roles/assign-to-user` | Assign roles to a user |

## hotels

Source: `apps/web/src/features/hotels/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/hotels` | Admin list, paginated |
| GET | `/hotels/public` | Public list, paginated |
| GET | `/hotels/:id` | Hotel detail |
| POST | `/hotels` | Create hotel |
| PATCH | `/hotels/:id` | Update hotel |
| DELETE | `/hotels/:id` | Delete hotel |
| POST | `/hotels/:hotelId/members` | Add members |
| GET | `/hotels/:hotelId/members` | List members |
| DELETE | `/hotels/:hotelId/members/:userId` | Remove member |

## rooms

Source: `apps/web/src/features/rooms/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/hotels/:hotelId/rooms` | Query `RoomQueryParams` |
| GET | `/hotels/:hotelId/rooms/:roomId` | Room detail |
| POST | `/hotels/:hotelId/rooms` | Create room |
| PATCH | `/hotels/:hotelId/rooms/:roomId` | Update room |
| DELETE | `/hotels/:hotelId/rooms/:roomId` | Delete room |

## room types

Source: `apps/web/src/features/room-types/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/hotels/:hotelId/room-types` | Hotel room types |
| GET | `/room-types` | Query `{ limit }` |
| GET | `/hotels/:hotelId/room-types/available` | Query `{ from, to, page?, limit?, q? }`; `from` inclusive, `to` exclusive; returns `{ data, meta }`, default `limit=20`, ordered by `price_per_night asc`; `availableRooms` is min availability across stay dates or `0` if missing/stop-sell |
| GET | `/hotels/:hotelId/room-types/:id` | Detail |
| POST | `/hotels/:hotelId/room-types` | Create |
| PATCH | `/hotels/:hotelId/room-types/:id` | Update |
| DELETE | `/hotels/:hotelId/room-types/:id` | Delete |

## bookings and payments

Source: `apps/web/src/features/bookings/api.ts`.

| Method | Path | Notes |
|---|---|---|
| POST | `/hotels/:hotelId/bookings` | Authenticated create booking; server calculates totals and ignores client `totalAmount` if present |
| GET | `/hotels/:hotelId/bookings` | Hotel bookings; query supports `status`, `from`, `to`, `page`, `offset`, `limit`, and `q`; `offset` wins over `page`; ordered by `createdAt desc`; `limit` defaults to `10` and caps at `100` |
| GET | `/hotels/:hotelId/bookings/:bookingId` | Booking detail; queries by both `hotelId` and `bookingId`; cross-hotel mismatch returns `404 Booking not found` |
| PATCH | `/hotels/:hotelId/bookings/:bookingId/status` | Domain status update for `CANCELLED`, `CHECKED_IN`, `COMPLETED`, or `NO_SHOW`; checkout uses `{ status: "COMPLETED" }`, no-show uses `{ status: "NO_SHOW" }`; queries by both `hotelId` and `bookingId`; returns updated booking DTO; `COMPLETED` includes `checkedOutAt`; `NO_SHOW` has no `noShowAt` |
| PATCH | `/hotels/:hotelId/bookings/:bookingId/cancel` | Staff/admin cancel eligible bookings; queries by both `hotelId` and `bookingId`; customer cancellation is limited to owned unpaid `PENDING` bookings; does not accept/store cancellation reason in first migration; returns updated booking DTO with `status: "CANCELLED"` |
| GET | `/bookings/me` | My bookings; query supports `status`, `page`, `offset`, and `limit`; `offset` wins over `page`; ordered by `createdAt desc`; `limit` defaults to `10` and caps at `100` |
| GET | `/bookings/me/:bookingId` | My booking detail; queries by both `bookingId` and current `userId`; non-owned mismatch returns `404 Booking not found` |
| PATCH | `/bookings/me/:bookingId/cancel` | Customer cancel owned unpaid `PENDING` booking; queries by both `bookingId` and current `userId`; returns updated booking DTO |
| POST | `/bookings/:bookingId/payments/vnpay` | Create or reuse VNPAY payment URL; Vite sends no body by default; optional `{ locale, bankCode }` is accepted for future bank selection; returns `{ paymentId, merchantTxnRef, paymentUrl }` |
| GET | Payment result redirect | VNPAY return keeps query params `payment_status` and `booking_id`; `success` only when booking is confirmed, `failed` for normal provider failure, `requires_review` for `LATE_SUCCEEDED` such as success after customer cancellation |
| POST | `/hotels/:hotelId/bookings/:bookingId/check-in` | Canonical check-in mutation; queries by both `hotelId` and `bookingId`; cross-hotel mismatch returns `404 Booking not found`; returns updated booking DTO with `status: "CHECKED_IN"` and latest check-in summary/detail |
| POST | `/bookings/:bookingId/check-in` | Temporary source/Vite compatibility alias for check-in mutation; resolves booking first, then enforces the same hotel owner/member/admin access checks |
| GET | `/hotels/:hotelId/bookings/:bookingId/check-in` | Canonical full check-in detail and guest list; queries by both `hotelId` and `bookingId`; cross-hotel mismatch returns `404 Booking not found` |
| GET | `/bookings/:bookingId/check-in` | Temporary source/Vite compatibility alias for full check-in detail; resolves booking first, then enforces the same hotel owner/member/admin access checks |

Booking DTO notes:
- `checkedOutAt` is top-level when populated.
- Vite booking creation should call canonical `POST /hotels/:hotelId/bookings`; Spring treats the route `hotelId` as the authority for hotel, room type, inventory, and active-hotel validation even if the DTO still contains `hotelId` during adapter migration.
- After successful customer booking creation, Vite should immediately call `POST /bookings/:bookingId/payments/vnpay` and redirect to the returned `paymentUrl`; it should not show the pending booking as confirmed or send the user home.
- Hotel/admin booking detail, status, cancel, check-in, checkout, and no-show operations return `404 Booking not found` when the booking does not belong to the route `hotelId`.
- Vite hotel/admin booking list and detail adapters should call canonical `GET /hotels/:hotelId/bookings` and `GET /hotels/:hotelId/bookings/:bookingId`; global `/bookings/all` plus client-side filtering is not a valid target for the migrated authorization model.
- Canonical check-in mutation is `POST /hotels/:hotelId/bookings/:bookingId/check-in`; `POST /bookings/:bookingId/check-in` remains a temporary compatibility alias.
- Vite check-in adapters should call the canonical hotel-scoped mutation/detail routes; the global `/bookings/:bookingId/check-in` routes are backend-only temporary aliases for older/source-shaped callers.
- Check-in mutation payload is source/Vite-compatible `{ note?, primary, companions? }`; `primary` is required, `companions` defaults to `[]`, `note` is trimmed, stored blank as `null`, and capped at 1000 characters, guest fields are trimmed, optional guest `userId` must reference an existing user when provided, and total guests cannot exceed `sum(items.quantity * roomType.max_guests)`. Guest `userId` is not an authorization source.
- Check-in edits replace the full guest list by deleting and recreating `booking_guests` in submitted order; guest row IDs are not stable across edits.
- Check-in guests require only `fullName`; optional strings are trimmed and stored as `null` when blank; non-blank `email` must be valid, `gender` must be `MALE`, `FEMALE`, or `OTHER`, and `dateOfBirth` must be `yyyy-MM-dd`.
- Check-in detail returns `{ checkIn, guests }` with the primary guest first, then companions in submitted order; `isPrimary` and `sortOrder` are internal in the first migration.
- Check-in detail keeps `checkIn.checkedInBy` as the source-compatible user ID string and adds detail-only `checkIn.checkedInByUser: { id, email, firstName, lastName }`.
- Canonical check-in detail is `GET /hotels/:hotelId/bookings/:bookingId/check-in`; `GET /bookings/:bookingId/check-in` remains a temporary compatibility alias.
- Checkout and no-show do not have dedicated routes in the first migration; they stay behind `PATCH /hotels/:hotelId/bookings/:bookingId/status`.
- Vite status adapters should call canonical `PATCH /hotels/:hotelId/bookings/:bookingId/status`; any global `/bookings/update/:bookingId` route is a legacy/current-Spring compatibility concern, not the migrated frontend target.
- Customer-facing booking detail and customer booking actions return `404 Booking not found` when the booking does not belong to the current user.
- Vite customer booking adapters should call canonical `GET /bookings/me` and `GET /bookings/me/:bookingId`; `/users/get-user-bookings` is a legacy/current-Spring compatibility concern, not the migrated frontend target.
- Vite customer cancellation should call canonical `PATCH /bookings/me/:bookingId/cancel`; hotel/admin cancellation stays on `PATCH /hotels/:hotelId/bookings/:bookingId/cancel`.
- Cancellation reason is deferred; first-migration cancellation endpoints and Vite adapters should not send, accept, or silently ignore `reason`. Vite should consume the updated booking DTO from the appropriate customer or hotel/admin cancel route. Future support should add explicit `cancellationReason`.
- Vite exports the correctly spelled `cancelBooking`; the old misspelled `cancleBooking` export is a temporary compatibility alias only.
- Paginated booking lists do not include nested `checkIn`.
- Hotel/admin booking list `q` searches `guestName`, `guestEmail`, and `guestPhone` case-insensitively.
- Customer-facing `/bookings/me` does not support `from`, `to`, or `q` in the first migration.
- Booking lists do not support `sortBy` or `order` in the first migration.
- Booking detail and mutation responses may expose only compact nested `checkIn: { id, checkedInBy, checkedInAt, note, guestCount }`, not the full guest list and not duplicated as top-level `checkedInAt` or `checkedInBy`.
- `checkIn.guestCount` is derived from `booking_guests` for projections, not stored as independent check-in state.
- Full `{ checkIn, guests }` data and `checkedInByUser` are returned only by the dedicated check-in detail routes.
- Booking lists and details include compact `hotel: { id, name, address }` plus `hotelId`; they do not include full hotel images, policies, reviews, or amenities.
- Booking lists and details include compact `items[]`: `id`, `roomTypeId`, `quantity`, `unitPrice`, `lineTotal`, and `roomType: { id, name }`; booking item projections do not include room type images or amenities.
- Customer-facing `/bookings/me` responses expose `userId` plus guest/contact fields only; they do not include nested `user`.
- Hotel/admin booking lists and details include compact `user: { id, email, firstName, lastName }` plus `userId`.
- Booking contact fields stay flat as `guestName`, `guestEmail`, and `guestPhone` on create, list, detail, and mutation responses; all three are required on booking creation.
- `guestPhone` validation is intentionally loose: trim, require non-blank, max 30 characters, reject control characters, and do not enforce a country-specific phone regex.
- Booking notes use source-compatible `note`; Spring trims it, stores blank as `null`, caps it at 1000 characters, and does not introduce `specialRequests` in the first migration.
- Vite booking creation should trim `guestName`, `guestEmail`, `guestPhone`, and `note` before sending; Spring remains the authoritative validator/trimmer.
- Vite booking creation should omit `promotionCode` when the form value is blank after trimming, rather than sending an empty string.
- Booking lists and details expose only stable discount snapshot fields: `promotionId`, `promotionCode`, and `discountAmount`; they do not embed the full promotion object.
- Customer-facing `/bookings/me` responses do not expose commission fields.
- Hotel/admin booking detail and report-oriented DTOs expose stable commission snapshot fields: `commissionRate`, `commissionAmount`, and `commissionPackageCode`.
- Booking lists and details include a compact safe `payments[]` projection: `id`, `bookingId`, `amount`, `method`, `provider`, `status`, `merchantTxnRef`, `vnpTransactionNo`, `bankCode`, `responseCode`, `transactionStatus`, `payDate`, `createdAt`, and `updatedAt`.
- Booking DTOs never include `paymentUrl`; use `POST /bookings/:bookingId/payments/vnpay` for a fresh or reusable live payment URL.
- Vite payment creation adapters should call canonical `POST /bookings/:bookingId/payments/vnpay`; mock mode may return a source-shaped placeholder DTO, but it must not fake a successful provider payment.
- Vite payment creation should send no request body in the first migration. Spring defaults missing `locale` to `vn`; `bankCode` is only sent when the UI adds an explicit bank selector.
- Vite payment result pages must render `payment_status=requires_review` as a distinct review state and must not show normal success, normal failure, or retry-payment messaging for that state.

## amenities

Source: `apps/web/src/features/amentites/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/amenities` | Query `AmenitiesQueryParams` |
| GET | `/amenities/:id` | Detail |
| POST | `/amenities` | Create |
| PUT | `/amenities/:id` | Update |
| DELETE | `/amenities/:id` | Disable |

Amenity `key` is the unique business identifier. `iconKey` is optional reusable presentation metadata containing a Lucide icon export name. Create and update requests must not place the selected icon in `key`; updates that omit `iconKey` preserve its current value.

## banners

Source: `apps/web/src/features/banner/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/banners` | Public banners |
| GET | `/admin/banners` | Admin banners |
| POST | `/admin/banners` | Create |
| PATCH | `/admin/banners/:id` | Update |
| DELETE | `/admin/banners/:id` | Delete |

## news

Source: `apps/web/src/features/news/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/admin/news` | Admin list |
| GET | `/admin/news/:id` | Admin detail |
| POST | `/admin/news` | Create |
| PUT/PATCH | `/admin/news/:id` | Update |
| DELETE | `/admin/news/:id` | Delete |
| GET | `/news` | Public list |
| GET | `/news/:slug` | Public detail |

## policies

Source: `apps/web/src/features/policies/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/admin/hotels/:hotelId/policies` | Admin policies |
| GET | `/admin/hotels/:hotelId/policies/:policyId` | Detail |
| POST | `/admin/hotels/:hotelId/policies` | Create |
| PATCH | `/admin/hotels/:hotelId/policies/:policyId` | Update |
| DELETE | `/admin/hotels/:hotelId/policies/:policyId` | Delete |
| GET | `/hotels/:hotelId/policies` | Public hotel policies |

## promotions

Source: `frontend/src/features/promotion/api.ts`.

Live-mode behavior:

- Admin promotion list/detail/create/update/delete use Spring `/api/v1/admin/promotions`.
- Public checkout promotion search uses Spring `/api/v1/promotions/public`.
- Public lookup/search returns currently active promotions whose `hotel_id` is either `null` for global eligibility or matches the supplied `hotelId`.
- Public promotion responses are preview data only. Booking creation remains final authority for discount amount, usage limits, per-user limits, usage consumption, and rejection reasons.
- The frontend maps Spring `maxDiscount`/`startsAt`/`endsAt` style fields into the existing `maxDiscountAmount`/`startAt`/`endAt` view model. `description` remains `null` because the current `promotions` table has no description column.

| Method | Path | Notes |
|---|---|---|
| GET | `/admin/promotions` | Admin list with `search`, `hotelId`, `isActive`, `page`, and `limit` |
| GET | `/admin/promotions/:id` | Admin detail |
| POST | `/admin/promotions` | Create |
| PATCH | `/admin/promotions/:id` | Update |
| DELETE | `/admin/promotions/:id` | Delete |
| GET | `/promotions/public` | Public search with `search`, `hotelId`, `subtotal`, and `limit` |
| GET | `/promotions/public/:code` | Public code lookup with `hotelId` and `subtotal` |

## reviews

Source: `apps/web/src/features/reviews/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/hotels/:hotelId/reviews` | Public reviews |
| GET | `/admin/hotels/:hotelId/reviews` | OWNER/ADMIN/MANAGER moderation list for a manageable hotel |
| POST | `/hotels/:hotelId/reviews` | Create review |
| PATCH | `/admin/hotels/:hotelId/reviews/:id/moderation` | OWNER/ADMIN/MANAGER hide/show |
| DELETE | `/admin/hotels/:hotelId/reviews/:id` | OWNER/ADMIN/MANAGER delete |
| GET | `/reviews/mine` | Customer-owned review list |
| PATCH | `/reviews/:id/mine` | Customer-owned review update; maps frontend `content` to Spring `comment` |

## dashboard

Source: `apps/web/src/features/dashboard/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/dashboard/stats` | Optional `hotelId` |
| GET | `/dashboard/revenue-chart` | Revenue chart params |
| GET | `/dashboard/latest-reviews` | Optional `hotelId`, `limit` |
| GET | `/dashboard/newest-bookings` | Optional `hotelId`, `limit` |

## inventory

Source: `apps/web/src/features/inventory/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/hotels/:hotelId/room-types/:roomTypeId/inventory` | Inventory query with optional `from` and `to` |
| PUT | `/hotels/:hotelId/room-types/:roomTypeId/inventory` | Upsert date-range inventory for a room type |
| PUT | `/hotels/:hotelId/room-types/:roomTypeId/inventory/bulk` | Transactional inclusive date-range bulk upsert |
| DELETE | `/hotels/:hotelId/room-types/:roomTypeId/inventory/:inventoryId` | Delete only future records with no reserved or consumed capacity; conflicts otherwise |

## contacts

Source: `apps/web/src/features/contact/api.ts`.

| Method | Path | Notes |
|---|---|---|
| POST | `/contact` | Public contact |
| GET | `/admin/contacts` | Admin contacts |
| GET | `/admin/contacts/:id` | Detail |
| PATCH | `/admin/contacts/:id` | Update |

## commissions

Source: `apps/web/src/features/commissions/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/admin/commission-packages` | List |
| GET | `/admin/commission-packages/:id` | Detail |
| POST | `/admin/commission-packages` | Create |
| PATCH | `/admin/commission-packages/:id` | Update |
| PATCH | `/admin/commission-packages/:id/deactivate` | Deactivate |
| PUT | `/hotels/:hotelId/commission-package/:packageId` | Assign package |
| GET | `/admin/commission-packages/revenue/chart` | Revenue chart |

## permissions, roles, actions

Sources: `apps/web/src/features/permissions/api.ts`, `apps/web/src/features/roles/api.ts`, `apps/web/src/features/actions/api.ts`.

| Feature | Method | Path | Notes |
|---|---|---|---|
| permissions | GET | `/permissions` | List |
| permissions | POST | `/permissions` | Create |
| permissions | PATCH | `/permissions/:id` | Update |
| permissions | DELETE | `/permissions/:id` | Delete |
| roles | GET | `/roles` | List |
| roles | POST | `/roles` | Create |
| roles | PATCH | `/roles/:id` | Update |
| roles | POST | `/roles/:id/permissions` | Assign permissions |
| roles | DELETE | `/roles/:id` | Delete |
| actions | GET | `/actions` | List |
| actions | PATCH | `/actions/:actionId/permissions` | Assign permissions |

## gallery/upload

Source: `apps/web/src/features/gallery/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/upload/db-folders` | Gallery folders |
| GET | `/upload/db-folders/:folderId/images` | Images |
| POST | `/upload/create-folder` | `{ folderName }` |
| POST | `/upload/image/:folderName` | Multipart image upload |

## notifications

Source: `apps/web/src/features/notifications/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/notifications` | Query `{ page, limit }` |
| GET | `/notifications/unread-count` | Unread count |
| PATCH | `/notifications/:id/read` | Mark read |
| PATCH | `/notifications/read-all` | Mark all read |
| DELETE | `/notifications/:id` | Delete |

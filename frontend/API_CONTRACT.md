# API Contract

This frontend was migrated from the Kinyias `apps/web` source into the Vite React app. The backend for this repository is Spring Boot, not the Kinyias/NestJS backend. The tables below preserve the Kinyias frontend contract vocabulary while documenting which parts are currently backed by Spring and which remain frontend-visible but mock/TODO-safe.

Base URL: `VITE_API_BASE_URL`, default `http://localhost:8080/api/v1`.

Auth transport: `withCredentials: true`, `Authorization: Bearer <accessToken>` from the `accessToken` cookie.

Mock mode: all groups below are currently represented by fixture data when `VITE_USE_MOCKS=true`. Unsupported Kinyias-era modules use `mockOnly` adapters and do not call missing Spring endpoints even when `VITE_USE_MOCKS=false`.

Active route pages now call Kinyias-compatible feature API functions from `src/features/*/api.ts`. Spring-supported adapters decide between fixture data and Spring requests through `VITE_USE_MOCKS`, so routed pages should not read `mockApi` directly.

Unsupported adapters are deliberately `mockOnly`/TODO-safe. They return fixtures regardless of `VITE_USE_MOCKS` until matching Spring controllers exist.

## Current Spring Boot connection

`VITE_USE_MOCKS=false` now connects the migrated frontend to the existing Spring Boot API where an equivalent endpoint already exists:

| Frontend feature | Connected Spring Boot endpoint |
|---|---|
| auth login/register/logout | `/auth/login`, `/auth/register`, `/auth/logout` |
| public/admin hotels | `/hotels/all`, `/hotels/:hotelId`, `/hotels/search` |
| hotel rooms | `/hotels/:hotelId/rooms` |
| rooms | `/rooms/all`, `/rooms/:roomId`, `/rooms/types`, `/rooms/all-available-rooms` |
| bookings | Hotel create/list/detail/status/cancellation targets are under `/hotels/:hotelId/bookings`; customer history uses `/bookings/me` |
| account/users | `/users/all`, `/users/get-logged-in-profile-info`, `/users/update`, `/users/change-password`; customer bookings use `/bookings/me` |
| amenities | `/amenities/all`, `/amenities/:id`, `/amenities/create`, `/amenities/update/:id` |

Spring audit notes:

- Spring wraps responses as `{ status, message, data }`; frontend adapters unwrap this envelope.
- Spring auth returns `data.token`, `role`, `expirationTime`, and `isActive`; the frontend maps this into `accessToken` plus frontend user state.
- Spring currently has no refresh-token endpoint.
- Spring has revenue endpoints at `/revenue/yearly` and `/revenue/date-range`, but no complete Kinyias dashboard endpoint surface.
- Spring physical-room endpoints exist under `/physical-rooms/*`, but browser `PATCH` may need CORS follow-up.
- News, banners, policies, promotions, contacts, commissions, gallery/upload, notifications, permissions, roles, actions, inventory, payments, and review controllers are absent or incomplete in Spring and remain mock/TODO-safe.
- Migrated Kinyias admin pages for these unsupported modules remain visible in the SPA; their adapter functions deliberately return fixture data and do not call missing Spring controllers.

Kinyias modules without current Spring Boot equivalents remain documented below as TODOs for future Spring Boot implementation. Their listed Kinyias paths are contract references, not active NestJS runtime dependencies.

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
| PUT | `/users/:id` | Partial user update |
| DELETE | `/users/:id` | Delete user |
| GET | `/users/me` | Current user |
| PATCH | `/users/me` | Profile update |
| POST | `/users/me/change-password` | Password change |
| POST | `/users/me/avatar` | Multipart avatar upload |
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
| PATCH | `/amenities/:id` | Update |

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
| GET | `/admin/news/id/:id` | Admin detail |
| POST | `/admin/news` | Create |
| PATCH | `/admin/news/:id` | Update |
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

Source: `apps/web/src/features/promotion/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/promotions` | Admin list |
| GET | `/promotions/public` | Public list |
| GET | `/promotions/:id` | Detail |
| POST | `/promotions` | Create |
| PATCH | `/promotions/:id` | Update |
| DELETE | `/promotions/:id` | Delete |

## reviews

Source: `apps/web/src/features/reviews/api.ts`.

| Method | Path | Notes |
|---|---|---|
| GET | `/hotels/:hotelId/reviews` | Public reviews |
| GET | `/hotels/:hotelId/reviews/moderation` | Moderation list |
| POST | `/hotels/:hotelId/reviews` | Create review |
| PATCH | `/hotels/:hotelId/reviews/:id/moderate` | Hide/show |
| DELETE | `/hotels/:hotelId/reviews/:id` | Delete |
| GET | `/users/me/reviews` | Source omitted leading slash |
| PATCH | `/reviews/:id` | Source omitted leading slash |

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
| GET | `/hotels/:hotelId/inventories` | Inventory query |
| POST | `/hotels/:hotelId/inventories/bulk` | Bulk set |
| PATCH | `/hotels/:hotelId/inventories/:id` | Update |

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
| PATCH | `/admin/commission-packages/:hotelId/commission-package` | Assign package |
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

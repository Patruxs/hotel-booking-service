# API Contract

Base URL: `VITE_API_BASE_URL`, default `http://localhost:8080/api/v1`.

Auth transport: `withCredentials: true`, `Authorization: Bearer <accessToken>` from the `accessToken` cookie.

Mock mode: all groups below are currently represented by fixture data when `VITE_USE_MOCKS=true`; Spring Boot should implement the same paths under `/api/v1`.

## Current Spring Boot connection

`VITE_USE_MOCKS=false` now connects the migrated frontend to the existing Spring Boot API where an equivalent endpoint already exists:

| Frontend feature | Connected Spring Boot endpoint |
|---|---|
| auth login/register/logout | `/auth/login`, `/auth/register`, `/auth/logout` |
| public/admin hotels | `/hotels/all`, `/hotels/:hotelId`, `/hotels/search` |
| hotel rooms | `/hotels/:hotelId/rooms` |
| rooms | `/rooms/all`, `/rooms/:roomId`, `/rooms/types`, `/rooms/all-available-rooms` |
| bookings | `/bookings/all`, `/bookings/create`, `/bookings/update/:bookingId`, `/bookings/cancel/:bookingId` |
| account/users | `/users/all`, `/users/get-logged-in-profile-info`, `/users/get-user-bookings`, `/users/update`, `/users/change-password` |
| amenities | `/amenities/all`, `/amenities/:id`, `/amenities/create`, `/amenities/update/:id` |

Kinyias modules without current Spring Boot equivalents remain documented below as TODOs for backend implementation.

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
| GET | `/hotels/:hotelId/room-types/available` | Availability query |
| GET | `/hotels/:hotelId/room-types/:id` | Detail |
| POST | `/hotels/:hotelId/room-types` | Create |
| PATCH | `/hotels/:hotelId/room-types/:id` | Update |
| DELETE | `/hotels/:hotelId/room-types/:id` | Delete |

## bookings and payments

Source: `apps/web/src/features/bookings/api.ts`.

| Method | Path | Notes |
|---|---|---|
| POST | `/hotels/:hotelId/bookings` | Create booking |
| GET | `/hotels/:hotelId/bookings` | Hotel bookings |
| GET | `/hotels/:hotelId/bookings/:bookingId` | Booking detail |
| PATCH | `/hotels/:hotelId/bookings/:bookingId/status` | Update status |
| PATCH | `/hotels/:hotelId/bookings/:bookingId/cancel` | Cancel booking |
| GET | `/bookings/me` | My bookings |
| GET | `/bookings/me/:bookingId` | My booking detail |
| POST | `/bookings/:bookingId/payments/vnpay` | `{ locale: "vn", bankCode: "NCB" }` |
| POST | `/bookings/:bookingId/check-in` | Check-in payload |
| GET | `/bookings/:bookingId/check-in` | Check-in detail |

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

# Migration Notes

## Migrated

- Created `frontend/` as a Vite + React + TypeScript SPA.
- Preserved the `@/` alias to `frontend/src`.
- Added React Router route coverage for all required routes from `SPEC.md`, plus source routes `/admin/policies`, `/admin/reviews`, and `/admin/room-types`.
- Added public, account, and admin layout shells corresponding to the old public, `/me`, and dashboard layouts.
- Added `AuthProvider`, `PermissionProvider`, `Can`, and `useActionGuard` concepts with `VITE_BYPASS_AUTH=true` support.
- Added Axios client in `src/lib/axios.ts` using `VITE_API_BASE_URL`, `withCredentials`, and `accessToken` bearer header.
- Added simple fixture mock system under `src/mocks`.
- Recreated source-compatible `constants`, `types`, `utils`, `layouts`, and feature `api.ts` boundaries for the old app modules.
- Copied available text SVG assets from `apps/web/public`.

## Next.js APIs replaced

- `next/link` -> `Link` and `NavLink` from `react-router-dom`.
- `next/navigation` -> React Router hooks and route params.
- `next/image` -> local `AppImage` wrapper using `<img>`.
- Next App Router layouts -> `PublicLayout`, `AccountLayout`, `AdminLayout`.
- `next.config.ts`, `proxy.ts`, `next-env.d.ts`, `next`, and `eslint-config-next` are not used in the new frontend.

## Route map

Required routes implemented:

`/`, `/login`, `/register`, `/forgot-password`, `/auth/callback`, `/auth/reset`, `/auth/verify-email`, `/hotels`, `/hotels/:hotelId`, `/booking`, `/payment-result`, `/contact`, `/news`, `/news/:newsId`, `/partner`, `/me`, `/me/my-bookings`, `/me/my-bookings/:bookingId`, `/me/my-reviews`, `/admin`, `/admin/dashboard/:hotelId`, `/admin/hotels`, `/admin/hotels/:id`, `/admin/member-hotels`, `/admin/bookings`, `/admin/bookings/:hotelId`, `/admin/bookings/:hotelId/booking/:bookingId`, `/admin/amenities`, `/admin/amenities/:id`, `/admin/users`, `/admin/users/roles`, `/admin/users/permissions`, `/admin/users/actions`, `/admin/room-types/:hotelId`, `/admin/room-types/:hotelId/manage/:typeId`, `/admin/room-types/:hotelId/manage/:typeId/room/:roomId`, `/admin/inventory`, `/admin/news`, `/admin/news/:newsId`, `/admin/policies/:hotelId`, `/admin/policies/:hotelId/policy/:policyId`, `/admin/promotions`, `/admin/promotions/:id`, `/admin/reviews/:hotelId`, `/admin/contacts`, `/admin/contacts/:contactId`, `/admin/commissions`, `/admin/commissions/:commissionId`, `/admin/commissions/hotels`, `/admin/settings`, `/editor`, `/simple`, `/403`, `/forbidden`, `*`.

## Mocked modules

Mock data exists for users, hotels, bookings, rooms, amenities, banners, promotions, policies, news, reviews, inventory, commissions, contacts, permissions, roles, and actions.

When `VITE_USE_MOCKS=false`, supported feature API functions call the current Spring Boot endpoints directly. Some Kinyias-era modules still have no backend equivalent in this Spring Boot app and remain TODO-safe.

Currently connected Spring Boot modules:

- auth: `/auth/login`, `/auth/register`, `/auth/logout`
- hotels: `/hotels/all`, `/hotels/:id`, `/hotels/search`, `/hotels/:hotelId/rooms`
- rooms: `/rooms/all`, `/rooms/:roomId`, `/rooms/types`, `/rooms/all-available-rooms`
- bookings: `/bookings/all`, `/bookings/create`, `/bookings/update/:bookingId`, `/bookings/cancel/:bookingId`
- users: `/users/all`, `/users/get-logged-in-profile-info`, `/users/get-user-bookings`
- amenities: `/amenities/all`, `/amenities/:id`

## Binary assets

Repomix provided only:

- `file.svg`
- `globe.svg`
- `next.svg`
- `vercel.svg`
- `window.svg`

No real hotel/gallery/banner binary images or fonts were present in the XML. The migrated app uses those SVGs as safe placeholders.

## Manual review

- Pixel-perfect component parity is not complete. The old complex form/table/editor components from `apps/web/src/components` and `apps/web/src/features` were represented by Vite-safe module boundaries and mock-renderable route surfaces, not line-for-line copied.
- OAuth callback is TODO-safe because backend OAuth completion is not implemented in this phase.
- TipTap editor routes are backed by a Vite-safe `@tiptap/react` editor. The large old custom toolbar/component tree can still be reattached later for pixel parity.

## Commands

```bash
cd frontend
npm install
npm run dev
npm run typecheck
npm run build
```

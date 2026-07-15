# Migration Notes

## Migrated

- Migrated the Kinyias frontend from Repomix `apps/web` into `frontend/` as a Vite + React + TypeScript SPA.
- The backend for this repository remains Spring Boot. No NestJS backend code, Prisma, Redis, Meilisearch, or backend infrastructure was migrated.
- Preserved the `@/` alias to `frontend/src`.
- Added React Router route coverage for all required routes from `SPEC.md`, plus source routes `/admin/policies`, `/admin/reviews`, and `/admin/room-types`.
- Added public, account, and admin layout shells corresponding to the old public, `/me`, and dashboard layouts.
- Added `AuthProvider`, `PermissionProvider`, `Can`, and `useActionGuard` concepts with `VITE_BYPASS_AUTH=true` support.
- Added Axios client in `src/lib/axios.ts` using `VITE_API_BASE_URL`, `withCredentials`, and `accessToken` bearer header.
- Added simple fixture mock system under `src/mocks`.
- Recreated source-compatible `constants`, `types`, `utils`, `layouts`, and feature `api.ts` boundaries for the old app modules.
- Migrated Kinyias shared UI and feature source files from `apps/web/src/components`, `apps/web/src/features`, `apps/web/src/hooks`, `apps/web/src/lib/tiptap-utils.ts`, and `apps/web/src/styles`.
- Migrated shadcn/Radix primitives under `src/components/ui`.
- Migrated the Kinyias public `Header`/`Footer`, `AdminHeader`, `AdminSidebar`, `AccountSidebar`, dashboard widgets, section components, feature forms/tables, gallery UI, and TipTap UI/editor stack.
- The active route shell now uses the migrated Kinyias public/admin/account layout components.
- Replaced the Vite scaffold route bodies with copied Kinyias page compositions under `src/pages/kinyias`.
- Removed the unused scaffold page modules `src/pages/PublicPages.tsx`, `src/pages/AccountPages.tsx`, and `src/pages/AdminPages.tsx`.
- Replaced routed admin `AdminResourcePage` placeholders with module-specific Kinyias admin pages for amenities, users, roles, permissions, actions, room types, rooms, inventory, news, policies, promotions, reviews, contacts, commissions, and settings.
- Added Kinyias-compatible named API wrapper exports on each feature adapter so active migrated pages call feature APIs instead of reading `mockApi` directly.
- Updated migrated route pages to read React Router camelCase params such as `hotelId`, `bookingId`, `newsId`, `contactId`, `commissionId`, `policyId`, `typeId`, and `roomId`.
- Removed the temporary snake_case compatibility aliases from `src/hooks/navigation.ts`.
- Live Spring API mode is now the default when env flags are absent.
  Explicit local inspection requires `VITE_USE_MOCKS=true` and auth bypass
  requires `VITE_BYPASS_AUTH=true`.
- Unsupported Spring modules use `mockOnly`/TODO-safe feature adapters only in
  explicit mock mode. In live mode, `mockOnly` throws so missing contracts are
  visible instead of silently returning fixtures.
- Removed the broken `/me/sale` account dropdown link and the mobile "My
  Offers" entry because no product route or Spring contract exists.
- Hid Google OAuth buttons from login/register until Spring exposes a completed
  OAuth contract.
- Added task group 1 route inventory and guardrail evidence at
  `docs/stories/FRONTEND-API-001-live-spring-api-integration/route-inventory.md`.
- Removed `// @ts-nocheck` from active routed Kinyias pages, shared API adapter utilities, and feature `api.ts` adapter files. The feature API objects are still compatibility-typed at the adapter boundary while the copied Kinyias DTO contracts are reconciled with Spring DTOs.
- Reduced remaining suppressions from 324 to 254 by also checking active auth, booking, review, user, hotel, room, room-type, banner, contact, news, policy, promotion, inventory, commission feature components plus active layout, section, dashboard, booking-filter, and icon components.
- Copied available text SVG assets from `apps/web/public`.

## Next.js APIs replaced

- `next/link` -> `Link` and `NavLink` from `react-router-dom`.
- `next/navigation` -> React Router hooks and route params.
- `next/image` -> local `AppImage` wrapper using `<img>`.
- `next/dynamic` -> React `lazy`/`Suspense` in `EditorClient`.
- Next App Router layouts -> `PublicLayout`, `AccountLayout`, `AdminLayout`.
- `next.config.ts`, `proxy.ts`, `next-env.d.ts`, `next`, and `eslint-config-next` are not used in the new frontend.

## Route map

Required routes implemented with React Router and migrated Kinyias page bodies:

`/`, `/login`, `/register`, `/forgot-password`, `/auth/callback`, `/auth/reset`, `/auth/verify-email`, `/hotels`, `/hotels/:hotelId`, `/booking`, `/payment-result`, `/contact`, `/news`, `/news/:newsId`, `/partner`, `/me`, `/me/my-bookings`, `/me/my-bookings/:bookingId`, `/me/my-reviews`, `/admin`, `/admin/dashboard/:hotelId`, `/admin/hotels`, `/admin/hotels/:id`, `/admin/member-hotels`, `/admin/bookings`, `/admin/bookings/:hotelId`, `/admin/bookings/:hotelId/booking/:bookingId`, `/admin/amenities`, `/admin/amenities/:id`, `/admin/users`, `/admin/users/roles`, `/admin/users/permissions`, `/admin/users/actions`, `/admin/room-types/:hotelId`, `/admin/room-types/:hotelId/manage/:typeId`, `/admin/room-types/:hotelId/manage/:typeId/room/:roomId`, `/admin/inventory`, `/admin/news`, `/admin/news/:newsId`, `/admin/policies/:hotelId`, `/admin/policies/:hotelId/policy/:policyId`, `/admin/promotions`, `/admin/promotions/:id`, `/admin/reviews/:hotelId`, `/admin/contacts`, `/admin/contacts/:contactId`, `/admin/commissions`, `/admin/commissions/:commissionId`, `/admin/commissions/hotels`, `/admin/settings`, `/editor`, `/simple`, `/403`, `/forbidden`, `*`.

## Live and mock-mode modules

Mock data exists for users, hotels, bookings, rooms, amenities, banners, promotions, policies, news, reviews, inventory, commissions, contacts, permissions, roles, and actions.

When `VITE_USE_MOCKS=false`, reachable public, account, and admin product flows
call Spring Boot endpoints directly through `src/features/*/api.ts`. Feature
adapters unwrap Spring `{ status, message, data }` responses and map Spring DTOs
to the view models expected by migrated Kinyias pages.

When `VITE_USE_MOCKS=true`, fixture data may be used for local UI inspection.
Mock mode is not live proof and is not the default for production-like runs.
`mockOnly` remains development-only and throws outside explicit mock mode.

Currently connected Spring Boot modules include:

- auth and session: `/auth/login`, `/auth/register`, `/auth/logout`,
  `/auth/refresh`, `/auth/forgot-password`, `/auth/reset-password`,
  `/auth/verify-email`, `/auth/resend`, `/users/me`
- hotels, room types, rooms, members, and inventory: `/hotels`,
  `/hotels/:hotelId`, `/hotels/:hotelId/manage`,
  `/hotels/:hotelId/members`, `/hotels/:hotelId/room-types`,
  `/hotels/:hotelId/rooms`,
  `/hotels/:hotelId/room-types/:roomTypeId/inventory`
- bookings and payments: `/hotels/:hotelId/bookings`,
  `/hotels/:hotelId/bookings/:bookingId`, `/bookings/me`,
  `/bookings/me/:bookingId`, `/bookings/:bookingId/payments/vnpay`
- account profile, avatar, bookings, and reviews: `/users/me`,
  `/users/me/change-password`, `/uploads/avatar`, `/bookings/me`,
  `/reviews/mine`
- content and communication: `/banners`, `/admin/banners`, `/news`,
  `/admin/news`, `/contacts`, `/admin/contacts`, `/notifications`
- policies, promotions, commissions, and dashboard:
  `/hotels/:hotelId/policies`, `/admin/hotels/:hotelId/policies`,
  `/admin/promotions`, `/promotions/public`, `/admin/commission-packages`,
  `/hotels/:hotelId/commission-package/:packageId`, `/dashboard/*`
- RBAC and users: `/users`, `/users/:userId`, `/roles`,
  `/roles/assign-to-user`, `/permissions`, `/actions`

Hidden or excluded routes after live integration:

- `/me/sale` and the mobile "My Offers" entry are removed from normal account
  navigation because the product has no live route or Spring contract for them.
- Google OAuth entry points are hidden until a completed Spring OAuth flow and
  E2E proof exist. `/auth/callback` remains mounted for direct compatibility but
  is not linked from normal navigation.
- `/editor` and `/simple` are development/editor routes. They are not part of
  the product surface unless product navigation intentionally links to them.

## Binary assets

Repomix provided only:

- `file.svg`
- `globe.svg`
- `next.svg`
- `vercel.svg`
- `window.svg`

No real hotel/gallery/banner binary images or fonts were present in the XML. The migrated app uses those SVGs as safe placeholders.

## Manual review

- Active routed pages, shared API adapter utilities, and feature `api.ts` adapter files are typechecked. Remaining `// @ts-nocheck` directives are limited to preserved legacy/editor/component code that is not part of the checked route/API adapter seam.
- The old complex form/table/editor components from `apps/web/src/components` and `apps/web/src/features` are copied into the Vite source tree. Preserved legacy files may remain marked `// @ts-nocheck` where their original Kinyias DTO/query contracts are still intentionally broader than the Spring adapter DTOs.
- Active route bodies now import the copied Kinyias page compositions. Unsupported Spring modules remain visible and mock/TODO-safe rather than deleted.
- OAuth callback is TODO-safe because backend OAuth completion is not implemented in this phase.
- TipTap editor routes are backed by Vite-safe editor components, and the large Kinyias TipTap toolbar/icon/node/template source tree is migrated under `src/components/tiptap-*`.

## Latest verification

```bash
cd frontend
npm run typecheck
npm run build
rg -n "next/(link|image|navigation|dynamic|font)|from ['\"]next|HashRouter|<Link href=|@/app" src package.json
rg -n "AdminResourcePage" src/router src/pages/kinyias src/pages
```

All commands pass or return no matches. The production build still reports large chunk warnings from the full migrated TipTap/editor stack.

Additional acceptance checks now covered:

```bash
rg -n "params\\.(hotel_id|booking_id|news_id|contact_id|commission_id|policy_id|type_id|room_id)" src/pages/kinyias
rg -n "mockOrRequest|from \"@/lib/axios\"|api\\." src/features/{banner,news,policies,promotion,contact,commissions,gallery,notifications,permissions,roles,actions,inventory,reviews,dashboard,room-types}/api.ts
```

Both return no matches.

## Commands

```bash
cd frontend
npm install
npm run dev
npm run typecheck
npm run build
```

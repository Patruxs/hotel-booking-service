You are working inside the existing Spring Boot repository `repomix-output-Patruxs-hotel-booking-service.git.xml` / the real Patruxs Spring Boot workspace.

Your task is to migrate the frontend UI from the provided Repomix source file:

`repomix-output-kinyias-hotel-booking-web.git (1).xml`

into this Spring Boot repository as a new Vite + React + TypeScript SPA under:

`frontend/`

Do not use Next.js. Do not keep Next.js. Do not create a Next.js app.

Important source restriction:
Only use `apps/web` from the Kinyias Repomix frontend source.
Do not copy or migrate `apps/api`.
Do not copy NestJS backend code.
Do not copy Prisma, Redis, Meilisearch, Nest modules, or backend-specific configuration from the source repo.
The Spring Boot backend will be handled later.

Main goal:
Preserve the entire old frontend UI and route structure as much as possible, but convert it from Next.js App Router to Vite + React + React Router.

Target frontend stack:

* Vite
* React
* TypeScript
* React Router
* TanStack React Query
* Axios
* js-cookie
* Tailwind CSS
* Shadcn/ui
* Radix UI dependencies used by the old frontend
* Lucide React
* React Hook Form
* Zod
* React Hot Toast
* TipTap editor dependencies
* date-fns and other non-Next UI/runtime libraries used by the old frontend

Do not use:

* next
* next/image
* next/link
* next/navigation
* next.config.ts
* Next.js middleware/proxy
* eslint-config-next
* Turborepo runtime scripts

Create this structure:

frontend/
index.html
package.json
vite.config.ts
tsconfig.json
tsconfig.node.json if needed
postcss.config.js or postcss.config.mjs
tailwind.config.js or tailwind.config.ts
components.json
.env.example
README.md
API_CONTRACT.md
MIGRATION_NOTES.md
public/
src/
main.tsx
App.tsx
router/
pages/
components/
constants/
features/
hooks/
lib/
providers/
styles/
types/
utils/
mocks/

Keep the import alias `@/` pointing to `frontend/src`.

Keep the Shadcn/ui configuration as close as possible to the old frontend:

* style: new-york
* base color: neutral
* CSS variables
* aliases for components, utils, hooks, lib, ui

Routing requirements:
Convert Next.js file-system routes from `apps/web/src/app` into React Router routes.
Keep URLs as close to the old frontend as possible.

Required route coverage:

* `/`
* `/login`
* `/register`
* `/forgot-password`
* `/auth/callback`
* `/auth/reset`
* `/auth/verify-email`
* `/hotels`
* `/hotels/:hotelId`
* `/booking`
* `/payment-result`
* `/contact`
* `/news`
* `/news/:newsId`
* `/partner`
* `/me`
* `/me/my-bookings`
* `/me/my-bookings/:bookingId`
* `/me/my-reviews`
* `/admin`
* `/admin/dashboard/:hotelId`
* `/admin/hotels`
* `/admin/hotels/:id`
* `/admin/member-hotels`
* `/admin/bookings`
* `/admin/bookings/:hotelId`
* `/admin/bookings/:hotelId/booking/:bookingId`
* `/admin/amenities`
* `/admin/amenities/:id`
* `/admin/users`
* `/admin/users/roles`
* `/admin/users/permissions`
* `/admin/users/actions`
* `/admin/room-types/:hotelId`
* `/admin/room-types/:hotelId/manage/:typeId`
* `/admin/room-types/:hotelId/manage/:typeId/room/:roomId`
* `/admin/inventory`
* `/admin/news`
* `/admin/news/:newsId`
* `/admin/policies/:hotelId`
* `/admin/policies/:hotelId/policy/:policyId`
* `/admin/promotions`
* `/admin/promotions/:id`
* `/admin/reviews/:hotelId`
* `/admin/contacts`
* `/admin/contacts/:contactId`
* `/admin/commissions`
* `/admin/commissions/:commissionId`
* `/admin/commissions/hotels`
* `/admin/settings`
* `/editor`
* `/simple`
* `/403`
* `/forbidden`
* `*` NotFound route

Use `BrowserRouter`, not `HashRouter`.

Replace all Next.js-specific APIs:

* Replace `next/link` with `Link` from `react-router-dom`.
* Replace `next/navigation` with `useNavigate`, `useParams`, `useSearchParams`, and related React Router APIs.
* Replace `next/image` with a local `AppImage` component.
* Replace Next.js layouts with React layout components such as `PublicLayout`, `AdminLayout`, and `AccountLayout`.
* Remove all `'use client'` directives where unnecessary. They are harmless but not needed in Vite; remove them if convenient.

Create an `AppImage` wrapper:

* Use a normal `<img>`.
* Support `src`, `alt`, `className`, `loading`.
* Default to lazy loading.
* Provide a safe fallback image or fallback behavior.
* Do not attempt to reproduce Next.js image optimization.

Auth requirements:
Keep the old frontend-style auth flow where possible, but make it work in Vite.
Use:

* `js-cookie`
* `accessToken` cookie
* `Authorization: Bearer <accessToken>`
* `withCredentials: true`

Create/update Axios client in `frontend/src/lib/axios.ts`.
Use `VITE_API_BASE_URL`.

Environment variables:
Create `.env.example` with:

VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_USE_MOCKS=true
VITE_BYPASS_AUTH=true

Production expectation:

VITE_API_BASE_URL=/api/v1
VITE_USE_MOCKS=false
VITE_BYPASS_AUTH=false

Mock mode:
Implement a simple fixture mock system. Do not use MSW in this phase.

Create:

frontend/src/mocks/
data/
users.ts
hotels.ts
bookings.ts
rooms.ts
amenities.ts
banners.ts
promotions.ts
policies.ts
news.ts
reviews.ts
inventory.ts
commissions.ts
contacts.ts
permissions.ts
roles.ts
actions.ts
mockApi.ts
mockAuth.ts

When `VITE_USE_MOCKS=true`, feature API functions should return mock data sufficient for pages to render.
When `VITE_USE_MOCKS=false`, feature API functions should call the real backend using Axios.

Permission/auth bypass:
Keep the frontend permission structure.
Keep PermissionProvider, Can-like components, AdminRoute, and action guard concepts.
However, when `VITE_BYPASS_AUTH=true`, allow protected/admin pages to render using a mock admin user.
The mock admin user should preserve the old frontend user shape as much as possible and include:

* roles based on the old frontend data model
* allowedActions: ["*"]

Google OAuth:
Local login/register is the priority.
Keep `/auth/callback` route and related UI if possible.
If full OAuth cannot be completed without backend support, make it TODO-safe and do not crash.

Editor:
Keep TipTap editor support.
Keep `/editor` and `/simple` routes as internal/dev routes.
Do not remove editor-related UI/components just because backend is not ready.

API strategy:
Keep the API contract expected by the old frontend.
The Spring Boot backend will be updated later to match this frontend.
Do not redesign all API endpoints to match the current Spring Boot controllers in this phase.

Create `frontend/API_CONTRACT.md`:
Document every API endpoint used by the migrated frontend, grouped by feature:

* auth
* users
* hotels
* rooms
* room types
* bookings
* payments
* amenities
* banners
* news
* policies
* promotions
* reviews
* dashboard
* inventory
* contacts
* commissions
* permissions
* roles
* actions
* gallery/upload
  For each endpoint, include:
* method
* path
* request params/body if inferable
* response shape if inferable
* whether it is currently mocked
* TODO notes for Spring Boot implementation

Create `frontend/MIGRATION_NOTES.md`:
Document:

* what was migrated
* which Next.js APIs were replaced
* which pages/routes were mapped
* which modules use mock data
* which binary assets could not be reconstructed from Repomix XML
* which files need manual review
* known limitations
* commands to run locally

Binary assets:
If the real image/font files are available in the workspace, copy them from `apps/web/public` into `frontend/public`.
If only the Repomix XML is available and binary contents are not present, do not invent real image/font content. Create the expected folder paths if useful and document missing binary assets in `MIGRATION_NOTES.md`.

Do not modify backend Java source in this phase:

* Do not edit `src/main/java/**`.
* Do not change Spring Boot controllers/services/entities.
* Do not implement backend API endpoints now.
* Do not change backend business logic.

Allowed root-level changes:

* Create `frontend/`.
* Update `.gitignore` if needed to ignore `frontend/node_modules`, `frontend/dist`, etc.
* Add documentation if useful.
* Do not break existing Spring Boot build.

Build requirements:
The final frontend should support:

cd frontend
npm install
npm run dev
npm run build
npm run typecheck

Acceptance criteria:

* `npm run build` passes.
* `npm run typecheck` passes or has only clearly documented temporary migration limitations if unavoidable.
* No imports from `next/*` remain in `frontend/src`.
* No `next.config.ts` is used in the new frontend.
* No Next.js App Router runtime remains.
* React Router defines all required routes.
* `VITE_USE_MOCKS=true` lets public and admin UI render without backend.
* `VITE_BYPASS_AUTH=true` allows admin UI inspection without real login.
* `AppImage` replaces `next/image`.
* `react-router-dom` replaces Next navigation.
* Alias `@/` works.
* Shadcn/ui styling remains close to the old frontend.
* `API_CONTRACT.md` exists and is useful for implementing Spring Boot APIs later.
* `MIGRATION_NOTES.md` exists and clearly documents any manual follow-up.

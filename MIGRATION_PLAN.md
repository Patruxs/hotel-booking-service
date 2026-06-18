Không còn thắc mắc lớn nữa. Bây giờ có thể chốt thành **migration strategy chính thức** và sau đó dùng nó để viết prompt cho Codex.

Tôi sẽ chia làm 4 phần:

1. Quyết định cuối cùng đã chốt
2. Migration plan
3. Checklist kiểm tra sau migration
4. System prompt cho Codex

## 1. Quyết định cuối cùng

Ta chốt hướng này:

```text id="s732n3"
Source:
repomix-output-kinyias-hotel-booking-web.git (1).xml

Chỉ lấy:
apps/web

Không lấy:
apps/api, Prisma, NestJS backend, Redis, Meilisearch, Turborepo backend config

Target:
repo Spring Boot Patruxs

Frontend mới:
frontend/ dùng Vite + React + TypeScript

Deploy:
Frontend build static, chạy bằng Nginx/container riêng

Local dev:
Spring Boot chạy localhost:8080
Vite chạy localhost:5173

API strategy:
Giữ API contract frontend cũ
Spring Boot sẽ implement endpoint theo frontend sau

UI goal:
Giữ toàn bộ UI/module/route gần giống cũ nhất có thể
```

Điều này hợp với repo đích vì infrastructure hiện tại đã có mô hình `backend` container port `8080` và `frontend` container port `80` riêng . Repo frontend cũ cũng đã tách nhiều phần có thể reuse như `components`, `features`, `hooks`, `lib`, `providers`, `types`, `utils`, nhưng vẫn đang chứa `next.config.ts`, `proxy.ts`, `package.json` Next.js và App Router nên cần convert chứ không copy nguyên xi .

## 2. Migration plan

### Phase 1 — Tạo Vite frontend shell

Tạo folder:

```text id="yz1tq6"
hotel-booking-service/
├── src/
├── pom.xml
├── Dockerfile
├── frontend/
│   ├── src/
│   ├── public/
│   ├── package.json
│   ├── vite.config.ts
│   ├── tsconfig.json
│   ├── index.html
│   └── .env.example
└── infrastructure/
```

Stack:

```text id="i1x71d"
Vite
React
TypeScript
React Router
TanStack React Query
Axios
js-cookie
Tailwind CSS
Shadcn/ui
Radix UI
Lucide React
React Hook Form
Zod
React Hot Toast
TipTap
```

Không dùng:

```text id="dtsqmc"
next
next/image
next/link
next/navigation
next.config.ts
proxy.ts middleware của Next.js
eslint-config-next
turbo
```

### Phase 2 — Copy phần reusable từ `apps/web`

Copy/recreate các nhóm sau từ `apps/web` sang `frontend/src`:

```text id="edj2aw"
components/
constants/
features/
hooks/
lib/
providers/
styles/
types/
utils/
```

Copy toàn bộ public assets nếu Codex có file thật:

```text id="mddq4w"
apps/web/public/* → frontend/public/*
```

Nếu Codex chỉ có XML và không có binary assets, nó phải tạo `MIGRATION_NOTES.md` ghi rõ ảnh/font nào cần copy thủ công.

### Phase 3 — Convert routing từ Next App Router sang React Router

Giữ URL gần nhất có thể.

Ví dụ:

```text id="el9cq3"
src/app/(public)/hotels/[hotel_id]/page.tsx
→
src/pages/public/HotelDetailPage.tsx
route: /hotels/:hotelId
```

```text id="l431lp"
src/app/(dashboard)/admin/bookings/[hotel_id]/booking/[booking_id]/page.tsx
→
src/pages/admin/BookingDetailPage.tsx
route: /admin/bookings/:hotelId/booking/:bookingId
```

Dùng:

```text id="pm98gi"
BrowserRouter
```

Không dùng HashRouter. Sau này Nginx cần fallback về `index.html`.

### Phase 4 — Replace toàn bộ Next.js APIs

Replace:

```text id="k7kzq9"
next/link        → react-router-dom Link
next/navigation  → useNavigate, useParams, useSearchParams
next/image       → AppImage wrapper
Next layout.tsx  → PublicLayout / AdminLayout / AccountLayout
Next page.tsx    → route components
```

Giữ `AppImage` đơn giản:

```text id="w4owh2"
- nhận src, alt, className
- dùng <img>
- hỗ trợ lazy loading
- fallback ảnh nếu lỗi
```

### Phase 5 — Auth và protected routes

Giữ:

```text id="zzi7si"
js-cookie
Authorization: Bearer <accessToken>
withCredentials: true
```

Env:

```text id="4yk6mf"
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_USE_MOCKS=true
VITE_BYPASS_AUTH=true
```

Production sau này:

```text id="0kebvi"
VITE_API_BASE_URL=/api/v1
VITE_USE_MOCKS=false
VITE_BYPASS_AUTH=false
```

Protected routes:

```text id="2fv0bi"
/admin/* → cần login/admin, nhưng bypass nếu VITE_BYPASS_AUTH=true
/me/*    → cần login, nhưng bypass nếu VITE_BYPASS_AUTH=true
```

Giữ permission structure nhưng bypass/mock trong giai đoạn đầu. Mock admin user dùng dữ liệu theo frontend cũ, ví dụ `roles` và `allowedActions`. Frontend cũ đang có provider auth và user state, nhưng hiện phụ thuộc `next/navigation`, nên provider này cần convert sang React Router .

### Phase 6 — Mock mode

Dùng simple fixture mock, không dùng MSW ở phase đầu.

Cấu trúc:

```text id="u5eokc"
frontend/src/mocks/
├── data/
│   ├── users.ts
│   ├── hotels.ts
│   ├── bookings.ts
│   ├── rooms.ts
│   ├── amenities.ts
│   ├── banners.ts
│   ├── promotions.ts
│   ├── policies.ts
│   ├── news.ts
│   ├── reviews.ts
│   ├── inventory.ts
│   ├── commissions.ts
│   ├── contacts.ts
│   ├── permissions.ts
│   ├── roles.ts
│   └── actions.ts
├── mockApi.ts
└── mockAuth.ts
```

Mỗi `features/*/api.ts` nên giữ API contract cũ. Ví dụ promotion hiện đang gọi `/promotions`, `/promotions/public`, `/promotions/:id`, create/update/delete cũng nằm trong cùng API layer nên rất phù hợp để thêm mock switch ở API boundary .

### Phase 7 — Giữ toàn bộ route

Route map tối thiểu phải giữ:

```text id="g8sz21"
/                           Home
/login                      Login
/register                   Register
/forgot-password            Forgot Password
/auth/callback              OAuth callback placeholder
/auth/reset                 Reset Password
/auth/verify-email          Verify Email

/hotels                     Hotel list
/hotels/:hotelId            Hotel detail
/booking                    Booking
/payment-result             Payment result
/contact                    Contact
/news                       News list
/news/:newsId               News detail
/partner                    Partner

/me                         Account profile
/me/my-bookings             My bookings
/me/my-bookings/:bookingId  Booking detail
/me/my-reviews              My reviews

/admin                      Admin home
/admin/dashboard/:hotelId
/admin/hotels
/admin/hotels/:id
/admin/member-hotels
/admin/bookings
/admin/bookings/:hotelId
/admin/bookings/:hotelId/booking/:bookingId
/admin/amenities
/admin/amenities/:id
/admin/users
/admin/users/roles
/admin/users/permissions
/admin/users/actions
/admin/room-types/:hotelId
/admin/room-types/:hotelId/manage/:typeId
/admin/room-types/:hotelId/manage/:typeId/room/:roomId
/admin/inventory
/admin/news
/admin/news/:newsId
/admin/policies/:hotelId
/admin/policies/:hotelId/policy/:policyId
/admin/promotions
/admin/promotions/:id
/admin/reviews/:hotelId
/admin/contacts
/admin/contacts/:contactId
/admin/commissions
/admin/commissions/:commissionId
/admin/commissions/hotels
/admin/settings

/editor                     internal/dev
/simple                     internal/dev
/403                        Forbidden
/forbidden                  Forbidden
*                           NotFound
```

### Phase 8 — Documentation output

Codex nên tạo thêm:

```text id="rb5vhr"
frontend/API_CONTRACT.md
frontend/MIGRATION_NOTES.md
frontend/README.md
```

`API_CONTRACT.md` dùng để bạn code Spring Boot sau này.

`MIGRATION_NOTES.md` ghi rõ:

```text id="uk7jwh"
- file nào đã migrate
- file nào còn TODO-safe
- binary assets nào cần copy thủ công
- Next.js API nào đã replace
- API nào đang mock
- route nào đã map
```

## 3. Acceptance checklist

Sau migration, phải đạt:

```text id="kh9alm"
cd frontend
npm install
npm run dev
npm run build
npm run typecheck
```

Điều kiện pass:

```text id="p4l83i"
- Không còn import từ next/*
- Không còn next.config.ts trong frontend mới
- Không còn src/app routing kiểu Next.js trong frontend mới
- React Router render được toàn bộ route chính
- Mock mode xem được public UI và admin UI
- VITE_BYPASS_AUTH=true vào được admin không cần backend
- VITE_USE_MOCKS=true không làm app crash khi backend chưa có API
- AppImage thay thế toàn bộ next/image
- Link/navigation dùng react-router-dom
- Alias @/ hoạt động
- Shadcn/ui style giữ giống cũ nhất có thể
- API_CONTRACT.md được tạo
- MIGRATION_NOTES.md được tạo
```


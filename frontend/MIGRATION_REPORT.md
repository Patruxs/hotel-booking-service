# Frontend Migration Report

## Source

- Repomix XML: `hotel-booking.xml`
- Migrated source scope: `apps/web`
- Target: React + Vite + TypeScript under `frontend/`
- Backend target: existing Spring Boot service in this repository

The XML was generated in compressed mode, so many file bodies contain `...`-style compression placeholders. The migration preserves the route surface, API intent, and product flows as a working Vite app instead of copying invalid compressed source.

## Route Mapping

| XML Next.js route | Vite route |
| --- | --- |
| `apps/web/src/app/(public)/page.tsx` | `/` |
| `apps/web/src/app/(public)/hotels/page.tsx` | `/hotels` |
| `apps/web/src/app/(public)/hotels/[hotel_id]/page.tsx` | `/hotels/:hotelId` |
| `apps/web/src/app/(public)/booking/page.tsx` | `/booking` |
| `apps/web/src/app/(public)/(auth)/login/page.tsx` | `/login` |
| `apps/web/src/app/(public)/(auth)/register/page.tsx` | `/register` |
| `apps/web/src/app/(public)/contact/page.tsx` | `/contact` |
| `apps/web/src/app/(public)/partner/page.tsx` | `/partner` |
| `apps/web/src/app/(public)/payment-result/page.tsx` | `/payment-result` |
| `apps/web/src/app/(public)/news/page.tsx` | `/news` |
| `apps/web/src/app/(public)/news/[news_id]/page.tsx` | `/news/:newsId` |
| `apps/web/src/app/(public)/me/page.tsx` | `/me` |
| `apps/web/src/app/(public)/me/my-bookings/page.tsx` | `/me/my-bookings` |
| `apps/web/src/app/(public)/me/my-bookings/[booking_id]/page.tsx` | `/me/my-bookings/:bookingId` |
| `apps/web/src/app/(dashboard)/admin/page.tsx` | `/admin` |
| `apps/web/src/app/(dashboard)/admin/**/page.tsx` | `/admin/**` with React Router params |

## Next.js APIs Replaced

- App Router folder routing -> `react-router-dom` routes in `src/App.tsx`
- `next/link` -> `Link` and `NavLink`
- `next/navigation` -> `useNavigate`, `useParams`, and `useSearchParams`
- `next/image` -> standard `img`
- `process.env.NEXT_PUBLIC_*` -> `import.meta.env.VITE_API_BASE_URL`
- `layout.tsx` route wrappers -> `PublicLayout`, `AdminLayout`, and `AccountLayout`
- `metadata` and server component conventions -> removed

## Matched Spring Boot Endpoints

| Flow | Endpoint |
| --- | --- |
| Login | `POST /api/v1/auth/login` |
| Register | `POST /api/v1/auth/register` |
| Logout | `POST /api/v1/auth/logout` |
| Hotel list | `GET /api/v1/hotels/all` |
| Hotel search | `GET /api/v1/hotels/search` |
| Hotel detail | `GET /api/v1/hotels/{hotelId}` |
| Hotel rooms | `GET /api/v1/hotels/{hotelId}/rooms` |
| My hotels | `GET /api/v1/hotels/my-hotels` |
| Room list | `GET /api/v1/rooms/all` |
| Room detail | `GET /api/v1/rooms/{roomId}` |
| Available rooms | `GET /api/v1/rooms/all-available-rooms` |
| Room types | `GET /api/v1/rooms/types` |
| Create booking | `POST /api/v1/bookings/create` |
| Booking lookup | `GET /api/v1/bookings/get-by-confirmation-code/{confirmationCode}` |
| Admin bookings | `GET /api/v1/bookings/all` |
| Update booking | `PUT /api/v1/bookings/update/{bookingId}` |
| Cancel booking | `DELETE /api/v1/bookings/cancel/{bookingId}` |
| Current user profile | `GET /api/v1/users/get-logged-in-profile-info` |
| User bookings | `GET /api/v1/users/get-user-bookings` |
| Users | `GET /api/v1/users/all` |
| Amenities | `GET /api/v1/amenities/all` |
| Hotel amenities | `GET /api/v1/amenities/hotel/{hotelId}/hotel-amenities` |
| Room amenities | `GET /api/v1/amenities/hotel/{hotelId}/room-amenities` |
| Revenue | `GET /api/v1/revenue/yearly` and `GET /api/v1/revenue/date-range` |
| Physical rooms | `GET /api/v1/physical-rooms/all` and related physical room endpoints |

## Missing Backend Endpoints

These frontend flows existed in the XML route tree or feature folders but do not have matching Spring Boot endpoints in the current project:

- News: `/api/v1/news`, `/api/v1/news/{id}`, news admin CRUD
- Contact and partner inquiries: `/api/v1/contacts`, contact admin review
- Promotions: `/api/v1/promotions`, promotion admin CRUD
- Policies: `/api/v1/policies`, hotel policy CRUD
- Reviews: public review listing, user review listing, admin review moderation
- Commissions: commission list/detail/hotel commission screens
- Gallery/upload folders from the XML backend
- Notification endpoints
- Role, permission, and action management endpoints
- OAuth callback, forgot password, reset password, and email verification endpoints
- Payment provider callback and payment status endpoints
- Application settings endpoint

The Vite routes for these flows remain present and include TODO markers rather than fake API behavior.

## CORS

`src/main/java/org/example/hotelbookingservice/security/CorsConfig.java` already allows `http://localhost:5173` and `http://localhost:3000`, so no backend CORS change was required.

## Assumptions

- The Spring Boot backend returns the shared `ApiResponse<T>` shape with data under `data`.
- Auth uses the JWT token returned by `POST /api/v1/auth/login`.
- Admin routes require an authenticated token with sufficient Spring Security authorities.
- The XML source was compressed, so visual design was approximated from the route and feature structure rather than copied verbatim.

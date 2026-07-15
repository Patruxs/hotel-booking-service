# Thesis Demo Plan: Stable Core Hotel Booking

This runbook intentionally demonstrates only the verified core booking and hotel-management surface. Optional modules are hidden and must not be opened during the presentation.

## 1. Demo prerequisites

- Java 21 at `/home/pat/.local/jdks/jdk-21.0.11+10` or another Java 21 JDK
- Node.js 20 or newer and npm
- Docker with Compose
- Ports 5173, 5432, and 8080 available
- A clean browser profile or private window

## 2. Environment and database setup

From the repository root:

```bash
docker compose up -d postgres
docker compose ps
```

The PostgreSQL service must report healthy. The local Spring profile uses Flyway and applies every migration automatically, including the forward migration that restores the hotel-scoped Owner role after the historical retirement migration.

Set frontend live-mode values in `frontend/.env.local` or the shell:

```bash
VITE_API_BASE_URL=http://127.0.0.1:8080/api/v1
VITE_USE_MOCKS=false
VITE_BYPASS_AUTH=false
VITE_VNPAY_ENABLED=false
```

Never enable mock data, authentication bypass, or VNPay for the thesis demo.

## 3. Build and run commands

Terminal 1, backend:

```bash
JAVA_HOME=/home/pat/.local/jdks/jdk-21.0.11+10 ./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Wait for `Started HotelBookingServiceApplication`, then verify:

```bash
curl http://127.0.0.1:8080/actuator/health
```

Expected result: `{"status":"UP"}`.

Terminal 2, frontend:

```bash
cd frontend
VITE_API_BASE_URL=http://127.0.0.1:8080/api/v1 \
VITE_USE_MOCKS=false \
VITE_BYPASS_AUTH=false \
VITE_VNPAY_ENABLED=false \
npm run dev -- --host 127.0.0.1
```

Open `http://127.0.0.1:5173`.

## 4. Demo accounts

| Purpose | Email | Password | Role |
|---|---|---|---|
| Main management demo | `admin@gmail.com` | `admin123` | Admin |
| Hotel owner demo | `owner@demo.local` | `owner123` | Owner |
| Hotel-scoped management backup | `manager@demo.local` | `staff123` | Manager |
| Front desk backup | `receptionist@demo.local` | `staff123` | Receptionist |
| Customer demo | `customer@gmail.com` | `customer123` | Customer |

The Owner account is scoped to the three `owner@demo.local` hotels seeded by `DataSeeder`. It is intentionally separate from the Manager account. Existing deployments must explicitly assign `OWNER` to a reviewed account and link that account to the intended hotel through `account_roles` and `hotels.owner_id`; the restoration migration does not infer former owners from old Manager assignments.

## 5. Included demo features

- Login, logout, JWT session restoration, and role-based route protection
- Public hotel browsing and hotel details
- Room-type details and date-based availability
- Booking creation without external payment
- Booking confirmation/reference and customer booking history/detail
- Customer cancellation for an eligible booking
- Basic management dashboard
- Hotel list/detail and supported hotel status/edit actions
- Room-type, physical-room, and inventory management
- Hotel booking list/detail and supported booking status actions

## 6. Intentionally hidden or disabled

- VNPay and every external payment action
- Reviews and review moderation
- News and content management
- Promotions
- Commissions and revenue administration beyond the basic dashboard
- Contacts
- Hotel policies
- Amenities administration
- User, role, permission, and API-action administration
- Partner onboarding
- Settings
- Rich-text editor routes

Direct navigation to these frontend pages redirects to a stable hotel page. Backend code is preserved for future work but is outside the demo contract.

## 7. Customer demo flow

### Step 1: Open and browse

Action: Open `/`, then choose `HOTELS` and open a seeded hotel.

Expected: Hotel cards load from the live Spring API. The detail page shows hotel information, images, and bookable room types. These are room types, not individual physical room numbers.

Backup: If a search filter has stale values, use Clear Filters or reload `/hotels`.

### Step 2: Log in

Action: Open `/login` and use `customer@gmail.com` / `customer123`.

Expected: Login succeeds, the header shows the customer account, and `/me` remains authenticated after refresh.

Backup: Use a private window and log in again. Do not enable `VITE_BYPASS_AUTH`.

### Step 3: Check availability and create a booking

Action: Return to a hotel, select dates at least three days in the future, choose an available room type, enter valid guest details, and confirm.

Expected: One booking is created through `POST /api/v1/hotels/{hotelId}/bookings`. A booking reference and status are displayed. No VNPay option or redirect appears.

Backup: Use dates three to five days from today and quantity 1. If inventory was consumed by rehearsal, restart with a fresh database or choose another seeded room type.

### Step 4: View confirmation and history

Action: Open Account, then My Bookings, and select the new record.

Expected: `/api/v1/bookings/me` returns the customer's records and the detail page matches the created booking.

Backup: Show an existing seeded booking if the newly created record is not needed.

### Step 5: Cancel an eligible booking

Action: Cancel the newly created pending booking.

Expected: Status becomes `CANCELLED`; the action cannot be repeated and reserved inventory is restored once.

Backup: Skip cancellation if the record has already advanced to a non-cancellable state. State that cancellation is status-guarded.

## 8. Hotel-management demo flow

### Step 1: Log in as Owner

Action: Log out, then use `owner@demo.local` / `owner123` and open `/admin`.

Expected: The owner shell renders Dashboard, Hotel, Room types, Bookings, and Inventory navigation. The hotel list contains only the hotels assigned to the owner; News, Reviews, Contacts, Amenities, Settings, and RBAC administration remain hidden.

Backup: Open `/admin/hotels` directly after login and use the Admin account for platform-only demonstrations.

### Step 2: Log in as Admin

Action: Log out, then use `admin@gmail.com` / `admin123` and open `/admin`.

Expected: The admin shell renders only Dashboard, Hotel, Room types, Bookings, and Inventory navigation.

Backup: Open `/admin/hotels` directly after login.

### Step 3: Dashboard

Action: Open Dashboard and select a seeded hotel if requested.

Expected: Basic live statistics and recent booking data render. Do not claim advanced financial reporting.

Backup: Continue to Hotels if a non-critical chart is empty.

### Step 4: Hotel management

Action: Open Hotels, select a seeded hotel, and demonstrate the supported detail/edit or status action.

Expected: The list and detail are live. Saved changes remain after refresh and authorization is enforced.

Backup: Demonstrate read-only detail and status if image upload or an optional field depends on an external provider.

### Step 5: Room and inventory management

Action: Open Room types, choose the hotel and a room type, inspect physical rooms, then open Inventory and update a future date.

Expected: Room-type, physical-room, and inventory data load from hotel-scoped APIs. A valid update persists and appears after refresh.

Backup: Use an existing seeded room and demonstrate an inventory update only. Avoid deleting seeded data.

### Step 6: Booking management

Action: Open Bookings, choose the seeded hotel, and open a booking.

Expected: Hotel-scoped booking data loads. Only valid status actions are enabled. A customer account cannot open this management surface.

Backup: Use the seeded completed booking for read-only detail. Do not force an invalid status transition.

## 9. Known limitations

- Registration requires email verification, so live registration is not part of the timed core flow. Explain it briefly or use the seeded customer.
- External VNPay payment is disabled intentionally. Booking creation and confirmation are demonstrated without payment initiation.
- Optional content, commercial, review, RBAC-administration, and editor modules are preserved in source but hidden.
- Hotel pages expose room types to customers; physical room numbers are management data.
- Some dashboard values can be empty when the selected date range has no seeded activity.
- Cloudinary-dependent upload actions are not required for the core demo.

## 10. Troubleshooting

- Backend does not start: run `docker compose ps`, wait for PostgreSQL health, then inspect Flyway errors before restarting.
- HTTP 401 after a previous rehearsal: log out, clear site storage, and log in again.
- HTTP 403 in management: confirm the Owner account owns or is a member of the selected hotel, or use the Manager account with an explicit hotel membership. Platform security routes require Admin.
- Empty availability: choose future dates within the seeded inventory window and quantity 1.
- Frontend shows fixture data: stop immediately and confirm `VITE_USE_MOCKS=false` and `VITE_BYPASS_AUTH=false`.
- Port conflict: stop the old process instead of changing URLs during the presentation.
- Broken non-core action: return to `/hotels` or `/admin/hotels` and continue with the documented backup step.

## 11. Final pre-demo checklist

- [ ] PostgreSQL container is healthy
- [ ] Backend health endpoint returns `UP`
- [ ] Frontend opens with no console errors
- [ ] Mocks, auth bypass, and VNPay are disabled
- [ ] Admin, Owner, Manager, and customer credentials work
- [ ] Hotels and one hotel detail load
- [ ] Future room availability is non-empty
- [ ] A rehearsal booking can be created and appears in My Bookings
- [ ] Admin sidebar shows only the core management surface
- [ ] Owner sees only assigned hotels and receives 403 for a foreign hotel
- [ ] Dashboard, hotels, room types, inventory, and bookings load
- [ ] Optional direct routes redirect safely
- [ ] Browser zoom is 100 percent and no stale tabs are visible
- [ ] Backup seeded booking and hotel are known

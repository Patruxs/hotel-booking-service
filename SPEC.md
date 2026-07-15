# Thesis Demo Stabilization and Core Feature Completion

This project is my thesis. I have very limited time remaining, and my immediate goal is to prepare a stable version of the hotel booking and hotel management system for a live demonstration.

Your task is to inspect the existing codebase, create an OpenSpec implementation plan, and complete the unfinished core features listed below.

Use the project’s available tools:

* GitNexus for repository structure, dependency analysis, call graphs, and change-impact analysis.
* SrcWalk for tracing frontend, backend, database, route, and service flows.
* OpenSpec for creating the implementation specification, tasks, acceptance criteria, and validation plan.
* AgentMemory for recording architecture discoveries, important decisions, completed work, known issues, reusable commands, and verification results across agents and sessions.

Do not rely only on assumptions or previous documentation. Verify every flow against the actual codebase.

---

# Primary objective

Prepare a stable, traditional hotel booking system that can be demonstrated live.

The booking model must be:

1. A customer searches for a hotel.
2. The customer selects an available room type.
3. The customer submits a booking.
4. If inventory is available, the booking is created successfully.
5. The customer pays directly at the hotel reception desk.
6. Staff manage check-in, checkout, cancellation, and no-show operations.

There must be no required online payment flow.

---

# Critical business-rule change

Remove VNPay and promotion dependencies from the live demo flow.

## Required behavior

When a verified customer creates a valid booking and sufficient inventory is available:

* Reserve the requested room-type inventory transactionally.
* Create the booking successfully.
* Generate a confirmation code.
* Show the booking in the customer’s booking history.
* Make the booking visible to hotel staff.
* Set an appropriate initial status for a pay-at-reception booking.
* Do not redirect the customer to VNPay.
* Do not require a payment callback.
* Do not require a promotion code.
* Do not wait for online payment before treating the reservation as valid.

Use a clear payment method or payment status such as:

* Payment method: `PAY_AT_HOTEL`, `PAY_AT_RECEPTION`, or the closest existing supported value.
* Payment status: `UNPAID` or equivalent.
* Booking status: use the existing status that best represents a valid reserved booking.

Do not invent new statuses unless the existing model cannot support this flow safely.

## VNPay

For the demo version:

* Hide VNPay buttons, links, routes, menu items, retry-payment actions, payment-result screens, and redirects.
* Remove VNPay from the primary booking flow.
* Do not spend time finishing VNPay integration.
* Do not delete reusable VNPay code unless deletion is necessary to prevent runtime errors.
* Disable or comment out VNPay integration safely.
* Ensure no VNPay configuration is required to start or demonstrate the application.
* Prevent background callbacks, scheduled tasks, or payment reconciliation logic from breaking the demo.

## Promotions

For the demo version:

* Hide promotion input fields and promotion-selection interfaces.
* Do not automatically apply promotions.
* Do not require promotion lookup during booking creation.
* Do not spend time completing promotion management.
* Preserve reusable promotion code where possible, but isolate it from the demo flow.
* Ensure missing promotion data cannot cause booking creation to fail.

---

# Scope and priority

The features listed below are the requested demo scope.

However, implementation must be prioritized by demo value and dependency order.

Do not implement features in random order.

Use this priority:

## Priority 0: Application stability

Fix these before any feature work:

* Backend build failures.
* Frontend build failures.
* Application startup failures.
* Database migration failures.
* Seed-data failures.
* Authentication failures.
* Critical runtime exceptions.
* Broken environment configuration.
* CORS or API-base-URL issues.
* Route crashes.
* Serialization or validation errors.
* Required demo pages that render blank.
* Required API endpoints returning unexpected errors.

## Priority 1: Core customer booking flow

Complete and verify:

* Registration and login.
* Verified-customer booking access.
* Hotel search.
* Hotel detail.
* Room-type availability.
* Booking creation.
* Booking confirmation.
* Booking history.
* Booking detail.
* Customer booking cancellation.
* Inventory reservation.
* Inventory release after cancellation.
* Clear pay-at-reception messaging.

## Priority 2: Hotel operations

Complete and verify:

* Hotel-scoped booking list.
* Hotel-scoped booking detail.
* Booking-status management.
* Dedicated check-in.
* Guest-detail editing during check-in.
* Checkout.
* No-show.
* Staff cancellation.
* Physical-room assignment if supported.
* Physical-room condition management.

## Priority 3: Hotel and room management

Complete and verify:

* Hotel creation.
* Hotel editing.
* Hotel archiving.
* Hotel-status changes.
* Hotel-member management.
* Room-type management.
* Physical-room management.
* Inventory management by room type and date.
* Amenity management.
* Hotel image management.
* Room-type image management.
* Gallery management.
* Hotel-policy management.

## Priority 4: Customer account and reviews

Complete and verify:

* Profile update.
* Password change.
* Avatar upload.
* Avatar deletion.
* Customer review list.
* Review creation.
* Review editing.
* Review visibility rules.
* Review moderation.

## Priority 5: Administration

Complete only after the booking and operational flows are stable:

* User listing.
* Staff-account creation.
* Admin profile editing.
* Account locking.
* Account unlocking.
* Role assignment.
* Role management.
* Permission management.
* API-action management.
* Role-permission replacement.
* Action-permission replacement.
* Dashboard statistics.
* Dashboard revenue chart.
* Dashboard latest reviews.
* Dashboard newest bookings.

## Priority 6: Public content and optional administration

Complete only if time remains after all higher priorities are stable:

* Public news list and detail.
* Public banners.
* Contact-message submission.
* Partner page.
* News CRUD.
* Banner CRUD.
* Contact-message processing.
* Notifications.
* Unread notification counts.
* Policy CRUD.
* Extended gallery management.

---

# Functional requirements

## 1. Hotel search

The current search implementation supports:

* City.
* Check-in date.
* Check-out date.
* Room count.
* Adults.
* Children.
* Price.

Do not claim that hotel search filters by amenities unless amenity filtering is actually implemented end to end.

For this demo, either:

* Implement amenity filtering fully across frontend, backend, query, and tests, or
* Remove amenity filtering from the demo description.

Do not show a filter that has no effect.

Verify:

* Invalid date ranges are rejected.
* Check-out must be after check-in.
* Guest counts are valid.
* Room count is valid.
* Search results correspond to available room-type inventory.
* Search does not expose archived or inactive hotels.

## 2. Hotel detail and availability

Guests must see bookable room types, not physical room inventory.

The hotel-detail page must display:

* Hotel information.
* Amenities.
* Policies.
* Images.
* Available room types.
* Room-type price.
* Room-type capacity.
* Available quantity for the selected dates.
* Clear booking action.

Physical rooms are operational inventory for staff and must not be presented as customer-selectable hotel rooms unless the architecture intentionally supports that.

Verify the existing room-type availability endpoint and correct any frontend/backend mismatch.

## 3. Booking creation

Use the current primary customer endpoint:

`POST /api/v1/hotels/{hotelId}/bookings`

Do not make the legacy `POST /api/v1/bookings` endpoint the primary frontend flow.

Booking creation must:

* Require an authenticated and verified customer.
* Validate the hotel.
* Validate dates.
* Validate each requested room type.
* Reject duplicate room-type items or merge them safely.
* Confirm that all room types belong to the selected hotel.
* Validate occupancy and quantity.
* Check sufficient availability.
* Prevent overselling during concurrent requests.
* Reserve inventory transactionally.
* Create the booking.
* Generate a confirmation code.
* Store customer and guest information.
* Set the pay-at-reception payment method.
* Set the unpaid payment status.
* Return the created booking.
* Avoid all VNPay and promotion dependencies.

A successful booking must be treated as a valid reservation immediately.

## 4. Booking statuses

Inspect the existing booking-status model before changing behavior.

Map the traditional hotel flow to the safest existing statuses.

The expected logical flow is:

`RESERVED or CONFIRMED -> CHECKED_IN -> CHECKED_OUT`

Alternative outcomes:

* `CANCELLED`
* `NO_SHOW`

Do not use a misleading status merely because it already exists.

If `PENDING` currently means “waiting for online payment,” it must not remain the long-term state for a successful pay-at-reception reservation.

Either:

* Change the booking to the existing confirmed/reserved status immediately, or
* Redefine and document the status safely if the architecture requires it.

Update all affected:

* Backend services.
* Validation rules.
* Scheduled expiry logic.
* Inventory-release logic.
* Frontend labels.
* Staff actions.
* Customer history.
* Dashboard counts.
* Tests.
* Documentation.

## 5. Pending-booking expiry

Inspect the existing expiry process carefully.

If pending expiry exists only for unpaid VNPay bookings:

* Disable it for pay-at-reception reservations.
* Do not allow valid reception-payment bookings to expire automatically.
* Ensure scheduled jobs cannot release inventory for a valid confirmed reservation.

If expiry is still needed for incomplete booking forms, separate incomplete booking attempts from completed pay-at-reception reservations.

## 6. Customer booking history

Use the live customer endpoints:

* `GET /api/v1/bookings/me`
* `GET /api/v1/bookings/me/{bookingId}`
* `PATCH /api/v1/bookings/me/{bookingId}/cancel`

Complete:

* Booking list.
* Booking details.
* Confirmation code.
* Hotel information.
* Dates.
* Room types.
* Guest information.
* Total amount.
* Payment-at-reception label.
* Booking status.
* Cancellation action where allowed.

Remove or hide:

* Retry online payment.
* VNPay payment result.
* Promotion details that are no longer relevant.

Cancellation must:

* Check ownership.
* Check allowed status.
* Release reserved inventory transactionally.
* Update the booking status.
* Be idempotent where possible.
* Return a clear result.

## 7. Hotel-scoped booking operations

Use:

* `GET /api/v1/hotels/{hotelId}/bookings`
* `PATCH /api/v1/hotels/{hotelId}/bookings/{bookingId}/status`
* `POST /api/v1/hotels/{hotelId}/bookings/{bookingId}/check-in`

Complete or verify the actual hotel-scoped detail route as well.

Do not demonstrate legacy global integer-ID endpoints when the live frontend uses hotel-scoped routes.

Enforce:

* Hotel membership.
* Role permissions.
* Booking-to-hotel ownership.
* Valid status transitions.
* Clear operational errors.

## 8. Check-in

Check-in must use the dedicated check-in flow.

Do not simulate check-in by only changing a booking-status string.

The check-in flow should support the existing architecture, including:

* Booking validation.
* Hotel validation.
* Arrival-status validation.
* Guest information.
* Editing or confirming guest details.
* Physical-room assignment if supported.
* Check-in record creation.
* Booking-status transition.
* Timestamp tracking.
* Staff identity tracking where supported.

## 9. Checkout

Implement and expose the checkout action.

Checkout must:

* Require a checked-in booking.
* Validate hotel access.
* Record checkout information.
* Update the booking status.
* Update physical-room condition if applicable.
* Avoid corrupting inventory.
* Return a clear response.

Because payment is collected at reception, the checkout or reception workflow should allow staff to record payment status if the current data model supports it.

Do not build a complete accounting system unless already mostly implemented.

## 10. No-show

Implement and expose the no-show action.

No-show must:

* Be allowed only from an appropriate reserved or confirmed status.
* Be restricted to hotel staff with permission.
* Update the booking status.
* Release or reconcile inventory correctly.
* Record operational information if supported.

## 11. Staff cancellation

Implement hotel-staff cancellation separately from customer cancellation where the architecture supports it.

Staff cancellation must:

* Be hotel scoped.
* Validate permissions.
* Validate the booking’s hotel.
* Validate allowed status.
* Release inventory.
* Record a reason where supported.
* Return a clear result.

## 12. Room resource model

Keep these concepts separate:

### Room type

Contains information such as:

* Name.
* Description.
* Price.
* Capacity.
* Amenities.
* Images.
* Bed configuration.
* Booking inventory category.

### Physical room

Contains information such as:

* Room number.
* Floor.
* Room type.
* Condition.
* Operational status.

### Inventory

Contains:

* Room type.
* Date.
* Total quantity.
* Available quantity.
* Reserved quantity if supported.

Use the live physical-room endpoint:

`POST /api/v1/hotels/{hotelId}/rooms`

Do not use the legacy multipart `POST /api/v1/rooms` route as the primary management flow if it conflates room types and physical rooms.

## 13. Hotel management

Complete the existing management interfaces for:

* Create hotel.
* Edit hotel.
* Archive hotel.
* Change hotel status.
* Manage hotel members.
* Manage room types.
* Manage physical rooms.
* Manage room-type inventory by date.
* Manage amenities.
* Replace hotel images.
* Replace room-type images.
* Manage gallery folders.
* Manage gallery images.
* Manage hotel policies.

For each feature:

* Verify the frontend route.
* Verify the backend endpoint.
* Verify request and response models.
* Verify permission checks.
* Verify validation.
* Verify error handling.
* Verify the page refreshes correctly after changes.

Do not expose broken controls.

## 14. Physical-room conditions

Complete room-condition management using the existing model.

Examples may include:

* Available.
* Occupied.
* Cleaning.
* Maintenance.
* Out of service.

Use only statuses present in the project unless a minimal safe addition is required.

Ensure check-in and checkout interact correctly with room conditions.

## 15. Reviews

The review model must follow the implementation:

* One review per completed booking.
* The booking must belong to the customer.
* Review creation is allowed only for a completed booking.
* Moderation controls visibility.
* Reviews are shown or hidden.

Do not implement or document unsupported states such as:

* Pending review.
* Featured review.

Update inaccurate wording from “hidden or featured” to “shown or hidden.”

Complete:

* Customer review list.
* Review creation.
* Review editing if supported.
* Review visibility.
* Admin moderation.

## 16. Dashboard

Use the implemented endpoints:

* `/api/v1/dashboard/stats`
* `/api/v1/dashboard/revenue-chart`
* `/api/v1/dashboard/latest-reviews`
* `/api/v1/dashboard/newest-bookings`
* `/api/v1/revenue/yearly`
* `/api/v1/revenue/date-range`

Do not call the nonexistent:

`GET /api/v1/dashboard/revenue`

Update frontend API clients, hooks, pages, and documentation accordingly.

Because this is a pay-at-reception system, inspect how revenue is calculated.

Do not count unpaid reservations as collected revenue unless that is explicitly the intended business definition.

Use clear dashboard labels such as:

* Booking value.
* Expected revenue.
* Collected revenue.
* Unpaid at reception.

Only expose metrics that can be calculated reliably from the current data model.

## 17. Customer account management

Complete:

* Update profile.
* Change password.
* Upload avatar.
* Delete avatar.
* View booking details.
* Cancel eligible booking.
* View customer reviews.
* Edit customer review where supported.

Hide:

* Retry failed online payment.
* Online payment result.
* VNPay-related actions.

## 18. Public guest-facing content

Verify and complete, subject to priority:

* Public news list.
* News detail.
* Public banners.
* Contact-message submission.
* Hotel policies.
* Partner page.

Promotion lookup is excluded from the demo.

Remove promotion-related public UI or clearly hide it.

## 19. Admin content management

Complete only after core booking and hotel operations work:

* News CRUD.
* Banner CRUD.
* Contact-message processing.
* Notifications.
* Unread counts.
* Policy CRUD.
* Gallery management.
* Image management.

## 20. User administration

Complete:

* User listing.
* Staff-account creation.
* Admin profile editing.
* Account locking.
* Account unlocking.
* Role assignment.

Verify:

* Locked users cannot authenticate where expected.
* Unlocking restores access.
* Role changes take effect correctly.
* Staff accounts can be assigned to hotels where required.

## 21. RBAC administration

Complete the existing areas for:

* Roles.
* Permissions.
* API actions.
* Role-permission replacement.
* Action-permission replacement.

After permission changes:

* Refresh the authenticated user’s permission data.
* Invalidate or refresh cached `allowedActions`.
* Update the frontend session if safely possible.
* Otherwise show a clear message requiring refresh or re-login.

Do not claim that permission changes appear instantly if the current architecture cannot support it.

Verify backend authorization independently from sidebar visibility.

A hidden menu item is not sufficient authorization.

---

# Feature hiding policy

Any feature that remains incomplete, unstable, or risky must be hidden from the demo interface.

Hide or disable:

* Menu items.
* Buttons.
* Links.
* Routes.
* Cards.
* Dashboard widgets.
* Context-menu actions.
* Form fields.
* API calls.
* Background jobs.
* Scheduled tasks.

Do not leave visible controls that fail when clicked.

Prefer feature flags or centralized configuration over scattered code comments.

When feature flags are not available, comment out or disable code safely and document the exact files changed.

Preserve reusable code where possible.

---

# OpenSpec requirements

Before broad implementation, create an OpenSpec change for this demo-completion effort.

Suggested change name:

`stabilize-traditional-hotel-demo`

The OpenSpec must be based on actual repository inspection.

Include:

## Proposal

* Current problem.
* Demo objective.
* Traditional booking business model.
* Pay-at-reception rules.
* VNPay removal from demo scope.
* Promotion removal from demo scope.
* Included features.
* Deferred features.
* Risks.
* Migration impact.
* Compatibility impact.

## Architecture analysis

Document:

* Frontend architecture.
* Backend architecture.
* Authentication flow.
* Authorization flow.
* Booking flow.
* Inventory flow.
* Check-in flow.
* Checkout flow.
* Room-resource model.
* Dashboard data flow.
* File-storage flow.
* Scheduled jobs.
* Legacy endpoints.
* Current frontend endpoints.
* Database relationships.

## Requirement specifications

Create requirements and scenarios for:

* Hotel search.
* Hotel availability.
* Booking creation.
* Pay-at-reception booking.
* Customer cancellation.
* Staff cancellation.
* Inventory reservation.
* Inventory release.
* Concurrent booking.
* Check-in.
* Checkout.
* No-show.
* Room-condition management.
* Hotel management.
* Room-type management.
* Physical-room management.
* Inventory management.
* Reviews.
* Dashboard.
* User administration.
* RBAC.
* Feature hiding.

Use explicit `GIVEN`, `WHEN`, and `THEN` scenarios.

## Task list

Break tasks into small, dependency-aware units.

Each task must include:

* Exact objective.
* Likely files or modules.
* Dependency.
* Verification command.
* Acceptance result.
* Completion status.

Do not create one large task such as “finish booking system.”

---

# GitNexus usage requirements

Use GitNexus before changing shared or critical code.

Use it to inspect:

* Controllers and API interfaces.
* Service implementations.
* Repositories.
* Entities.
* DTOs.
* Mappers.
* Security configuration.
* Permission checks.
* Frontend API clients.
* React hooks.
* Route definitions.
* Sidebar access.
* Scheduled jobs.
* Database migrations.
* Seed data.
* Tests.

Before editing a core method, identify:

* Callers.
* Callees.
* Related endpoints.
* Related DTOs.
* Related database entities.
* Related tests.
* Change-impact risk.

Do not modify booking statuses, inventory logic, or authorization without tracing dependencies first.

---

# SrcWalk usage requirements

Use SrcWalk to trace complete user flows.

Trace at minimum:

## Customer booking trace

Search form
→ frontend query state
→ search API client
→ backend endpoint
→ availability service
→ inventory query
→ hotel results
→ hotel detail
→ room-type selection
→ booking request
→ booking service
→ inventory reservation
→ booking response
→ booking history

## Staff operations trace

Staff login
→ allowed actions
→ hotel selection
→ hotel booking list
→ booking detail
→ check-in
→ physical-room assignment
→ checkout or no-show
→ room-condition update

## Hotel management trace

Admin login
→ hotel management page
→ hotel endpoint
→ room-type management
→ physical-room management
→ inventory management
→ image management
→ policy management

Record broken links and mismatched request models in AgentMemory.

---

# AgentMemory requirements

Use AgentMemory actively.

Store:

* Project architecture summary.
* Build commands.
* Test commands.
* Environment requirements.
* Database-start commands.
* Demo seed credentials.
* Current supported roles.
* Booking-status meanings.
* Payment-status meanings.
* Room-condition meanings.
* Important endpoints.
* Legacy endpoints that should not be used.
* Frontend routes.
* Hidden features.
* Deferred features.
* Migration decisions.
* Bugs found.
* Fixes completed.
* Verification results.
* Known limitations.

After each major task, update AgentMemory with:

* What changed.
* Why it changed.
* Files affected.
* Commands run.
* Test result.
* Remaining risk.

Do not store guesses as facts. Mark uncertain findings clearly.

---

# Implementation rules

* Make the smallest safe changes.
* Prefer completing existing code over rewriting the architecture.
* Avoid unnecessary dependency upgrades.
* Avoid redesigning the UI.
* Avoid unrelated refactors.
* Preserve working functionality.
* Do not duplicate services or endpoints.
* Do not build a second booking flow.
* Do not use legacy endpoints when the live frontend already uses newer hotel-scoped endpoints.
* Keep authorization on the backend.
* Use database transactions for booking and inventory changes.
* Handle concurrency safely.
* Add clear validation messages.
* Add defensive error handling.
* Do not silently ignore booking failures.
* Do not claim a feature is complete without testing it.
* Do not leave temporary mock data in production paths unless explicitly documented for the demo.
* Do not require VNPay keys.
* Do not require promotion data.
* Do not require external services unless already essential.
* Keep the live demo deterministic.

---

# Testing requirements

## Backend

Run the project’s actual test suite.

Add or update tests for:

* Valid pay-at-reception booking.
* Invalid date range.
* Duplicate room-type item.
* Room type from another hotel.
* Insufficient inventory.
* Concurrent booking attempts.
* Cancellation inventory release.
* Valid status transitions.
* Invalid status transitions.
* Check-in.
* Checkout.
* No-show.
* Staff cancellation.
* Review ownership.
* One review per completed booking.
* Review visibility.
* Hotel-scoped authorization.
* Account locking.
* Role assignment.
* Permission enforcement.

## Frontend

Verify:

* Production build.
* Type checking.
* Linting where configured.
* Search filters.
* Hotel detail.
* Booking form.
* Booking success page.
* Booking history.
* Booking detail.
* Cancellation.
* Staff booking list.
* Check-in modal or page.
* Checkout.
* No-show.
* Hotel management.
* Room-type management.
* Physical-room management.
* Inventory management.
* User administration.
* RBAC pages.
* Hidden VNPay UI.
* Hidden promotion UI.
* No broken navigation.

## End-to-end validation

Use seeded demo data and validate the real application.

Do not validate only isolated API calls.

---

# Required demo scenarios

## Scenario A: Customer booking

1. Open the public website.
2. Search by city, dates, rooms, adults, children, and price.
3. Open a hotel.
4. View amenities, policies, images, and available room types.
5. Select a room type and quantity.
6. Log in or register as a verified customer.
7. Submit the booking.
8. Confirm that the booking succeeds immediately.
9. Confirm that no VNPay page opens.
10. Confirm that no promotion is required.
11. Show the confirmation code.
12. Show the pay-at-reception message.
13. Open My Bookings.
14. Open booking details.
15. Confirm that the booking appears for staff.

## Scenario B: Customer cancellation

1. Open an eligible booking.
2. Cancel it.
3. Confirm the status changes.
4. Confirm inventory is released.
5. Confirm the cancelled booking remains visible in history.

## Scenario C: Reception operations

1. Log in as receptionist or authorized hotel staff.
2. Open the hotel-scoped booking list.
3. Open booking details.
4. Confirm or edit guest information.
5. Perform the dedicated check-in action.
6. Assign a physical room if supported.
7. Show the check-in record.
8. Complete checkout.
9. Show the final booking status.
10. Show the room’s updated condition.

## Scenario D: No-show

1. Open an eligible reservation.
2. Mark it as no-show.
3. Confirm inventory and operational state are updated.
4. Confirm invalid transitions are blocked.

## Scenario E: Hotel management

1. Log in as an authorized manager or administrator.
2. Create or edit a hotel.
3. Manage hotel status.
4. Manage hotel members.
5. Create or edit a room type.
6. Add a physical room.
7. Change physical-room condition.
8. Update room-type inventory for selected dates.
9. Manage amenities.
10. Manage hotel policies.
11. Replace an image.
12. Confirm changes appear in the public hotel page where appropriate.

## Scenario F: Review management

1. Use a completed booking.
2. Create a review.
3. Confirm a second review for the same booking is blocked.
4. Edit the review if supported.
5. Hide the review as an administrator.
6. Show it again.
7. Confirm there is no unsupported pending or featured state.

## Scenario G: Administration and RBAC

1. List users.
2. Create a staff account.
3. Lock the account.
4. Confirm access is blocked.
5. Unlock the account.
6. Assign a role.
7. Edit role permissions.
8. Refresh or re-login if required.
9. Confirm frontend visibility changes.
10. Confirm backend authorization also changes.

---

# Demo data requirements

Create or repair deterministic seed data for the live demo.

Include:

* One active customer account.
* One administrator account.
* One hotel manager account if that role exists.
* One receptionist account.
* At least one active hotel.
* Hotel amenities.
* Hotel policies.
* Hotel images.
* Multiple room types.
* Multiple physical rooms.
* Inventory for the demo date range.
* At least one future reservation.
* At least one checked-in booking if useful.
* At least one completed booking for review demonstration.
* At least one visible review.
* At least one hidden review if moderation is demonstrated.

Do not include Owner-role accounts if the Owner role has already been removed from the project scope.

Document credentials only in the demo documentation or secure local demo configuration.

---

# Update `demo/demo_plan.md`

After implementation and verification, rewrite:

`demo/demo_plan.md`

It must match the final working system exactly.

Include:

## 1. Demo objective

Explain that this is a traditional hotel booking system with payment at reception.

## 2. Prerequisites

* Required software.
* Required environment variables.
* Database requirements.
* Storage requirements.
* Ports.
* Supported browser.
* Required seed data.

VNPay configuration must not be required.

## 3. Startup commands

Provide exact commands for:

* Database.
* Backend.
* Frontend.
* Tests.
* Seed data.
* Production build if needed.

## 4. Demo accounts

Document:

* Username or email.
* Password.
* Role.
* Allowed demo actions.

## 5. Included features

List only features that were verified.

## 6. Hidden or disabled features

Explicitly list:

* VNPay.
* Online payment.
* Payment retry.
* Payment callback.
* Promotion input.
* Promotion application.
* Any other unfinished page or action.

## 7. Customer demo script

Provide exact navigation steps, actions, and expected results.

## 8. Reception demo script

Provide check-in, checkout, no-show, cancellation, and room-condition steps.

## 9. Hotel-management demo script

Provide hotel, room type, physical room, inventory, amenities, policies, and image-management steps.

## 10. Review demo script

Provide customer and moderation steps.

## 11. Administration demo script

Provide user and RBAC steps.

## 12. Expected result

For every action, state what should appear.

## 13. Backup path

For each important scenario, provide a fallback.

Examples:

* Use an existing seeded booking if new booking creation fails.
* Use a seeded completed booking for reviews.
* Use a prepared room for check-in.
* Use a second account if permission cache requires re-login.

## 14. Known limitations

Document all remaining incomplete or hidden functionality honestly.

## 15. Do-not-open list

List all routes, pages, buttons, or features that should not be opened during the presentation.

## 16. Pre-demo checklist

Include:

* Backend starts.
* Frontend starts.
* Database is seeded.
* Demo dates have inventory.
* Demo accounts can log in.
* No VNPay configuration error.
* No promotion error.
* Booking creation works.
* Booking appears in staff view.
* Check-in works.
* Checkout works.
* Admin pages load.
* Browser console has no critical error.
* Server log has no critical error.

---

# Required final deliverables

When the work is complete, provide:

1. OpenSpec proposal.
2. OpenSpec requirement specifications.
3. Prioritized task list.
4. Architecture findings.
5. GitNexus impact-analysis summary.
6. SrcWalk flow-tracing summary.
7. AgentMemory summary.
8. List of fixed features.
9. List of hidden or disabled features.
10. List of remaining incomplete features.
11. VNPay-disabling summary.
12. Promotion-disabling summary.
13. Pay-at-reception implementation summary.
14. Database-migration instructions.
15. Seed-data instructions.
16. Build instructions.
17. Run instructions.
18. Test results.
19. End-to-end verification results.
20. Updated `demo/demo_plan.md`.
21. Exact known limitations.
22. Exact files changed.

---

# Definition of done

The work is complete only when:

* The application builds successfully.
* The backend starts successfully.
* The frontend starts successfully.
* The database initializes successfully.
* A customer can create a real booking using the live frontend.
* The booking succeeds without VNPay.
* The booking succeeds without a promotion.
* The booking clearly says payment is made at reception.
* Inventory is reserved safely.
* The booking appears in customer history.
* The booking appears in the hotel-scoped staff view.
* Customer cancellation releases inventory.
* Staff can perform check-in.
* Staff can perform checkout.
* Staff can mark a valid booking as no-show.
* Hotel and room management pages used in the demo work.
* Required admin and RBAC pages used in the demo work.
* Unfinished features are hidden.
* No visible demo button leads to an error.
* `demo/demo_plan.md` matches the verified system.
* All claims in the final report are supported by executed tests or manual verification.

The main goal is not to complete every possible feature perfectly. The main goal is to produce the most stable, complete, and credible thesis demonstration possible, centered on customer booking, payment at reception, hotel operations, hotel management, and essential administration.

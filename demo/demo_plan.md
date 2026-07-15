# Hotel Booking Service: Comprehensive Demo Plan

## Phase 2: Project Summary

### Architecture & Tech Stack
The **Hotel Booking Service** is built using a modern, layered architecture:
*   **Backend:** Spring Boot (Java 21) adhering to hexagonal/layered architecture principles.
*   **Database:** PostgreSQL (with Flyway for migrations) and Redis for caching/session management.
*   **Frontend:** React 19 (Vite) using TypeScript, styled with Tailwind CSS v4, and utilizing Shadcn UI (Radix UI) for accessible, premium components. Form handling is powered by React Hook Form & Zod.
*   **Security:** JWT-based authentication with a robust Role-Based Access Control (RBAC) model mapping `accounts` to granular `api_actions`.

### Identified User Roles
1.  **Guest (Unauthenticated User):** Can browse the platform, view hotels and rooms, and register for a new account.
2.  **Customer (Authenticated User):** Can book rooms, manage their profile, view booking history, and submit reviews.
3.  **Receptionist:** Can view all bookings, create new bookings, update booking statuses (check-in/check-out), and cancel bookings.
4.  **Manager:** Can moderate (approve/hide/delete) hotel reviews.
5.  **Admin:** Full system access to manage RBAC, Hotels, Rooms, Promotions, Revenue, and oversee User activities.

*   **Guest:** Hotel Search, Room Availability view, User Registration.
*   **Customer:** Authentication (Login/Token refresh), Room Booking, Booking Management, Profile Updates, Review Submission.
*   **Receptionist & Manager:** Booking Status Management (`PUT /api/v1/bookings/{bookingId}`), Review Moderation (`PATCH /admin/hotels/{hotelId}/reviews/{reviewId}/moderation`).
*   **Admin:** RBAC Administration (Roles/Permissions), Hotel & Room CRUD, Revenue Dashboard, Content & Promotion Management.

---

## Phase 3: The Detailed Demo Plan

This chronological demo script is designed to take stakeholders through the complete lifecycle of the platform, building up from basic access to complex administrative controls.

### Part 1: The Guest Experience (Onboarding & Discovery)

**Step 1.1: Platform Discovery**
*   **What to say:** "Welcome to our Hotel Booking Service. We'll start from the perspective of a brand new visitor who wants to find a place to stay."
*   **What to click/do:** Navigate to the homepage (`/`). Use the search bar to filter hotels by location, date, and amenities.
*   **What to expect:** The UI populates a list of available hotels matching the criteria, complete with high-quality images (via Cloudinary integration) and base pricing.

**Step 1.2: Exploring Hotel Details**
*   **What to say:** "Let's dive into a specific property to see what rooms are available."
*   **What to click/do:** Click on a specific Hotel card to view its details (`/hotels/{hotelId}`). Scroll down to the available rooms section.
*   **What to expect:** The detailed hotel page loads, displaying descriptions, amenities, verified reviews, and a list of bookable physical rooms.

**Step 1.3: User Registration**
*   **What to say:** "To proceed with a booking, our guest needs to create an account."
*   **What to click/do:** Click the "Sign Up" button. Fill out the registration form with a new user's details (Email, Password, Name) and submit (`POST /api/v1/auth/register`).
*   **What to expect:** A success notification appears, and the user is either automatically logged in or prompted to log in with their new credentials.

### Part 2: The User Experience (Booking & Management)

**Step 2.1: Authentication & Profile**
*   **What to say:** "Now that we have an account, let's log in and ensure our profile is set up correctly."
*   **What to click/do:** Enter credentials in the Login modal (`POST /api/v1/auth/login`). Navigate to the "My Profile" section.
*   **What to expect:** The system authenticates the user, returning a JWT token. The UI updates to show the user's logged-in state. The profile page displays their details.

**Step 2.2: Completing a Booking**
*   **What to say:** "Here is the core value proposition: seamlessly booking a room."
*   **What to click/do:** Navigate back to a hotel's room list. Select a room, choose dates, and click "Book Now" (`POST /api/v1/bookings`). Complete the mock payment/checkout flow.
*   **What to expect:** The booking is confirmed. The room's availability state changes (preventing double-booking), and a booking reference is generated.

**Step 2.3: Managing Bookings & Submitting Reviews**
*   **What to say:** "Users can easily track their upcoming stays and leave feedback for past ones."
*   **What to click/do:** Go to "My Bookings" (`GET /api/v1/bookings/me` or search via `GET /api/v1/bookings/confirmation-codes/{confirmationCode}`). Select a previously completed booking and submit a 5-star review (`POST /api/v1/hotels/{hotelId}/reviews`).
*   **What to expect:** The bookings list populates correctly. The review submission succeeds and becomes pending or public depending on hotel policies.

### Part 3: The Receptionist & Manager Experience (Operations)

**Step 3.1: Booking Management & Check-in**
*   **What to say:** "Let's log in as a Receptionist, the staff member who handles day-to-day guest operations."
*   **What to click/do:** Log in with Receptionist credentials. Navigate to the bookings list (`GET /api/v1/bookings`). Update the status of the booking we just made to 'Checked In' (`PUT /api/v1/bookings/{bookingId}`).
*   **What to expect:** The booking status updates. 

**Step 3.2: Review Moderation**
*   **What to say:** "For quality control, a Hotel Manager can moderate reviews left by guests."
*   **What to click/do:** Log in with Manager credentials. Navigate to Review Moderation (`GET /api/v1/admin/hotels/{hotelId}/reviews`). Select a review and moderate it (`PATCH /api/v1/hotels/{hotelId}/reviews/{reviewId}/moderation`).
*   **What to expect:** The review status is updated (e.g., hidden or featured).

### Part 4: The Admin Experience (Management & Oversight)

**Step 3.1: Admin Dashboard & Revenue**
*   **What to say:** "Let's switch hats and log in as an Administrator to see the backend controls."
*   **What to click/do:** Log out, then log in with Admin credentials. Navigate to the Admin Dashboard (`GET /api/v1/dashboard/revenue` etc.).
*   **What to expect:** The UI transitions to the Admin layout. A dashboard loads showing high-level metrics: total revenue, occupancy rates, and recent bookings.

**Step 3.2: Hotel & Room Management (CRUD)**
*   **What to say:** "Admins have full control over the inventory. Let's add a new room to an existing hotel."
*   **What to click/do:** Navigate to the "Manage Hotels" section. Select a hotel, navigate to its rooms, and click "Add Room" (`POST /api/v1/rooms`). Fill in the room details (type, price, capacity) and save.
*   **What to expect:** The room is instantly added to the database and becomes immediately available for guests to book on the frontend.

**Step 3.3: Promotions & Content**
*   **What to say:** "To drive sales, Admins can manage active promotions and site content."
*   **What to click/do:** Navigate to "Promotions". Create a new discount code (e.g., "SUMMER20") (`POST /api/v1/admin/promotions`).
*   **What to expect:** The promotion is saved and is now applicable during a user's checkout flow.

**Step 3.4: RBAC & Security Administration**
*   **What to say:** "Finally, our robust RBAC system ensures we can granularly control who has access to what."
*   **What to click/do:** Navigate to "Settings > Roles & Permissions" (`GET /api/v1/roles`). Show the list of actions and demonstrate assigning a new permission to a role.
*   **What to expect:** The system reflects the new access control policies dynamically, showcasing the enterprise-grade security of the platform.

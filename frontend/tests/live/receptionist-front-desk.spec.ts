import { expect, request, test } from "playwright/test";

const apiBaseUrl = normalizeApiBaseUrl(
  process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1",
);
const receptionistCredentials = {
  email: "receptionist@demo.local",
  password: "staff123",
};
const demoHotelId = "40000000-0000-4000-8000-000000000101";
const demoRoomTypeId = "40000000-0000-4000-8000-000000000201";

test("RECEPTIONIST can perform assigned front-desk booking and room operations", async ({ page }) => {
  const browserApiOrigin = process.env.VITE_BROWSER_API_ORIGIN;
  if (browserApiOrigin) {
    await page.route("**/api/v1/**", (route) => {
      const url = new URL(route.request().url());
      url.host = new URL(browserApiOrigin).host;
      return route.continue({ url: url.toString() });
    });
  }

  const api = await authenticatedApi();
  const marker = `${Date.now()}@receptionist-front-desk.test`;
  let bookingId: string | undefined;
  let cancellationBookingId: string | undefined;
  let updatedRoomId: string | undefined;
  let originalRoomCondition: string | undefined;

  try {
    const bookingListResponse = await api.get(
      `hotels/${demoHotelId}/bookings?limit=10&offset=0`,
    );
    expect(bookingListResponse.status(), await bookingListResponse.text()).toBe(200);

    const createResponse = await api.post(`hotels/${demoHotelId}/bookings`, {
      data: {
        checkIn: dateFromToday(0),
        checkOut: dateFromToday(2),
        guestName: "Receptionist Front-Desk Guest",
        guestEmail: marker,
        guestPhone: "0900000027",
        note: "BUG-027 live receptionist regression",
        items: [{ roomTypeId: demoRoomTypeId, quantity: 1 }],
      },
    });
    expect(createResponse.ok(), await createResponse.text()).toBeTruthy();
    const createBody = await createResponse.json();
    expect(createBody.status).toBe(201);
    bookingId = createBody.data?.id;
    expect(bookingId).toBeTruthy();

    const confirmResponse = await api.patch(
      `hotels/${demoHotelId}/bookings/${bookingId}/status`,
      { data: { status: "CONFIRMED" } },
    );
    expect(confirmResponse.status(), await confirmResponse.text()).toBe(200);

    const checkInResponse = await api.post(
      `hotels/${demoHotelId}/bookings/${bookingId}/check-in`,
      {
        data: {
          note: "Receptionist live check-in",
          primary: {
            fullName: "Receptionist Front-Desk Guest",
            email: marker,
            phone: "0900000027",
          },
          companions: [],
        },
      },
    );
    expect(checkInResponse.status(), await checkInResponse.text()).toBe(200);

    const checkOutResponse = await api.patch(
      `hotels/${demoHotelId}/bookings/${bookingId}/status`,
      { data: { status: "COMPLETED" } },
      );
      expect(checkOutResponse.status(), await checkOutResponse.text()).toBe(200);
      bookingId = undefined;

      const cancellationCreateResponse = await api.post(`hotels/${demoHotelId}/bookings`, {
      data: {
        checkIn: dateFromToday(3),
        checkOut: dateFromToday(4),
        guestName: "Receptionist Cancellation Guest",
        guestEmail: `cancel-${marker}`,
        guestPhone: "0900000028",
        note: "BUG-027 cancellation regression",
        items: [{ roomTypeId: demoRoomTypeId, quantity: 1 }],
      },
    });
    expect(cancellationCreateResponse.ok(), await cancellationCreateResponse.text()).toBeTruthy();
    cancellationBookingId = (await cancellationCreateResponse.json()).data?.id;
    expect(cancellationBookingId).toBeTruthy();
    const cancellationResponse = await api.patch(
      `hotels/${demoHotelId}/bookings/${cancellationBookingId}/cancel`,
    );
    expect(cancellationResponse.status(), await cancellationResponse.text()).toBe(200);
    cancellationBookingId = undefined;

    const roomsResponse = await api.get(`hotels/${demoHotelId}/rooms`);
    expect(roomsResponse.status(), await roomsResponse.text()).toBe(200);
    const roomsBody = await roomsResponse.json();
    const room = roomsBody.data?.[0];
    expect(room?.id).toBeTruthy();

    const originalCondition = room.condition ?? room.cleanStatus ?? "CLEAN";
    const nextCondition = originalCondition === "DIRTY" ? "CLEAN" : "DIRTY";
    updatedRoomId = room.id;
    originalRoomCondition = originalCondition;
    const roomUpdateResponse = await api.patch(
      `hotels/${demoHotelId}/rooms/${room.id}/condition`,
      { data: { condition: nextCondition } },
    );
    expect(roomUpdateResponse.status(), await roomUpdateResponse.text()).toBe(200);

    await page.goto("/login");
    await page.getByLabel("Email").fill(receptionistCredentials.email);
    await page.getByPlaceholder("Enter your password").fill(receptionistCredentials.password);
    await page.getByRole("button", { name: "Login", exact: true }).click();
    await expect(page).toHaveURL(/\/admin\/?$/);

    const menu = page.locator('[data-sidebar="menu"]');
    await expect(menu).toContainText("Bookings");
    await expect(menu).toContainText("Rooms");

      await page.goto(`/admin/bookings/${demoHotelId}`);
      await expect(page.getByText("All Bookings", { exact: true })).toBeVisible();
      await expect(page.getByRole("button", { name: /Create Booking/i })).toBeVisible();

      await page.goto(`/admin/rooms/${demoHotelId}`);
      await expect(page.getByRole("heading", { name: "Physical Rooms" })).toBeVisible();
      await expect(page.getByRole("combobox", { name: /Condition for room/i }).first()).toBeVisible();
    } finally {
    if (updatedRoomId && originalRoomCondition) {
      const restoreRoomResponse = await api.patch(
        `hotels/${demoHotelId}/rooms/${updatedRoomId}/condition`,
        { data: { condition: originalRoomCondition } },
      );
      expect(restoreRoomResponse.status(), await restoreRoomResponse.text()).toBe(200);
    }
    if (bookingId) {
      await api.patch(`hotels/${demoHotelId}/bookings/${bookingId}/cancel`);
    }
    if (cancellationBookingId) {
      await api.patch(`hotels/${demoHotelId}/bookings/${cancellationBookingId}/cancel`);
    }
    await api.dispose();
  }
});

async function authenticatedApi() {
  const context = await request.newContext({ baseURL: apiBaseUrl });
  const loginResponse = await context.post("auth/login", {
    data: receptionistCredentials,
  });
  expect(loginResponse.status(), await loginResponse.text()).toBe(200);
  const loginBody = await loginResponse.json();
  const accessToken = loginBody.data?.accessToken;
  expect(accessToken).toBeTruthy();
  const headers = { Authorization: `Bearer ${accessToken}` };

  return {
    get: (url: string) => context.get(url, { headers }),
    post: (url: string, options?: Parameters<typeof context.post>[1]) =>
      context.post(url, { ...options, headers: { ...headers, ...options?.headers } }),
    patch: (url: string, options?: Parameters<typeof context.patch>[1]) =>
      context.patch(url, { ...options, headers: { ...headers, ...options?.headers } }),
    dispose: () => context.dispose(),
  };
}

function dateFromToday(offsetDays: number) {
  const date = new Date();
  date.setUTCDate(date.getUTCDate() + offsetDays);
  return date.toISOString().slice(0, 10);
}

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  const withApiVersion = trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`;
  return `${withApiVersion}/`;
}

import { expect, request, test } from "playwright/test";

const apiBaseUrl = normalizeApiBaseUrl(
  process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1",
);
const demoHotelId = "40000000-0000-4000-8000-000000000101";
const demoRoomTypeId = "40000000-0000-4000-8000-000000000201";

test("ADMIN checks in a customer booking during its booked stay window", async ({ page }) => {
  const browserApiOrigin = process.env.VITE_BROWSER_API_ORIGIN;
  if (browserApiOrigin) {
    await page.route("**/api/v1/**", (route) => {
      const url = new URL(route.request().url());
      url.host = new URL(browserApiOrigin).host;
      return route.continue({ url: url.toString() });
    });
  }

  const customerApi = await authenticatedApi("customer@gmail.com", "customer123");
  const adminApi = await authenticatedApi("admin@gmail.com", "admin123");
  const marker = `${Date.now()}@admin-check-in.test`;
  let bookingId: string | undefined;

  try {
    const createResponse = await customerApi.post(`hotels/${demoHotelId}/bookings`, {
      data: {
        checkIn: dateFromToday(0),
        checkOut: dateFromToday(2),
        guestName: "ADMIN Check-In Guest",
        guestEmail: marker,
        guestPhone: "0900000024",
        note: "BUG-024 live browser regression",
        items: [{ roomTypeId: demoRoomTypeId, quantity: 1 }],
      },
    });
    expect(createResponse.ok(), await createResponse.text()).toBeTruthy();
    const createBody = await createResponse.json();
    bookingId = createBody.data?.id;
    expect(bookingId).toBeTruthy();

    const confirmResponse = await adminApi.patch(
      `hotels/${demoHotelId}/bookings/${bookingId}/status`,
      { data: { status: "CONFIRMED" } },
    );
    expect(confirmResponse.ok(), await confirmResponse.text()).toBeTruthy();

    await page.goto("/login");
    await page.getByLabel("Email").fill("admin@gmail.com");
    await page.getByPlaceholder("Enter your password").fill("admin123");
    await page.getByRole("button", { name: "Login", exact: true }).click();
    await expect(page).toHaveURL(/\/admin\/?$/);

    await page.goto(`/admin/bookings/${demoHotelId}/booking/${bookingId}`);
    await page.getByRole("button", { name: "Update Status" }).click();
    await page.getByRole("button", { name: /Check In Guest/ }).click();
    await expect(page.getByRole("heading", { name: "Check In Guests" })).toBeVisible();
    await page.getByLabel(/Full Name/).fill("ADMIN Check-In Guest");

    const checkInResponsePromise = page.waitForResponse((response) =>
      response.request().method() === "POST"
      && response.url().endsWith(`/hotels/${demoHotelId}/bookings/${bookingId}/check-in`),
    );
    await page.getByRole("button", { name: "Confirm Check In" }).click();
    const checkInResponse = await checkInResponsePromise;
    const checkInBody = await checkInResponse.text();

    expect(checkInResponse.status(), checkInBody).toBe(200);
    await expect(page.getByText("Checked In", { exact: true })).toBeVisible();
  } finally {
    if (bookingId) {
      await adminApi.patch(`hotels/${demoHotelId}/bookings/${bookingId}/cancel`);
    }
    await customerApi.dispose();
    await adminApi.dispose();
  }
});

async function authenticatedApi(email: string, password: string) {
  const context = await request.newContext({ baseURL: apiBaseUrl });
  const loginResponse = await context.post("auth/login", { data: { email, password } });
  expect(loginResponse.ok(), await loginResponse.text()).toBeTruthy();
  const loginBody = await loginResponse.json();
  const accessToken = loginBody.data?.accessToken;
  expect(accessToken).toBeTruthy();
  const headers = { Authorization: `Bearer ${accessToken}` };

  return {
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

import { expect, request, test } from "playwright/test";

const apiBaseUrl = normalizeApiBaseUrl(process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1");
const demoHotelId = "40000000-0000-4000-8000-000000000101";
const demoRoomTypeId = "40000000-0000-4000-8000-000000000201";
const demoBookingId = "40000000-0000-4000-8000-000000000604";

type ApiEnvelope<T> = {
  status?: number;
  message?: string;
  data?: T;
};

async function json<T>(response: Awaited<ReturnType<ReturnType<typeof request.newContext>["get"]>>) {
  expect(response.ok(), `${response.url()} returned ${response.status()}`).toBeTruthy();
  return (await response.json()) as ApiEnvelope<T>;
}

async function login(email: string, password: string) {
  const context = await request.newContext({ baseURL: apiBaseUrl });
  const response = await context.post("auth/login", {
    data: { email, password },
  });
  const body = await json<{ accessToken: string; tokenType?: string }>(response);
  expect(body.data?.accessToken).toBeTruthy();
  const headers = { Authorization: `Bearer ${body.data?.accessToken}` };
  return {
    get: (url: string, options?: Parameters<typeof context.get>[1]) =>
      context.get(url, { ...options, headers: { ...headers, ...options?.headers } }),
    post: (url: string, options?: Parameters<typeof context.post>[1]) =>
      context.post(url, { ...options, headers: { ...headers, ...options?.headers } }),
    patch: (url: string, options?: Parameters<typeof context.patch>[1]) =>
      context.patch(url, { ...options, headers: { ...headers, ...options?.headers } }),
    dispose: () => context.dispose(),
  };
}

test("live mode browser boot does not request fixture assets", async ({ page }) => {
  await page.goto("/");
  await expect(page.locator("body")).toContainText(/hotel|stay|booking/i);
  expect(process.env.VITE_USE_MOCKS).toBe("false");
  expect(process.env.VITE_BYPASS_AUTH).toBe("false");
});

test("auth covers seeded login, users me, refresh, session restore, and logout", async ({ page }) => {
  const api = await login("customer@gmail.com", "customer123");

  const me = await json<{ email: string; allowedActions: string[] }>(await api.get("users/me"));
  expect(me.data?.email).toBe("customer@gmail.com");
  expect(Array.isArray(me.data?.allowedActions)).toBeTruthy();

  const refresh = await json<{ accessToken: string }>(await api.post("auth/refresh"));
  expect(refresh.data?.accessToken).toBeTruthy();

  await page.goto("/login");
  await page.getByLabel("Email").fill("customer@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("customer123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).not.toHaveURL(/\/login$/);

  await page.goto("/me");
  await expect(page).not.toHaveURL(/\/login$/);
  await page.reload();
  await expect(page).not.toHaveURL(/\/login$/);

  const logout = await api.post("auth/logout");
  expect(logout.ok()).toBeTruthy();
  await api.dispose();
});

  test("public booking covers discovery, availability, and pay-at-reception creation", async () => {
  const publicApi = await request.newContext({ baseURL: apiBaseUrl });
  const hotels = await json<{ data?: unknown[]; items?: unknown[] } | unknown[]>(await publicApi.get("hotels"));
  expect(hotels.data).toBeTruthy();

  const availability = await json<{ data: unknown[] }>(
    await publicApi.get(`hotels/${demoHotelId}/room-types/available`, {
      params: {
        from: futureDate(3),
          to: futureDate(5),
      },
    }),
  );
  expect(Array.isArray(availability.data?.data)).toBeTruthy();
  await publicApi.dispose();

  const customerApi = await login("customer@gmail.com", "customer123");
    const booking = await json<{ id: string; totalAmount: string; discountAmount?: string }>(
      await customerApi.post(`hotels/${demoHotelId}/bookings`, {
        data: bookingPayload(),
    }),
  );
      expect(booking.data?.id).toBeTruthy();
      expect(booking.data?.totalAmount).toBeTruthy();
      expect(Number(booking.data?.discountAmount ?? 0)).toBe(0);
      const cancelled = await json<{ status: string }>(
        await customerApi.patch(`bookings/me/${booking.data!.id}/cancel`),
      );
      expect(cancelled.data?.status).toBe("CANCELLED");
      await customerApi.dispose();
  });

  test("customer account covers profile, bookings, booking detail, and cancellation", async () => {
  const api = await login("customer@gmail.com", "customer123");

  const profile = await json<{ email: string }>(await api.get("users/me"));
  expect(profile.data?.email).toBe("customer@gmail.com");

  const bookings = await json<{ data: Array<{ id: string }> }>(await api.get("bookings/me"));
  expect(bookings.data?.data.length).toBeGreaterThan(0);

  const detail = await json<{ id: string }>(await api.get(`bookings/me/${demoBookingId}`));
  expect(detail.data?.id).toBe(demoBookingId);

  const cancelBooking = await json<{ id: string }>(
    await api.post(`hotels/${demoHotelId}/bookings`, {
      data: bookingPayload(),
    }),
  );
  const cancelled = await json<{ id: string; status: string }>(await api.patch(`bookings/me/${cancelBooking.data!.id}/cancel`));
  expect(cancelled.data?.status).toBe("CANCELLED");

    await api.dispose();
  });

test("admin management and permission failure use Spring without fixture fallback", async () => {
  const adminApi = await login("admin@gmail.com", "admin123");

  await json(await adminApi.get("hotels"));
    await json(await adminApi.get(`hotels/${demoHotelId}/room-types`));
    await json(await adminApi.get(`hotels/${demoHotelId}/room-types/${demoRoomTypeId}/inventory`));
    await json(await adminApi.get(`hotels/${demoHotelId}/bookings`));
    await adminApi.dispose();

    const customerApi = await login("customer@gmail.com", "customer123");
    const forbidden = await customerApi.get(`hotels/${demoHotelId}/bookings`);
  expect(forbidden.status()).toBe(403);
  await customerApi.dispose();
});

  function bookingPayload() {
  return {
    checkIn: futureDate(3),
    checkOut: futureDate(5),
    guestName: "Live E2E Guest",
    guestEmail: "customer@gmail.com",
    guestPhone: "0900000001",
    note: "Live E2E smoke booking",
      items: [{ roomTypeId: demoRoomTypeId, quantity: 1 }],
  };
}

function futureDate(offsetDays: number) {
  const date = new Date();
  date.setUTCDate(date.getUTCDate() + offsetDays);
  return date.toISOString().slice(0, 10);
}

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  const withApiVersion = trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`;
  return `${withApiVersion}/`;
}

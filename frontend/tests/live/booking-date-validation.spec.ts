import { expect, request, test } from "playwright/test";

const apiBaseUrl = normalizeApiBaseUrl(
  process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1",
);
const demoHotelId = "40000000-0000-4000-8000-000000000101";
const demoRoomTypeId = "40000000-0000-4000-8000-000000000201";

test("public and staff booking surfaces enforce today plus a one-night minimum", async ({ page }) => {
  const today = dateFromToday(0);
  const tomorrow = dateFromToday(1);
  const yesterdayDayPickerValue = dayPickerValue(-1);

  await page.goto("/");
  await page.getByText("Check In - Check Out", { exact: true }).click();
  await expect(page.locator(`button[data-day="${yesterdayDayPickerValue}"]`).first()).toBeDisabled();

  await page.goto(`/hotels/${demoHotelId}?from=${dateFromToday(-1)}&to=${tomorrow}`);
  await page.getByText("Check-in - Check-out", { exact: true }).locator("..").getByRole("button").click();
  await expect(page.locator(`button[data-day="${yesterdayDayPickerValue}"]`).first()).toBeDisabled();

  await page.goto("/login");
  await page.getByLabel("Email").fill("receptionist@demo.local");
  await page.getByPlaceholder("Enter your password").fill("staff123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/?$/);
  await page.goto(`/admin/bookings/${demoHotelId}`);
  await page.getByRole("button", { name: "Create Booking" }).click();

  const checkIn = page.locator("#reception-check-in");
  const checkOut = page.locator("#reception-check-out");
  await expect(checkIn).toHaveAttribute("min", today);
  await checkIn.fill(today);
  await expect(checkOut).toHaveAttribute("min", tomorrow);
  await checkOut.fill(today);
  expect(await checkOut.evaluate((input: HTMLInputElement) => input.checkValidity())).toBe(false);
});

test("customer confirmation rejects an invalid date range from URL parameters", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Email").fill("customer@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("customer123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).not.toHaveURL(/\/login$/);

  await page.goto(
    `/booking?hotel_id=${demoHotelId}&check_in=${dateFromToday(-1)}&check_out=${dateFromToday(1)}&rooms=${demoRoomTypeId}:1`,
  );
  await expect(page.getByText("Invalid Booking Details", { exact: true })).toBeVisible();
});

test("booking API rejects invalid ranges and accepts today through tomorrow", async () => {
  const context = await request.newContext({ baseURL: apiBaseUrl });
  const loginResponse = await context.post("auth/login", {
    data: { email: "customer@gmail.com", password: "customer123" },
  });
  expect(loginResponse.status(), await loginResponse.text()).toBe(200);
  const accessToken = (await loginResponse.json()).data?.accessToken;
  const headers = { Authorization: `Bearer ${accessToken}` };

  for (const [checkIn, checkOut] of [
    [dateFromToday(-1), dateFromToday(1)],
    [dateFromToday(0), dateFromToday(0)],
    [dateFromToday(2), dateFromToday(1)],
  ]) {
    const response = await context.post(`hotels/${demoHotelId}/bookings`, {
      headers,
      data: bookingPayload(checkIn, checkOut),
    });
    expect(response.status(), await response.text()).toBe(400);
  }

  const validResponse = await context.post(`hotels/${demoHotelId}/bookings`, {
    headers,
    data: bookingPayload(dateFromToday(0), dateFromToday(1)),
  });
  expect(validResponse.ok(), await validResponse.text()).toBe(true);
  const validBody = await validResponse.json();
  expect(validBody.status).toBe(201);
  const bookingId = validBody.data?.id;
  expect(bookingId).toBeTruthy();
  const cancelResponse = await context.patch(`bookings/me/${bookingId}/cancel`, { headers });
  expect(cancelResponse.ok(), await cancelResponse.text()).toBe(true);
  await context.dispose();
});

function bookingPayload(checkIn: string, checkOut: string) {
  return {
    checkIn,
    checkOut,
    guestName: "Booking Date Validation",
    guestEmail: "customer@gmail.com",
    guestPhone: "0900000030",
    note: "BUG-030 live date validation",
    items: [{ roomTypeId: demoRoomTypeId, quantity: 1 }],
  };
}

function dateFromToday(offsetDays: number) {
  const date = new Date();
  date.setDate(date.getDate() + offsetDays);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, "0");
  const day = String(date.getDate()).padStart(2, "0");
  return `${year}-${month}-${day}`;
}

function dayPickerValue(offsetDays: number) {
  const date = new Date();
  date.setDate(date.getDate() + offsetDays);
  return `${date.getMonth() + 1}/${date.getDate()}/${date.getFullYear()}`;
}

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  const withApiVersion = trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`;
  return `${withApiVersion}/`;
}

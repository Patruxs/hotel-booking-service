import { expect, request, test } from "playwright/test";

const demoHotelId = "40000000-0000-4000-8000-000000000101";
const apiBaseUrl = normalizeApiBaseUrl(
  process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1",
);

test.skip(
  process.env.VITE_VNPAY_ENABLED === "true",
  "This file verifies the disabled demo profile; enabled payment coverage lives in live-e2e.spec.ts",
);

function futureDate(daysFromToday: number) {
  const date = new Date();
  date.setDate(date.getDate() + daysFromToday);
  return date.toISOString().slice(0, 10);
}

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  return `${trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`}/`;
}

test("demo checkout does not start or navigate to VNPAY", async ({ page }) => {
  const paymentStartRequests: string[] = [];
  const sandboxRequests: string[] = [];

  page.on("request", (request) => {
    if (request.url().includes("/payments/vnpay")) {
      paymentStartRequests.push(request.url());
    }
  });
  await page.route("https://sandbox.vnpayment.vn/**", async (route) => {
    sandboxRequests.push(route.request().url());
    await route.abort();
  });

  await page.goto("/login");
  await page.getByLabel("Email").fill("customer@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("customer123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).not.toHaveURL(/\/login$/);

  await page.goto(
    `/hotels/${demoHotelId}?from=${futureDate(3)}&to=${futureDate(5)}`,
  );
  const plusButtons = page.getByRole("button", { name: "+", exact: true });
  await expect(plusButtons.first()).toBeVisible();
  for (let index = 0; index < await plusButtons.count(); index += 1) {
    const plusButton = plusButtons.nth(index);
    if (await plusButton.isEnabled()) {
      await plusButton.click();
      break;
    }
  }

  await page.getByRole("button", { name: /Book Now/ }).click();
  await page.getByLabel("Full Name").fill("Demo Customer");
  await page.getByLabel("Phone Number").fill("0900000000");
  await page.getByLabel("Email Address").fill("customer@gmail.com");
    await page.getByRole("button", { name: "Confirm Booking", exact: true }).click();

    await expect(page).toHaveURL(/\/me\/my-bookings\/[^/]+$/);
    await expect(page.getByText(/online payment is unavailable for this demo/i)).toHaveCount(0);
    expect(paymentStartRequests).toHaveLength(0);
  expect(sandboxRequests).toHaveLength(0);
});

test("disabled demo backend rejects direct VNPAY initiation", async () => {
  const api = await request.newContext({ baseURL: apiBaseUrl });
  const loginResponse = await api.post("auth/login", {
    data: { email: "customer@gmail.com", password: "customer123" },
  });
  expect(loginResponse.ok()).toBeTruthy();
  const loginBody = await loginResponse.json();
  const accessToken = loginBody.data?.accessToken;
  expect(accessToken).toBeTruthy();

  const headers = { Authorization: `Bearer ${accessToken}` };
  const bookingsResponse = await api.get("bookings/me", {
    params: { limit: 50, offset: 0, status: "PENDING" },
    headers,
  });
  expect(bookingsResponse.ok()).toBeTruthy();
  const bookingsBody = await bookingsResponse.json();
  const pendingBooking = bookingsBody.data?.data?.at(-1);
  expect(pendingBooking?.id).toBeTruthy();

  const paymentResponse = await api.post(
    `bookings/${pendingBooking.id}/payments/vnpay`,
    { data: {}, headers },
  );
  const problem = await paymentResponse.json();
  expect(paymentResponse.status()).toBe(503);
  expect(problem.status).toBe(503);
  expect(problem.detail).toContain("VNPAY payment is currently unavailable");
  await api.dispose();
});

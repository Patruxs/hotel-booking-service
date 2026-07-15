import { expect, test } from "@playwright/test";

const adminCredentials = {
  email: "admin@gmail.com",
  password: "admin123",
};

const hotelDashboardPath =
  "/admin/dashboard/40000000-0000-4000-8000-000000000102";

test("admin hotel dashboard renders revenue data without a date error", async ({ page }) => {
  const browserErrors: string[] = [];
  page.on("pageerror", (error) => browserErrors.push(error.message));

  await page.goto("/login");
  await page.getByLabel("Email").fill(adminCredentials.email);
  await page.getByPlaceholder("Enter your password").fill(adminCredentials.password);
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin/);

  const revenueResponsePromise = page.waitForResponse((response) =>
    response.url().includes("/api/v1/dashboard/revenue-chart"),
  );
  await page.goto(hotelDashboardPath, { waitUntil: "networkidle" });
  const revenueResponse = await revenueResponsePromise;
  const revenuePayload = (await revenueResponse.json()) as {
    data?: Array<{ period?: string; month?: string }>;
  };

  expect(revenueResponse.ok()).toBeTruthy();
  expect(revenuePayload.data?.length).toBeGreaterThan(0);
  expect(revenuePayload.data?.[0]).toEqual(
    expect.objectContaining({
      period: expect.any(String),
      month: expect.any(String),
    }),
  );

  await page.waitForTimeout(500);
  expect(browserErrors).toEqual([]);
  await expect(page.getByText("Revenue", { exact: true }).last()).toBeVisible();
});

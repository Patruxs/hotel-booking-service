import { expect, test } from "@playwright/test";

const owner = {
  id: "owner-account",
  email: "owner@example.com",
  fullName: "Demo Owner",
  roles: [{ id: "owner-role", name: " role_owner " }],
  allowedActions: [
    "reports.dashboard",
    "reports.hotel.view",
    "hotels.manage",
    "hotel.members.manage",
    "inventory.manage",
    "room_types.manage",
    "bookings.list.hotel",
    "content.manage",
    "reviews.manage",
  ],
};

const ownerAccessToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJvd25lckBleGFtcGxlLmNvbSJ9.test-signature";

test("owner enters the management shell and cannot open an admin-only route", async ({ page }) => {
  await page.route("**/api/v1/auth/login", (route) =>
    route.fulfill({
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        data: { accessToken: ownerAccessToken, jti: "owner-jti", tokenType: "Bearer" },
      }),
    }),
  );
  await page.route("**/api/v1/users/me**", (route) =>
    route.fulfill({
      contentType: "application/json",
      body: JSON.stringify({ status: 200, data: owner }),
    }),
  );
  await page.route("**/api/v1/hotels/manageable**", (route) =>
    route.fulfill({
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        data: {
          data: [{ id: "owned-hotel", name: "Owner Hotel", address: "Owner Street", owner }],
          meta: { limit: 10, offset: 0, total: 1 },
        },
      }),
    }),
  );
  await page.route("**/api/v1/notifications/**", (route) =>
    route.fulfill({
      contentType: "application/json",
      body: JSON.stringify({ status: 200, data: { data: [], meta: { total: 0 } } }),
    }),
  );
  await page.route("**/api/v1/auth/refresh", (route) =>
    route.fulfill({
      contentType: "application/json",
        body: JSON.stringify({ status: 200, data: { accessToken: ownerAccessToken } }),
    }),
  );
  await page.route("**/api/v1/admin/news**", (route) =>
    route.fulfill({
      contentType: "application/json",
      body: JSON.stringify({ status: 200, data: { data: [], meta: { total: 0 } } }),
    }),
  );

  await page.goto("/login");
  await page.getByLabel("Email").fill("owner@example.com");
  await page.getByPlaceholder("Enter your password").fill("owner-password");
  await page.getByRole("button", { name: "Login", exact: true }).click();

  await expect(page).toHaveURL(/\/admin\/?$/);
  const menu = page.locator('[data-sidebar="menu"]');
  await expect(menu).toContainText("Hotel");
  await menu.getByRole("button", { name: "Hotel", exact: true }).click();
  await expect(menu).toContainText("Inventory");
  await expect(menu).toContainText("News");
  await expect(menu).toContainText("Reviews");
  await expect(menu).toContainText("Amenities");
  await expect(menu).not.toContainText("Settings");

  await page.reload();
  await expect(page).toHaveURL(/\/admin\/?$/);
  await expect(menu).toContainText("Hotel");

  await page.goto("/admin/news");
  await expect(page).toHaveURL(/\/admin\/news$/);
});

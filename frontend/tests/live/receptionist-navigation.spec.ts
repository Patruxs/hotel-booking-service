import { expect, test } from "playwright/test";

test("RECEPTIONIST reaches rooms and opens guest booking creation from the sidebar", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Email").fill("receptionist@demo.local");
  await page.getByPlaceholder("Enter your password").fill("staff123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/?$/);
  await expect(page.getByRole("link", { name: "Reviews", exact: true })).toHaveCount(0);

  await page.goto("/admin/reviews");
  await expect(page).toHaveURL(/\/forbidden$/);
  await page.goto("/admin");

  await page.getByRole("link", { name: "Rooms", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/rooms$/);
  await expect(page.getByRole("heading", { name: "Physical Rooms" })).toBeVisible();
  await page.goto("/admin/rooms");
  await expect(page.getByRole("heading", { name: "Physical Rooms" })).toBeVisible();

  await page.getByRole("link", { name: "Bookings", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/bookings$/);
  await page.getByRole("button", { name: "View Bookings" }).first().click();
  await expect(page).toHaveURL(/\/admin\/bookings\/[0-9a-f-]+$/);
  await page.getByRole("button", { name: "Create Booking" }).click();
  await expect(page.getByRole("dialog")).toContainText("Create a reservation on behalf of a guest.");
});

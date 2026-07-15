import { expect, test } from "@playwright/test";

test("customer login keeps the customer identity after a full page reload", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Email").fill("customer@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("customer123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).not.toHaveURL(/\/login$/);

  await page.goto("/me");
  await expect(page.getByText("customer@gmail.com", { exact: true })).toBeVisible();
  await expect(page.getByText("admin@example.com", { exact: true })).toHaveCount(0);

  await page.reload();
  await expect(page.getByText("customer@gmail.com", { exact: true })).toBeVisible();
  await expect(page.getByText("admin@example.com", { exact: true })).toHaveCount(0);

  await page.goto("/admin");
  await expect(page).toHaveURL(/\/forbidden$/);
});

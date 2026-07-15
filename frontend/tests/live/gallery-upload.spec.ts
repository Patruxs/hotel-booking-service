import { expect, test } from "playwright/test";
import { resolve } from "node:path";

const galleryImagePath = resolve(process.cwd(), "../image/vojtech-bruzek-Yrxr3bsPdS0-unsplash.jpg");

test("admin gallery dialog uploads a normal-sized image through the live gallery route", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Email").fill("admin@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("admin123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin/);

  await page.goto("/admin/hotels/new");
  await page.getByRole("button", { name: "Manage Gallery", exact: true }).click();

  const dialog = page.getByRole("dialog").filter({ hasText: "Media Gallery" });
  await expect(dialog).toBeVisible();
  await dialog.getByRole("button", { name: /^Hotel/ }).click();

  const uploadResponse = page.waitForResponse((response) =>
    response.request().method() === "POST" && response.url().includes("/api/v1/") && response.url().includes("Hotel"),
  );
  await dialog.locator('input[type="file"]').setInputFiles(galleryImagePath);

  const response = await uploadResponse;
  expect(response.url()).toContain("/api/v1/gallery/folders/Hotel/images");
  expect(response.status()).toBeLessThan(300);
});

test("live mode drops a stale mock token before requesting the current user", async ({ page }) => {
  await page.context().addCookies([
    {
      name: "accessToken",
      value: "mock-access-token",
      url: "http://127.0.0.1:5173",
    },
  ]);

  const userRequests: string[] = [];
  page.on("request", (request) => {
    if (request.url().includes("/api/v1/users/me")) {
      userRequests.push(request.url());
    }
  });

  await page.goto("/me");
  await expect(page).toHaveURL(/\/login$/);
  expect(userRequests).toHaveLength(0);
});

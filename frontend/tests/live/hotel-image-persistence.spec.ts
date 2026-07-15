import { expect, test } from "@playwright/test";

const adminCredentials = {
  email: "admin@gmail.com",
  password: "admin123",
};

const hotelId = "40000000-0000-4000-8000-000000000101";

test("admin hotel edit persists a selected gallery image", async ({ page }) => {
  await page.goto("/login");
  await page.getByLabel("Email").fill(adminCredentials.email);
  await page.getByPlaceholder("Enter your password").fill(adminCredentials.password);
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin/);

  await page.goto(`/admin/hotels/${hotelId}`);
  await expect(page.getByLabel("Country")).toHaveValue("Vietnam");

  const firstHotelImage = page.locator('img[alt="Hotel image"]').first();
  await firstHotelImage.locator("xpath=..").locator("button").first().click();

  const dialog = page.getByRole("dialog").filter({ hasText: "Media Gallery" });
  await dialog.getByRole("button", { name: /^Hotel/ }).click();
  const galleryImage = dialog.locator("main button:has(img)").first();
  await galleryImage.waitFor({ state: "visible" });
  const selectedImageUrl = await galleryImage.locator("img").getAttribute("src");
  expect(selectedImageUrl).toBeTruthy();
  expect(selectedImageUrl).not.toBe("/globe.svg");
  await galleryImage.click();
  await dialog.getByRole("button", { name: "Confirm Selection", exact: true }).click();

  const imageUpdateResponse = page.waitForResponse(
    (response) =>
      response.request().method() === "PUT" &&
      response.url().endsWith(`/api/v1/hotels/${hotelId}/images`),
    { timeout: 10000 },
  );
  await page.getByRole("button", { name: "Save Hotel", exact: true }).click();
  const response = await imageUpdateResponse;
  expect(response.status()).toBe(200);

  await page.waitForURL(/\/admin\/hotels$/);
  await page.goto(`/admin/hotels/${hotelId}`);
  await expect(page.locator('img[alt="Hotel image"]').first()).toHaveAttribute("src", selectedImageUrl!);
});

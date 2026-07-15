import { expect, test } from "@playwright/test";

test("clear all filters removes URL filters and restores hotel results", async ({ page }) => {
  const hotelRequests: string[] = [];

  await page.route("**/api/v1/hotels**", async (route) => {
    const requestUrl = new URL(route.request().url());
    hotelRequests.push(requestUrl.toString());
    const hasPriceFilter = hotelRequests.length === 1;
    const hotels = hasPriceFilter
      ? []
      : [{
          id: "hotel-after-clear",
          name: "Hotel After Clear",
          location: "Test City",
          description: "A hotel returned after filters are cleared.",
          minPrice: 100,
          images: [{ id: "image-after-clear", url: "/globe.svg" }],
        }];

    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        success: true,
        data: {
          data: hotels,
          meta: { limit: 12, offset: 0, total: hotels.length },
        },
      }),
    });
  });

  await page.goto(
    "/hotels?minPrice=50000000&maxPrice=50000000&check_in=2026-07-20&check_out=2026-07-21&city=Test%20City&rooms=2&adults=2&children=1",
  );
  await expect(page.getByRole("heading", { name: "No hotels found" })).toBeVisible();
  await page.getByRole("button", { name: "Clear all filters", exact: true }).click();

  await expect(page).toHaveURL(/\/hotels\/?$/);
  await expect(page.getByRole("heading", { name: "No hotels found" })).toHaveCount(0);
  await expect(page.getByRole("heading", { name: "Hotel After Clear" })).toBeVisible();
  expect(hotelRequests.some((requestUrl) => !requestUrl.includes("minPrice") && !requestUrl.includes("maxPrice"))).toBeTruthy();
});

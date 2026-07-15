import { expect, test } from "@playwright/test";

const hotelId = "40000000-0000-4000-8000-000000000101";
const bookingId = "completed-hotel-detail-booking";

test("customer can write a review from the hotel detail page", async ({ page }) => {
  const reviewRequests: Request[] = [];

  await page.route(`**/api/v1/hotels/${hotelId}/reviews/eligibility`, async (route) => {
    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        success: true,
        message: "Success",
        data: { canReview: true, bookingId, reason: "ELIGIBLE" },
      }),
    });
  });

  await page.route(`**/api/v1/hotels/${hotelId}/reviews`, async (route) => {
    if (route.request().method() !== "POST") {
      await route.continue();
      return;
    }

    reviewRequests.push(route.request());
    await route.fulfill({
      status: 201,
      contentType: "application/json",
      body: JSON.stringify({
        status: 201,
        success: true,
        message: "Review created",
        data: {
          id: "hotel-detail-review",
          bookingId,
          hotelId,
          rating: 5,
          comment: "Excellent stay from the hotel page.",
          visible: true,
        },
      }),
    });
  });

  await page.goto("/login");
  await page.getByLabel("Email").fill("customer@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("customer123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).not.toHaveURL(/\/login$/);

  await page.goto(`/hotels/${hotelId}`, { waitUntil: "domcontentloaded" });
  const writeReviewButton = page.getByRole("button", { name: "Write a Review", exact: true });
  await expect(writeReviewButton).toBeVisible({ timeout: 5_000 });
  await writeReviewButton.click();
  await page
    .getByRole("textbox", { name: "Review", exact: true })
    .fill("Excellent stay from the hotel page.");
  await page.getByRole("button", { name: "Submit Review", exact: true }).click();

  await expect(page.getByRole("dialog")).toHaveCount(0);
  expect(reviewRequests).toHaveLength(1);
  expect(reviewRequests[0].postDataJSON()).toMatchObject({
    bookingId,
    rating: 5,
    comment: "Excellent stay from the hotel page.",
  });
});

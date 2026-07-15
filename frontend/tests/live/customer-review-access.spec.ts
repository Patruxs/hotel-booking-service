import { expect, test } from "@playwright/test";

const cancelledBookings = Array.from({ length: 10 }, (_, index) => ({
  id: `cancelled-booking-${index + 1}`,
  hotelId: "hotel-grand-city",
  status: "CANCELLED",
  checkIn: "2026-07-16",
  checkOut: "2026-07-18",
  totalAmount: 1600000,
  hotel: {
    id: "hotel-grand-city",
    name: "Grand City Hotel",
    address: "1 Nguyen Hue, District 1",
    city: "Ho Chi Minh City",
    country: "Vietnam",
  },
  items: [],
  payments: [],
  createdAt: "2026-07-11T04:18:18.884404Z",
}));

const completedBooking = {
  id: "completed-booking",
  hotelId: "hotel-grand-city",
  status: "COMPLETED",
  checkIn: "2026-07-03",
  checkOut: "2026-07-05",
  totalAmount: 2220000,
  hotel: {
    id: "hotel-grand-city",
    name: "Grand City Hotel",
    address: "1 Nguyen Hue, District 1",
    city: "Ho Chi Minh City",
    country: "Vietnam",
  },
  items: [
    {
      id: "completed-booking-item",
      quantity: 1,
      unitPrice: 1200000,
      lineTotal: 2400000,
      roomType: { id: "deluxe-city-view", name: "Deluxe City View" },
    },
  ],
  payments: [],
  createdAt: "2026-07-11T04:18:18.884404Z",
};

test("customer can submit a review for a completed booking", async ({ page }) => {
  const bookingRequests: URL[] = [];
  const reviewRequests: Request[] = [];

  await page.route("**/api/v1/bookings/me**", async (route) => {
    const requestUrl = new URL(route.request().url());
    bookingRequests.push(requestUrl);
    const offset = Number(requestUrl.searchParams.get("offset") ?? 0);
    const rows = offset === 10 ? [completedBooking] : cancelledBookings;

    await route.fulfill({
      status: 200,
      contentType: "application/json",
      body: JSON.stringify({
        status: 200,
        success: true,
        message: "Success",
        data: {
          data: rows,
          meta: { limit: 10, offset, total: 11 },
        },
      }),
    });
  });

  await page.route("**/api/v1/hotels/hotel-grand-city/reviews", async (route) => {
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
          id: "new-review",
          bookingId: "completed-booking",
          hotelId: "hotel-grand-city",
          rating: 5,
          comment: "A comfortable stay.",
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

  await page.goto("/me/my-bookings");
  await page.getByRole("button", { name: "2", exact: true }).click();

  const reviewButton = page.getByRole("button", { name: "Review", exact: true });
  await expect(reviewButton).toBeVisible();
  await reviewButton.click();
  await page.getByRole("textbox", { name: "Review", exact: true }).fill("A comfortable stay.");
  await page.getByRole("button", { name: "Submit Review", exact: true }).click();
  await expect(page.getByRole("dialog")).toHaveCount(0);

  expect(reviewRequests).toHaveLength(1);
  expect(reviewRequests[0].postDataJSON()).toMatchObject({
    bookingId: "completed-booking",
    rating: 5,
    comment: "A comfortable stay.",
  });

  const secondPageRequest = bookingRequests.at(-1);
  expect(secondPageRequest?.searchParams.get("offset")).toBe("10");
  expect(secondPageRequest?.searchParams.get("page")).toBeNull();
});

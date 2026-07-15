import { expect, request, test } from "playwright/test";

const apiBaseUrl = normalizeApiBaseUrl(
  process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1",
);
const demoHotelId = "40000000-0000-4000-8000-000000000101";

test("active room type without inventory remains visible as fully booked", async ({
  page,
}) => {
  const api = await authenticatedApi("admin@gmail.com", "admin123");
  const roomTypeName = `E2E unavailable room type ${Date.now()}`;
  let roomTypeId: string | undefined;

  try {
    const createResponse = await api.post(`hotels/${demoHotelId}/room-types`, {
      data: {
        name: roomTypeName,
        description: "Room type intentionally created without inventory",
        pricePerNight: 1_500_000,
        maxGuests: 2,
        numberOfBedrooms: 1,
        amenityIds: [],
      },
    });
    expect(createResponse.ok(), await createResponse.text()).toBeTruthy();
    const created = (await createResponse.json()) as { data?: { id?: string } };
    roomTypeId = created.data?.id;
    expect(roomTypeId).toBeTruthy();

    await page.goto(`/hotels/${demoHotelId}`);

    const roomTypeRow = page
      .getByRole("row")
      .filter({ hasText: roomTypeName });
    await expect(roomTypeRow).toBeVisible();
    await expect(roomTypeRow).toContainText("Fully booked");
    await expect(roomTypeRow.getByRole("button", { name: "+" })).toBeDisabled();
  } finally {
    if (roomTypeId) {
      await api.delete(`hotels/${demoHotelId}/room-types/${roomTypeId}`);
    }
    await api.dispose();
  }
});

async function authenticatedApi(email: string, password: string) {
  const context = await request.newContext({ baseURL: apiBaseUrl });
  const loginResponse = await context.post("auth/login", {
    data: { email, password },
  });
  expect(loginResponse.ok(), await loginResponse.text()).toBeTruthy();
  const login = (await loginResponse.json()) as {
    data?: { accessToken?: string };
  };
  expect(login.data?.accessToken).toBeTruthy();
  const headers = { Authorization: `Bearer ${login.data?.accessToken}` };

  return {
    post: (url: string, options?: Parameters<typeof context.post>[1]) =>
      context.post(url, {
        ...options,
        headers: { ...headers, ...options?.headers },
      }),
    delete: (url: string, options?: Parameters<typeof context.delete>[1]) =>
      context.delete(url, {
        ...options,
        headers: { ...headers, ...options?.headers },
      }),
    dispose: () => context.dispose(),
  };
}

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  const withApiVersion = trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`;
  return `${withApiVersion}/`;
}

import { expect, request, test } from "playwright/test";

const managerCredentials = {
  email: "manager@demo.local",
  password: "staff123",
};
const ownerOnlyRoutes = ["news", "member-hotels", "inventory", "room-types"];
const apiBaseUrl = normalizeApiBaseUrl(
  process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1",
);

test("MANAGER updates hotels and amenities without seeing OWNER-only features", async ({ page }) => {
  const browserApiOrigin = process.env.VITE_BROWSER_API_ORIGIN;
  if (browserApiOrigin) {
    await page.route("**/api/v1/**", (route) => {
      const url = new URL(route.request().url());
      url.host = new URL(browserApiOrigin).host;
      return route.continue({ url: url.toString() });
    });
  }

  const api = await request.newContext({ baseURL: apiBaseUrl });
  const loginResponse = await api.post("auth/login", { data: managerCredentials });
  expect(loginResponse.ok(), await loginResponse.text()).toBeTruthy();
  const loginBody = await loginResponse.json();
    const accessToken = loginBody.data?.accessToken;
    expect(accessToken).toBeTruthy();
    const headers = { Authorization: `Bearer ${accessToken}` };

    const manageableHotelsResponse = await api.get("hotels/manageable", { headers });
    expect(manageableHotelsResponse.ok(), await manageableHotelsResponse.text()).toBeTruthy();
    const manageableHotelsBody = await manageableHotelsResponse.json();
    const managerHotelId = manageableHotelsBody.data?.data?.[0]?.id;
    expect(managerHotelId).toBeTruthy();
    const forbiddenArchiveResponse = await api.delete(`hotels/${managerHotelId}`, { headers });
    expect(forbiddenArchiveResponse.status()).toBe(403);

  const amenityKey = `manager-live-${Date.now()}`;
  const createAmenityResponse = await api.post("amenities", {
    headers,
    data: { key: amenityKey, name: "Manager Live Amenity", type: "GENERAL", active: true },
  });
  expect(createAmenityResponse.ok(), await createAmenityResponse.text()).toBeTruthy();
  const amenity = (await createAmenityResponse.json()).data;
  try {
    const updateAmenityResponse = await api.put(`amenities/${amenity.id}`, {
      headers,
      data: { key: amenityKey, name: "Manager Updated Amenity", type: "GENERAL", active: true },
    });
    expect(updateAmenityResponse.ok(), await updateAmenityResponse.text()).toBeTruthy();
  } finally {
    const deleteAmenityResponse = await api.delete(`amenities/${amenity.id}`, { headers });
    expect(deleteAmenityResponse.ok(), await deleteAmenityResponse.text()).toBeTruthy();
  }
    await page.goto("/login");
  await page.getByLabel("Email").fill(managerCredentials.email);
  await page.getByPlaceholder("Enter your password").fill(managerCredentials.password);
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/?$/);

  const menu = page.locator('[data-sidebar="menu"]');
  await expect(menu).toContainText("Amenities");
  await expect(menu).not.toContainText("News");
  await expect(menu).not.toContainText("Room types");
  await menu.getByRole("button", { name: "Hotel", exact: true }).click();
  await expect(menu).not.toContainText("Members");
  await expect(menu).not.toContainText("Inventory");

  for (const route of ownerOnlyRoutes) {
    await page.goto(`/admin/${route}`);
    await expect(page).toHaveURL(/\/forbidden$/);
  }

    await page.goto("/admin/amenities");
    await expect(page.getByRole("heading", { name: "Amenities" })).toBeVisible();

    const sharedIconLabel = `Manager Shared Wifi ${Date.now()}`;
    await page.getByRole("button", { name: "Add Amenity" }).click();
    await page.getByLabel("Label").fill(sharedIconLabel);
    await page.getByPlaceholder("Search icons...").fill("Wifi");
    await page.getByRole("button", { name: "Wifi", exact: true }).click();
    const sharedIconCreateResponse = page.waitForResponse(
      (response) => response.url().endsWith("/api/v1/amenities") && response.request().method() === "POST",
    );
    await page.getByRole("button", { name: "Create", exact: true }).click();
    const createdResponse = await sharedIconCreateResponse;
    expect(createdResponse.ok(), await createdResponse.text()).toBeTruthy();
    const createdAmenity = (await createdResponse.json()).data;
    expect(createdAmenity.iconKey).toBe("Wifi");
    await expect(page).toHaveURL(/\/admin\/amenities$/);
    const sharedIconRow = page.getByRole("row").filter({ hasText: sharedIconLabel });
    await expect(sharedIconRow).toBeVisible();
    await expect(sharedIconRow.locator("td").first().locator("svg")).toBeVisible();
    const cleanupResponse = await api.delete(`amenities/${createdAmenity.id}`, { headers });
    expect(cleanupResponse.ok(), await cleanupResponse.text()).toBeTruthy();

    await page.goto("/admin/hotels");
    await expect(page.getByRole("button", { name: "Add Hotel" })).toBeHidden();
    await expect(page.getByTitle("Edit").first()).toBeVisible();
    await expect(page.getByTitle("Delete")).toHaveCount(0);
    await page.goto("/admin/hotels/new");
    await expect(page).toHaveURL(/\/forbidden$/);

    await page.goto("/admin/hotels");
    const firstEditButton = page.getByTitle("Edit").first();
  await expect(firstEditButton).toBeVisible();
  await firstEditButton.click();
  await expect(page.getByRole("heading", { name: "Edit Hotel" })).toBeVisible();
    await page.getByRole("button", { name: "Save Hotel" }).click();
    await expect(page).toHaveURL(/\/admin\/hotels$/);
    await api.dispose();
  });

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  const withApiVersion = trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`;
  return `${withApiVersion}/`;
}

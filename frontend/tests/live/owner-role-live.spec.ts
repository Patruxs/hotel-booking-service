import { expect, request, test } from "playwright/test";

const ownerCredentials = {
  email: "owner@demo.local",
  password: "owner123",
};
const ownedHotelId = "40000000-0000-4000-8000-000000000101";
const foreignHotelId = "c0a8ae4a-cc3e-4af0-b336-c9bebc784f32";
const apiBaseUrl = normalizeApiBaseUrl(
  process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1",
);

test("live OWNER workflow stays scoped after reload", async ({ page }) => {
  const browserErrors: string[] = [];
  const serverErrors: string[] = [];
  page.on("pageerror", (error) => browserErrors.push(error.message));
  page.on("response", (response) => {
    if (response.status() >= 500) {
      serverErrors.push(`${response.status()} ${response.url()}`);
    }
  });
  const browserApiOrigin = process.env.VITE_BROWSER_API_ORIGIN;
  if (browserApiOrigin) {
    await page.route("**/api/v1/**", (route) => {
      const url = new URL(route.request().url());
      url.host = new URL(browserApiOrigin).host;
      return route.continue({ url: url.toString() });
    });
  }

  const api = await request.newContext({ baseURL: apiBaseUrl });
  const loginResponse = await api.post("auth/login", { data: ownerCredentials });
  expect(loginResponse.ok()).toBeTruthy();
  const loginBody = await loginResponse.json();
  const accessToken = loginBody.data?.accessToken;
  expect(accessToken).toBeTruthy();
  const headers = { Authorization: `Bearer ${accessToken}` };

  const meResponse = await api.get("users/me", { headers });
  expect(meResponse.ok()).toBeTruthy();
  const meBody = await meResponse.json();
  expect(meBody.data?.roles?.map((role: { name: string }) => role.name)).toContain("OWNER");

  const manageableResponse = await api.get("hotels/manageable", { headers });
  expect(manageableResponse.ok()).toBeTruthy();
  const manageableBody = await manageableResponse.json();
  expect(manageableBody.data?.data?.map((hotel: { id: string }) => hotel.id)).toContain(ownedHotelId);
  expect(manageableBody.data?.data?.map((hotel: { id: string }) => hotel.id)).not.toContain(foreignHotelId);

  const ownedDetail = await api.get(`hotels/${ownedHotelId}/manage`, { headers });
  expect(ownedDetail.ok()).toBeTruthy();

  const foreignDetail = await api.get(`hotels/${foreignHotelId}/manage`, { headers });
  expect(foreignDetail.status()).toBe(403);

  const rolesResponse = await api.get("roles", { headers });
  expect(rolesResponse.status()).toBe(403);
  await api.dispose();

  await page.goto("/login");
  await page.getByLabel("Email").fill(ownerCredentials.email);
  await page.getByPlaceholder("Enter your password").fill(ownerCredentials.password);
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin\/?$/);
  await expect(page.getByText("Grand City Hotel", { exact: true })).toBeVisible();
  await expect(page.getByText("Grand Sapphire Resort", { exact: true })).toHaveCount(0);

  await page.reload();
  await expect(page).toHaveURL(/\/admin\/?$/);
    await page.goto("/admin/hotels");
    await expect(page.getByText("Grand City Hotel", { exact: true })).toBeVisible();
    await expect(page.getByRole("button", { name: "Add Hotel" })).toBeVisible();
    await expect(page.getByTitle("Delete").first()).toBeVisible();
    await page.goto("/admin/hotels/new");
    await expect(page.getByRole("heading", { name: "Create Hotel" })).toBeVisible();
    await page.goto("/admin/hotels");
    await expect(page.getByText("News", { exact: true })).toBeVisible();
  await expect(page.getByText("Reviews", { exact: true })).toBeVisible();
  await expect(page.getByText("Amenities", { exact: true })).toBeVisible();

  await page.goto("/admin/news");
  await expect(page).toHaveURL(/\/admin\/news$/);
  await expect(page.getByRole("heading", { name: "News Management" })).toBeVisible();
  expect(browserErrors).toEqual([]);
  expect(serverErrors).toEqual([]);
});

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  const withApiVersion = trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`;
  return `${withApiVersion}/`;
}

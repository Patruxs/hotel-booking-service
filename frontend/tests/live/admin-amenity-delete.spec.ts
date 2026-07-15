import { expect, request, test } from "playwright/test";

const apiBaseUrl = normalizeApiBaseUrl(process.env.VITE_API_BASE_URL ?? "http://127.0.0.1:8080/api/v1");

test("admin disabling an amenity removes it from the active list", async ({ page }) => {
  const api = await login("admin@gmail.com", "admin123");
  const suffix = Date.now();
  const name = `Live Delete Amenity ${suffix}`;
  const key = `live-delete-amenity-${suffix}`;

  const createResponse = await api.post("amenities", {
    data: { key, name, type: "GENERAL", active: true },
  });
  const created = (await createResponse.json()) as { data?: { id?: string } };
  expect(createResponse.ok()).toBeTruthy();
  expect(created.data?.id).toBeTruthy();
  await api.dispose();

  await page.goto("/login");
  await page.getByLabel("Email").fill("admin@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("admin123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).not.toHaveURL(/\/login$/);

  await page.goto("/admin/amenities", { waitUntil: "networkidle" });
  const row = page.locator("tbody tr").filter({ hasText: name });
  await expect(row).toHaveCount(1);

  await row.getByTitle("Delete").click();
  const deleteResponse = page.waitForResponse((response) =>
    response.request().method() === "DELETE" && response.url().endsWith(`/amenities/${created.data!.id}`),
  );
  await page.getByRole("dialog").getByRole("button", { name: "Delete", exact: true }).click();
  expect((await deleteResponse).ok()).toBeTruthy();

  await expect(row).toHaveCount(0);
  await page.reload({ waitUntil: "networkidle" });
  await expect(page.getByText(name, { exact: true })).toHaveCount(0);
});

async function login(email: string, password: string) {
  const context = await request.newContext({ baseURL: apiBaseUrl });
  const response = await context.post("auth/login", { data: { email, password } });
  const body = (await response.json()) as { data?: { accessToken?: string } };
  expect(response.ok()).toBeTruthy();
  expect(body.data?.accessToken).toBeTruthy();
  const headers = { Authorization: `Bearer ${body.data!.accessToken}` };

  return {
    post: (url: string, options?: Parameters<typeof context.post>[1]) =>
      context.post(url, { ...options, headers: { ...headers, ...options?.headers } }),
    dispose: () => context.dispose(),
  };
}

function normalizeApiBaseUrl(value: string) {
  const trimmed = value.replace(/\/+$/, "");
  const withApiVersion = trimmed.endsWith("/api/v1") ? trimmed : `${trimmed}/api/v1`;
  return `${withApiVersion}/`;
}

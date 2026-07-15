import { expect, request, test } from "playwright/test";

const apiBaseUrl = "http://localhost:8080/api/v1/";

test("admin can create an active hotel without controlled-input warnings", async ({ page }) => {
  const warnings: string[] = [];
  page.on("console", (message) => {
    if (message.type() === "warning" && /uncontrolled|controlled input/i.test(message.text())) {
      warnings.push(message.text());
    }
  });

  await page.goto("/login");
  await page.getByLabel("Email").fill("admin@gmail.com");
  await page.getByPlaceholder("Enter your password").fill("admin123");
  await page.getByRole("button", { name: "Login", exact: true }).click();
  await expect(page).toHaveURL(/\/admin/);

  await page.goto("/admin/hotels/new");
  await page.getByPlaceholder("Hotel name").fill(`E2E Active Hotel ${Date.now()}`);
  await page.getByPlaceholder("Full address").fill("1 Active Street");
  await page.getByPlaceholder("City").fill("Test City");
  await page.getByPlaceholder("Country").fill("Vietnam");
  await page.locator('[contenteditable="true"]').fill("Active hotel regression test");

  await expect(page.getByText("Status", { exact: true })).toBeVisible();
  await page.locator('[role="combobox"]').click();
  await page.getByRole("option", { name: "Active", exact: true }).click();

  const createResponsePromise = page.waitForResponse(
    (response) => response.request().method() === "POST" && response.url().includes("/api/v1/hotels"),
  );
  await page.getByRole("button", { name: "Save Hotel", exact: true }).click();

  const createResponse = await createResponsePromise;
  expect(createResponse.status()).toBe(200);
  const body = await createResponse.json();
  expect(body.data.status).toBe("ACTIVE");
  expect(warnings).toEqual([]);

  const accessToken = (await page.context().cookies()).find((cookie) => cookie.name === "accessToken")?.value;
  const cleanup = await request.newContext({
    baseURL: apiBaseUrl,
    extraHTTPHeaders: { Authorization: `Bearer ${accessToken}` },
  });
  await expect(
    (await cleanup.patch(`hotels/${body.data.id}/status`, { data: { status: "ARCHIVED" } })).status(),
  ).toBe(200);
  await cleanup.dispose();
});

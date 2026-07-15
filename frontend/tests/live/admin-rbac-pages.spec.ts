import { expect, test } from "@playwright/test";

const adminCredentials = {
  email: "admin@gmail.com",
  password: "admin123",
};

for (const route of [
  "/admin/users/roles",
  "/admin/users/actions",
]) {
  test(`demo-hidden admin route ${route} redirects safely`, async ({ page }) => {
    const serverErrors: string[] = [];
    const browserErrors: string[] = [];

    page.on("response", (response) => {
      if (response.status() >= 500) {
        serverErrors.push(`${response.status()} ${response.url()}`);
      }
    });
    page.on("pageerror", (error) => browserErrors.push(error.message));

    await page.goto("/login");
    await page.getByLabel("Email").fill(adminCredentials.email);
    await page.getByPlaceholder("Enter your password").fill(adminCredentials.password);
    await page.getByRole("button", { name: "Login", exact: true }).click();
    await expect(page).not.toHaveURL(/\/login$/);

    await page.goto(route);
    await expect(page).toHaveURL(/\/admin\/hotels$/);
    expect(serverErrors).toEqual([]);
    expect(browserErrors).toEqual([]);
  });
}

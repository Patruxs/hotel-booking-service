import { test, expect } from '@playwright/test';

test('Check if Grand City Hotel is visible', async ({ page }) => {
  await page.goto('http://127.0.0.1:5173/');
  
  // Wait for the text to appear on the page, with a reasonable timeout in case data is loading
  await expect(page.getByText('Grand City Hotel')).toBeVisible({ timeout: 10000 });
});

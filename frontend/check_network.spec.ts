import { test, expect } from '@playwright/test';

test('check network requests', async ({ page }) => {
  page.on('response', async (response) => {
    const url = response.url();
    console.log(`[Response] ${response.status()} ${url}`);
    if (url.includes('/api/') || url.includes('localhost')) {
      try {
        const text = await response.text();
        try {
          const body = JSON.parse(text);
          console.log(`[Body] ${JSON.stringify(body).substring(0, 200)}`);
        } catch {
          console.log(`[Body text length: ${text.length}]`);
        }
      } catch (e) {
        console.log(`[Error reading body] ${e}`);
      }
    }
  });

  await page.goto('http://127.0.0.1:5174/hotels');
  
  // Wait a bit to let network requests complete
  await page.waitForTimeout(5000);
});

import { chromium } from 'playwright';

(async () => {
  const url = process.argv[2] || 'http://localhost:5173/';
  console.log(`Checking ${url}...`);
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  page.on('console', msg => {
    if(msg.type() === 'error') console.log(`[${url}] BROWSER CONSOLE ERROR:`, msg.text());
  });
  page.on('pageerror', err => console.log(`[${url}] BROWSER ERROR:`, err.message));
  page.on('response', response => {
    if(response.status() >= 400) console.log(`[${url}] NETWORK ERROR:`, response.url(), response.status());
  });

  try {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 10000 });
    const bodyText = await page.evaluate(() => document.body.innerText);
    console.log(`[${url}] BODY TEXT:`, bodyText.substring(0, 500).replace(/\n/g, ' '));
  } catch (e) {
    console.log(`[${url}] FAILED TO LOAD:`, e.message);
  }
  
  await browser.close();
})();

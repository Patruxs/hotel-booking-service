import { chromium } from 'playwright';

(async () => {
  const url = process.argv[2] || 'http://localhost:5173/';
  console.log(`Checking ${url}...`);
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  page.on('console', msg => {
    console.log(`[${url}] BROWSER CONSOLE (${msg.type()}):`, msg.text());
  });
  page.on('pageerror', err => console.log(`[${url}] BROWSER ERROR:`, err.message));
  page.on('response', response => {
    if(response.status() >= 400) console.log(`[${url}] NETWORK ERROR:`, response.url(), response.status());
  });

  try {
    await page.goto(url, { waitUntil: 'networkidle', timeout: 10000 });
    const carouselHTML = await page.evaluate(() => {
      const el = document.querySelector('.py-16.md\\:py-24'); // the HotelCarousel section
      return el ? el.innerHTML : 'Carousel not found';
    });
    console.log(`[${url}] CAROUSEL HTML:`, carouselHTML.substring(0, 1000).replace(/\n/g, ' '));
  } catch (e) {
    console.log(`[${url}] FAILED TO LOAD:`, e.message);
  }
  
  await browser.close();
})();

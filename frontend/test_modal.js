import { chromium } from 'playwright';

(async () => {
  const browser = await chromium.launch();
  const page = await browser.newPage();
  
  await page.goto('http://127.0.0.1:5173/admin/inventory', { waitUntil: 'networkidle' });
  
  try {
    // Wait for the hotels to load
    await page.waitForTimeout(2000); 
    
    const triggers = await page.locator('button[role="combobox"]').all();
    if (triggers.length >= 2) {
      console.log("clicking hotel...");
      await triggers[0].click();
      await page.waitForTimeout(500);
      await page.keyboard.press('ArrowDown');
      await page.keyboard.press('Enter');
      await page.waitForTimeout(1000);
      
      console.log("clicking Create Inventory button...");
      await page.locator('button:has-text("Create Inventory")').click();
      await page.waitForTimeout(1000);
      
      console.log("clicking Room Type dropdown in modal...");
      // In the modal, there is a combobox for Room Type
      const modalCombobox = page.locator('[role="dialog"] button[role="combobox"]');
      await modalCombobox.click();
      await page.waitForTimeout(500);
      
      const options = await page.locator('[role="option"]').allTextContents();
      console.log("Modal Room Type items:", options);
      
      // Let's also grab the HTML of the select content
      const content = await page.locator('[role="listbox"]').innerHTML();
      console.log("Listbox HTML:", content);
    } else {
        console.log("Not enough comboboxes found", triggers.length);
    }
  } catch (err) {
    console.log("TEST ERROR:", err);
  }
  
  await browser.close();
})();

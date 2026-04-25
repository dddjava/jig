const { test, expect } = require('@playwright/test');

test('has title', async ({ page }) => {
  await page.goto('/');

  // タイトルに JIG が含まれていることを確認
  await expect(page).toHaveTitle(/JIG/);
});

test('index page has navigation', async ({ page }) => {
  await page.goto('/');

  // 何らかのナビゲーションや要素が存在することを確認（適宜調整）
  const body = page.locator('body');
  await expect(body).not.toBeEmpty();
});

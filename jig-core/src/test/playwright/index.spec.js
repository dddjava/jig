const { test, expect } = require('@playwright/test');

test('JIG Scenario Test', async ({ page }) => {
  // 1秒以内の応答を期待する（Mermaidの描画などは除く）
  const fastExpect = expect.configure({ timeout: 1000 });

  // 1. index.htmlを表示
  await page.goto('/');
  await fastExpect(page).toHaveTitle(/JIG/);

  // 2. ドキュメントのリンクが並んでいること
  const docLinks = page.locator('#document-links');
  await fastExpect(docLinks.getByRole('link', { name: '用語集' })).toBeVisible();
  await fastExpect(docLinks.getByRole('link', { name: 'パッケージ関連' })).toBeVisible();
  await fastExpect(docLinks.getByRole('link', { name: 'ドメインモデル' })).toBeVisible();

  // 3. 「用語集」をクリックして画面遷移
  await page.getByRole('link', { name: '用語集' }).click();
  await fastExpect(page).toHaveURL(/glossary.html/);

  // 4. 「classファイル一式」のカードが表示されていること
  const classCard = page.locator('.jig-card', { hasText: 'classファイル一式' }).first();
  await fastExpect(classCard).toBeVisible();

  // 5. サイドバーに「JIGの情報源」のパッケージがあり、それをクリックしたらその場所に移動すること
  const sidebarItem = page.locator('#term-sidebar-list').getByText('JIGの情報源').first();
  await sidebarItem.click();

  // 移動先のセクションが存在することを確認
  const sourceSection = page.locator('.glossary-group', { hasText: 'JIGの情報源' }).first();
  await fastExpect(sourceSection).toBeVisible();

  // 6. カード内の属性情報をクリックしたら属性情報が表示される
  const attributesToggle = classCard.locator('.term-attributes-toggle');
  await attributesToggle.click();
  await fastExpect(classCard.getByRole('link', { name: 'ドメインモデル' })).toBeVisible();

  // 7. 属性情報の中の「関連ドキュメント」に「ドメインモデル」があり、
  // それをクリックしたらドメインモデルのページに遷移し、該当カードが表示されていること
  const domainLink = classCard.getByRole('link', { name: 'ドメインモデル' });
  await domainLink.click();

  await fastExpect(page).toHaveURL(/domain.html/);
  // ドメインモデルページで「classファイル一式」のカードが表示されていること
  const domainClassCard = page.locator('.jig-card', { hasText: 'classファイル一式' }).first();
  await fastExpect(domainClassCard).toBeVisible();

  // 8. パッケージ関連図が表示されていること
  // ※ 「classファイル一式」から所属パッケージ「JIGの情報源」のカードを特定
  const domainPackageCard = page.locator('.jig-card', { hasText: 'JIGの情報源' }).first();
  await domainPackageCard.scrollIntoViewIfNeeded();
  
  // MermaidのSVG生成待ち
  const diagram = domainPackageCard.locator('.mermaid-diagram').first();
  await expect(diagram.locator('svg')).toBeVisible({ timeout: 5000 });

  // 9. パッケージ内パッケージ関連図タブをクリックしたらパッケージ内パッケージ関連図が表示されること
  const pkgPkgTab = domainPackageCard.locator('.jig-tab', { hasText: 'パッケージ内パッケージ関連図' });
  if (await pkgPkgTab.isVisible()) {
    await pkgPkgTab.click();
    await expect(domainPackageCard.locator('.jig-tab-panel:not(.hidden)').locator('.mermaid-diagram svg')).toBeVisible({ timeout: 5000 });
  }

  // 10. パッケージ内クラス関連図タブをクリックしたらパッケージ内クラス関連図が表示されること
  const pkgClassTab = domainPackageCard.locator('.jig-tab', { hasText: 'パッケージ内クラス関連図' });
  if (await pkgClassTab.isVisible()) {
    await pkgClassTab.click();
    await expect(domainPackageCard.locator('.jig-tab-panel:not(.hidden)').locator('.mermaid-diagram svg')).toBeVisible({ timeout: 5000 });
  }
});

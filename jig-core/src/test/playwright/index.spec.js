const {test, expect} = require('@playwright/test');

test('JIG Scenario Test', async ({page}) => {
    // 1秒以内の応答を期待する（Mermaidの描画などは除く）
    const fastExpect = expect.configure({timeout: 1000});

    await page.goto('/');
    await fastExpect(page).toHaveTitle(/JIG/);

    // ドキュメントのリンクが並んでいること
    const docLinks = page.locator('#document-links');
    await fastExpect(docLinks.getByRole('link', {name: '用語集'})).toBeVisible();
    await fastExpect(docLinks.getByRole('link', {name: 'パッケージ関連'})).toBeVisible();
    await fastExpect(docLinks.getByRole('link', {name: 'ドメインモデル'})).toBeVisible();
    await expect(docLinks).toHaveScreenshot('index-docLinks.png');

    // 「用語集」をクリックして画面遷移
    await page.getByRole('link', {name: '用語集'}).click();
    // ------------------------------------------------------------
    // 用語集
    // ------------------------------------------------------------
    await fastExpect(page).toHaveURL(/glossary.html/);

    // 「classファイル一式」のカードが表示されていること
    const classCard = page.locator('.jig-card', {hasText: 'classファイル一式'}).first();
    await fastExpect(classCard).toBeVisible();
    await expect(classCard).toHaveScreenshot('glossary-classCard.png');

    // サイドバーに「JIGの情報源」のパッケージがあり、それをクリックしたらその場所に移動すること
    const sidebarItem = page.locator('#term-sidebar-list').getByText('JIGの情報源').first();
    await sidebarItem.click();
    const sourcesCard = page.locator('.jig-card', {hasText: 'JIGの情報源'}).first();
    await fastExpect(sourcesCard).toBeVisible();

    // カード内の属性情報をクリックしたら属性情報が表示される
    const attributesToggle = sourcesCard.locator('.term-attributes-toggle');
    await attributesToggle.click();
    const domainModelLink = sourcesCard.getByRole('link', {name: 'ドメインモデル'});
    await fastExpect(domainModelLink).toBeVisible();
    await expect(sourcesCard).toHaveScreenshot('glossary-sourcesCard.png');

    // 属性情報からドメインモデルページに移動
    await domainModelLink.click();
    // ------------------------------------------------------------
    // ドメインモデル
    // ------------------------------------------------------------
    await fastExpect(page).toHaveURL(/domain.html/);

    // ドメインモデルページで「JIGの情報源」のカードが表示されていること
    // 用語からの遷移ではURLにハッシュが含まれる
    const domainSourcesCardHash = new URL(page.url()).hash;
    const domainSourcesCard = page.locator(domainSourcesCardHash);
    await fastExpect(domainSourcesCard).toBeInViewport();
    await expect(domainSourcesCard).toHaveScreenshot('domain-sourcesCard.png');

    // パッケージ関連図が表示されていること
    const diagramTabSection = domainSourcesCard.locator(".tab-content-section", {hasText: 'パッケージ関連図'})
    await expect(diagramTabSection).toHaveScreenshot('domain-sourcesCard-1.png');
    // パッケージ内パッケージ関連図タブをクリックしたらパッケージ内パッケージ関連図が表示されること
    const pkgPkgTab = domainSourcesCard.locator('.jig-tab', {hasText: 'パッケージ内パッケージ関連図'});
    await pkgPkgTab.click();
    await expect(diagramTabSection).toHaveScreenshot('domain-sourcesCard-2.png');
    // パッケージ内クラス関連図タブをクリックしたらパッケージ内クラス関連図が表示されること
    const pkgClassTab = domainSourcesCard.locator('.jig-tab', {hasText: 'パッケージ内クラス関連図'});
    await pkgClassTab.click();
    await expect(diagramTabSection).toHaveScreenshot('domain-sourcesCard-3.png');
});

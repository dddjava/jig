const test = require('node:test');
const assert = require('node:assert/strict');
const {JSDOM} = require('jsdom');

test.describe('jig-dom.js', () => {
    let Jig;

    test.beforeEach(() => {
        // 前のテストの require キャッシュをクリア
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
        delete require.cache[require.resolve('../../main/resources/templates/assets/jig-dom.js')];

        // jsdom でブラウザ環境をセットアップ
        const dom = new JSDOM(`
            <!DOCTYPE html>
            <html>
            <head></head>
            <body>
                <header class="top">
                    <div class="jig-page-title">Page Title</div>
                </header>
                <div id="jig-document-description" class="hidden">Help content</div>
            </body>
            </html>
        `);

        global.window = dom.window;
        global.document = dom.window.document;
        global.location = dom.window.location;

        // モック設定
        global.marked = {
            parse: (text) => `<p>${text}</p>`
        };
        global.navigationData = undefined;

        // グローバルデータをクリア（テスト間での汚染防止）
        delete globalThis.Jig;

        // jig-glossary.js の Jig オブジェクト初期化
        require('../../main/resources/templates/assets/jig-glossary.js');
        require('../../main/resources/templates/assets/jig-dom.js');

        Jig = global.Jig;
    });

    test.describe('createElement', () => {
        test('tagName だけで要素を作成', () => {
            const el = Jig.dom.createElement('div');
            assert.equal(el.tagName, 'DIV');
            assert.equal(el.className, '');
        });

        test('className オプションを適用', () => {
            const el = Jig.dom.createElement('div', {className: 'my-class'});
            assert.equal(el.className, 'my-class');
        });

        test('複数クラス指定', () => {
            const el = Jig.dom.createElement('div', {className: 'class1 class2'});
            assert.equal(el.className, 'class1 class2');
        });

        test('id オプションを適用', () => {
            const el = Jig.dom.createElement('div', {id: 'my-id'});
            assert.equal(el.id, 'my-id');
        });

        test('textContent オプションを適用', () => {
            const el = Jig.dom.createElement('div', {textContent: 'Hello'});
            assert.equal(el.textContent, 'Hello');
        });

        test('innerHTML オプションを適用', () => {
            const el = Jig.dom.createElement('div', {innerHTML: '<strong>Bold</strong>'});
            assert.equal(el.innerHTML, '<strong>Bold</strong>');
        });

        test('attributes オプションを適用', () => {
            const el = Jig.dom.createElement('a', {
                attributes: {href: '#section', 'data-value': '123'}
            });
            assert.equal(el.getAttribute('href'), '#section');
            assert.equal(el.getAttribute('data-value'), '123');
        });

        test('style オプションを適用', () => {
            const el = Jig.dom.createElement('div', {
                style: {color: 'red', fontSize: '14px'}
            });
            assert.equal(el.style.color, 'red');
            assert.equal(el.style.fontSize, '14px');
        });

        test('children 配列（要素）を追加', () => {
            const child1 = Jig.dom.createElement('span', {textContent: 'Child 1'});
            const child2 = Jig.dom.createElement('span', {textContent: 'Child 2'});
            const parent = Jig.dom.createElement('div', {children: [child1, child2]});

            assert.equal(parent.children.length, 2);
            assert.equal(parent.children[0].textContent, 'Child 1');
            assert.equal(parent.children[1].textContent, 'Child 2');
        });

        test('children 配列内の文字列を追加', () => {
            const el = Jig.dom.createElement('div', {children: ['Text1', 'Text2']});
            assert.equal(el.textContent, 'Text1Text2');
        });

        test('children 配列内の null/undefined を無視', () => {
            const child = Jig.dom.createElement('span', {textContent: 'Child'});
            const parent = Jig.dom.createElement('div', {
                children: [null, child, undefined]
            });
            assert.equal(parent.children.length, 1);
        });

        test('複合オプション', () => {
            const child = Jig.dom.createElement('span', {textContent: 'Inner'});
            const el = Jig.dom.createElement('div', {
                className: 'container',
                id: 'main',
                textContent: 'Outer',
                attributes: {role: 'region'},
                style: {display: 'flex'}
            });
            assert.equal(el.className, 'container');
            assert.equal(el.id, 'main');
            assert.equal(el.getAttribute('role'), 'region');
        });
    });

    test.describe('parseMarkdown', () => {
        test('marked.parse が存在する場合、それを使用', () => {
            const result = Jig.dom.parseMarkdown('# Heading');
            assert.equal(result, '<p># Heading</p>');
        });

        test('null 入力は空文字列に変換', () => {
            const result = Jig.dom.parseMarkdown(null);
            assert.equal(result, '<p></p>');
        });

        test('undefined 入力は空文字列に変換', () => {
            const result = Jig.dom.parseMarkdown(undefined);
            assert.equal(result, '<p></p>');
        });

        test('marked.parse が存在しない場合、文字列をそのまま返す', () => {
            const originalMarked = global.marked;
            global.marked = null;
            const result = Jig.dom.parseMarkdown('**bold**');
            assert.equal(result, '**bold**');
            global.marked = originalMarked;
        });
    });

    test.describe('createCell', () => {
        test('テキストのみで <td> を作成', () => {
            const cell = Jig.dom.createCell('Data');
            assert.equal(cell.tagName, 'TD');
            assert.equal(cell.textContent, 'Data');
        });

        test('className を指定', () => {
            const cell = Jig.dom.createCell('123', 'number');
            assert.equal(cell.className, 'number');
            assert.equal(cell.textContent, '123');
        });

        test('className なし', () => {
            const cell = Jig.dom.createCell('Data', undefined);
            assert.equal(cell.className, '');
        });
    });

    test.describe('kind.badgeChar', () => {
        test('パッケージ → "P"', () => {
            assert.equal(Jig.dom.kind.badgeChar('パッケージ'), 'P');
        });

        test('クラス → "C"', () => {
            assert.equal(Jig.dom.kind.badgeChar('クラス'), 'C');
        });

        test('メソッド → "M"', () => {
            assert.equal(Jig.dom.kind.badgeChar('メソッド'), 'M');
        });

        test('フィールド → "F"', () => {
            assert.equal(Jig.dom.kind.badgeChar('フィールド'), 'F');
        });

        test('未知の種別は先頭大文字', () => {
            assert.equal(Jig.dom.kind.badgeChar('interface'), 'I');
        });

        test('null → "?"', () => {
            assert.equal(Jig.dom.kind.badgeChar(null), '?');
        });

        test('空文字列 → "?"', () => {
            assert.equal(Jig.dom.kind.badgeChar(''), '?');
        });
    });

    test.describe('kind.badgeElement', () => {
        test('<span class="kind-badge"> を生成', () => {
            const badge = Jig.dom.kind.badgeElement('クラス');
            assert.equal(badge.tagName, 'SPAN');
            assert.equal(badge.className, 'kind-badge');
            assert.equal(badge.getAttribute('data-kind'), 'クラス');
            assert.equal(badge.textContent, 'C');
        });

        test('未知の kind も正しく処理', () => {
            const badge = Jig.dom.kind.badgeElement('enum');
            assert.equal(badge.getAttribute('data-kind'), 'enum');
            assert.equal(badge.textContent, 'E');
        });
    });

    test.describe('sidebar.createSection', () => {
        test('空の items 配列は null を返す', () => {
            const section = Jig.dom.sidebar.createSection('Title', []);
            assert.equal(section, null);
        });

        test('null items は null を返す', () => {
            const section = Jig.dom.sidebar.createSection('Title', null);
            assert.equal(section, null);
        });

        test('正常系で <section> を生成', () => {
            const items = [
                {id: 'sec1', label: 'Section 1'},
                {id: 'sec2', label: 'Section 2'}
            ];
            const section = Jig.dom.sidebar.createSection('Sections', items);

            assert.equal(section.tagName, 'SECTION');
            assert.equal(section.className, 'in-page-sidebar__section');

            const title = section.querySelector('p');
            assert.ok(title);
            assert.equal(title.className, 'in-page-sidebar__title');
            assert.equal(title.textContent, 'Sections');

            const ul = section.querySelector('ul');
            assert.ok(ul);
            assert.equal(ul.className, 'in-page-sidebar__links');

            const items_el = ul.querySelectorAll('li');
            assert.equal(items_el.length, 2);
            assert.equal(items_el[0].querySelector('a').textContent, 'Section 1');
            assert.equal(items_el[0].querySelector('a').getAttribute('href'), '#sec1');
        });
    });

    test.describe('sidebar.renderSection', () => {
        test('container が null の場合、何もしない', () => {
            const items = [{id: 'item1', label: 'Item'}];
            Jig.dom.sidebar.renderSection(null, 'Title', items);
            // エラーが発生しないことを確認
            assert.ok(true);
        });

        test('container に section を追加', () => {
            const container = Jig.dom.createElement('div');
            const items = [{id: 'item1', label: 'Item 1'}];
            Jig.dom.sidebar.renderSection(container, 'Items', items);

            assert.equal(container.children.length, 1);
            assert.equal(container.children[0].tagName, 'SECTION');
        });

        test('複数回呼び出すと複数セクションが追加', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderSection(container, 'First', [{id: 'a', label: 'A'}]);
            Jig.dom.sidebar.renderSection(container, 'Second', [{id: 'b', label: 'B'}]);

            assert.equal(container.children.length, 2);
        });
    });

    test.describe('type.resolver', () => {
        test('デフォルトは null', () => {
            assert.equal(Jig.dom.type.getResolver(), null);
        });

        test('setResolver で関数を設定', () => {
            const resolver = (fqn) => ({href: `/types/${fqn}`, text: fqn});
            Jig.dom.type.setResolver(resolver);
            assert.equal(Jig.dom.type.getResolver(), resolver);
        });

        test('clearResolver で null にリセット', () => {
            const resolver = (fqn) => ({href: `/types/${fqn}`});
            Jig.dom.type.setResolver(resolver);
            Jig.dom.type.clearResolver();
            assert.equal(Jig.dom.type.getResolver(), null);
        });

        test('非関数を setResolver に渡すと null に設定', () => {
            Jig.dom.type.setResolver('not a function');
            assert.equal(Jig.dom.type.getResolver(), null);
        });
    });

    test.describe('type.elementForRef', () => {
        test('resolver なし、型引数なし → <span> を生成', () => {
            const typeRef = {fqn: 'java.lang.String'};
            const el = Jig.dom.type.elementForRef(typeRef);
            assert.equal(el.tagName, 'SPAN');
            assert.equal(el.textContent, 'String');
        });

        test('resolver あり、型引数なし → <a> を生成', () => {
            Jig.dom.type.setResolver((fqn) => ({
                href: `/types/${fqn}`,
                text: fqn.split('.').pop(),
                className: 'type-link'
            }));
            const typeRef = {fqn: 'java.util.List'};
            const el = Jig.dom.type.elementForRef(typeRef);

            assert.equal(el.tagName, 'A');
            assert.equal(el.getAttribute('href'), '/types/java.util.List');
            assert.equal(el.className, 'type-link');
            assert.equal(el.textContent, 'List');

            Jig.dom.type.clearResolver();
        });

        test('配列型（[] サフィックス）を処理', () => {
            const typeRef = {fqn: 'java.lang.String[]'};
            const el = Jig.dom.type.elementForRef(typeRef);
            assert.equal(el.textContent, 'String[]');
        });

        test('多次元配列型', () => {
            const typeRef = {fqn: 'int[][]'};
            const el = Jig.dom.type.elementForRef(typeRef);
            assert.equal(el.textContent, 'int[][]');
        });

        test('型引数あり → <span> 内にネストした要素を生成', () => {
            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: [
                    {fqn: 'java.lang.String'}
                ]
            };
            const el = Jig.dom.type.elementForRef(typeRef);
            assert.equal(el.tagName, 'SPAN');
            assert.ok(el.textContent.includes('List'));
            assert.ok(el.textContent.includes('String'));
            assert.ok(el.textContent.includes('<'));
            assert.ok(el.textContent.includes('>'));
        });

        test('複数の型引数', () => {
            const typeRef = {
                fqn: 'java.util.Map',
                typeArgumentRefs: [
                    {fqn: 'java.lang.String'},
                    {fqn: 'java.lang.Integer'}
                ]
            };
            const el = Jig.dom.type.elementForRef(typeRef);
            assert.ok(el.textContent.includes('String'));
            assert.ok(el.textContent.includes('Integer'));
            assert.ok(el.textContent.includes(','));
        });

        test('className オプションを適用', () => {
            const typeRef = {fqn: 'java.lang.String'};
            const el = Jig.dom.type.elementForRef(typeRef, 'my-class');
            assert.equal(el.className, 'my-class');
        });
    });

    test.describe('setupSortableTables', () => {
        test('table.sortable に click イベントを付与', () => {
            const tableHtml = `
                <table class="sortable">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Count</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr><td>Alpha</td><td class="number">10</td></tr>
                        <tr><td>Beta</td><td class="number">5</td></tr>
                    </tbody>
                </table>
            `;
            document.body.innerHTML = tableHtml;

            Jig.dom.setupSortableTables();

            const headers = document.querySelectorAll('table.sortable thead th');
            assert.ok(headers[0].style.cursor === 'pointer');
            assert.ok(headers[1].style.cursor === 'pointer');
        });

        test('ヘッダークリックで行がソートされる（文字列昇順）', () => {
            const tableHtml = `
                <table class="sortable">
                    <thead>
                        <tr><th>Name</th></tr>
                    </thead>
                    <tbody>
                        <tr><td>Zebra</td></tr>
                        <tr><td>Apple</td></tr>
                        <tr><td>Mango</td></tr>
                    </tbody>
                </table>
            `;
            document.body.innerHTML = tableHtml;
            Jig.dom.setupSortableTables();

            const header = document.querySelector('table.sortable thead th');
            header.click();

            const rows = document.querySelectorAll('table.sortable tbody tr');
            assert.equal(rows[0].querySelector('td').textContent, 'Apple');
            assert.equal(rows[1].querySelector('td').textContent, 'Mango');
            assert.equal(rows[2].querySelector('td').textContent, 'Zebra');
        });

        test('再度クリックで降順に切り替わる', () => {
            const tableHtml = `
                <table class="sortable">
                    <thead>
                        <tr><th>Name</th></tr>
                    </thead>
                    <tbody>
                        <tr><td>Alpha</td></tr>
                        <tr><td>Beta</td></tr>
                    </tbody>
                </table>
            `;
            document.body.innerHTML = tableHtml;
            Jig.dom.setupSortableTables();

            const header = document.querySelector('table.sortable thead th');
            header.click(); // 昇順
            header.click(); // 降順

            const rows = document.querySelectorAll('table.sortable tbody tr');
            assert.equal(rows[0].querySelector('td').textContent, 'Beta');
            assert.equal(rows[1].querySelector('td').textContent, 'Alpha');
        });

        test('数値列は数値ソート（初回は降順）', () => {
            const tableHtml = `
                <table class="sortable">
                    <thead>
                        <tr><th>Count</th></tr>
                    </thead>
                    <tbody>
                        <tr><td class="number">100</td></tr>
                        <tr><td class="number">20</td></tr>
                        <tr><td class="number">5</td></tr>
                    </tbody>
                </table>
            `;
            document.body.innerHTML = tableHtml;
            Jig.dom.setupSortableTables();

            const header = document.querySelector('table.sortable thead th');
            header.click(); // 初回は降順

            const rows = document.querySelectorAll('table.sortable tbody tr');
            assert.equal(rows[0].querySelector('td').textContent, '100');
            assert.equal(rows[1].querySelector('td').textContent, '20');
            assert.equal(rows[2].querySelector('td').textContent, '5');
        });

        test('no-sort クラスのヘッダーはソート不可', () => {
            const tableHtml = `
                <table class="sortable">
                    <thead>
                        <tr>
                            <th class="no-sort">Action</th>
                            <th>Name</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr><td>Edit</td><td>Beta</td></tr>
                        <tr><td>Edit</td><td>Alpha</td></tr>
                    </tbody>
                </table>
            `;
            document.body.innerHTML = tableHtml;
            Jig.dom.setupSortableTables();

            const headers = document.querySelectorAll('table.sortable thead th');
            assert.equal(headers[0].style.cursor, '');
            assert.equal(headers[1].style.cursor, 'pointer');
        });
    });

    test.describe('initCommonUi - renderMarkdownDescriptions', () => {
        test('.markdown クラスの HTML が marked で変換される', () => {
            const markdownHtml = `
                <div class="markdown"># Heading</div>
                <p>Regular paragraph</p>
                <div class="markdown">**Bold text**</div>
            `;
            document.body.innerHTML = markdownHtml;

            Jig.dom.initCommonUi();

            const markdowns = document.querySelectorAll('.markdown');
            assert.equal(markdowns[0].innerHTML, '<p># Heading</p>');
            assert.equal(markdowns[1].innerHTML, '<p>**Bold text**</p>');
        });
    });

    test.describe('initCommonUi - setupDocumentHelp', () => {
        test('#jig-document-description が存在するとヘルプボタンが生成される', () => {
            // require キャッシュをクリア
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-dom.js')];

            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body>
                    <header class="top">
                        <div class="jig-page-title">Page</div>
                    </header>
                    <div id="jig-document-description" class="hidden">Documentation</div>
                </body>
                </html>
            `);

            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            delete globalThis.Jig;

            require('../../main/resources/templates/assets/jig-glossary.js');
            require('../../main/resources/templates/assets/jig-dom.js');

            global.Jig.dom.initCommonUi();

            const helpButton = document.querySelector('.jig-help-button');
            assert.ok(helpButton);
            assert.equal(helpButton.getAttribute('aria-label'), 'ドキュメントの説明を表示');
        });

        test('#jig-document-description が空の場合、ヘルプボタンは生成されない', () => {
            // require キャッシュをクリア
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-dom.js')];

            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body>
                    <header class="top">
                        <div class="jig-page-title">Page</div>
                    </header>
                    <div id="jig-document-description" class="hidden"></div>
                </body>
                </html>
            `);

            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            delete globalThis.Jig;

            require('../../main/resources/templates/assets/jig-glossary.js');
            require('../../main/resources/templates/assets/jig-dom.js');

            global.Jig.dom.initCommonUi();

            const helpButton = document.querySelector('.jig-help-button');
            assert.equal(helpButton, null);
        });
    });

    test.describe('initCommonUi - setupHeaderNavigation', () => {
        test('navigationData.links からナビゲーションドロップダウンを生成', () => {
            // require キャッシュをクリア
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-dom.js')];

            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body>
                    <header class="top">
                        <div class="jig-page-title">Domain Model</div>
                    </header>
                </body>
                </html>
            `, {url: 'http://example.com/domain.html'});

            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            global.navigationData = {
                links: [
                    {href: 'glossary.html', label: 'Glossary'},
                    {href: './domain.html', label: 'Domain Model'},
                    {href: 'insight.html', label: 'Insight'}
                ]
            };
            delete globalThis.Jig;

            require('../../main/resources/templates/assets/jig-glossary.js');
            require('../../main/resources/templates/assets/jig-dom.js');

            global.Jig.dom.initCommonUi();

            const nav = document.querySelector('.jig-header-nav');
            assert.ok(nav);

            const items = nav.querySelectorAll('.jig-header-nav__item');
            assert.ok(items.length > 0);
        });

        test('body.index クラスがある場合、ナビは生成されない', () => {
            // require キャッシュをクリア
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-dom.js')];

            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body class="index">
                    <header class="top">
                        <div class="jig-page-title">Index</div>
                    </header>
                </body>
                </html>
            `);

            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            global.navigationData = {
                links: [{href: 'index.html', label: 'Index'}]
            };
            delete globalThis.Jig;

            require('../../main/resources/templates/assets/jig-glossary.js');
            require('../../main/resources/templates/assets/jig-dom.js');

            global.Jig.dom.initCommonUi();

            const nav = document.querySelector('.jig-header-nav');
            assert.equal(nav, null);
        });

        test('navigationData がない場合、ナビは生成されない', () => {
            // require キャッシュをクリア
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-glossary.js')];
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-dom.js')];

            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body>
                    <header class="top">
                        <div class="jig-page-title">Page</div>
                    </header>
                </body>
                </html>
            `);

            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            global.navigationData = undefined;
            delete globalThis.Jig;

            require('../../main/resources/templates/assets/jig-glossary.js');
            require('../../main/resources/templates/assets/jig-dom.js');

            global.Jig.dom.initCommonUi();

            const nav = document.querySelector('.jig-header-nav');
            assert.equal(nav, null);
        });
    });
});

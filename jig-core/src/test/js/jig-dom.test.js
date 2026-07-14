const test = require('node:test');
const assert = require('node:assert/strict');
const {JSDOM} = require('jsdom');

// bundle に含まれる順で並べる。reloadJigModules で require.cache クリア → 再 require する。
const ASSET_MODULES = ['jig-util.js', 'jig-data.js', 'jig-glossary.js', 'jig-i18n.js', 'jig-dom.js'];

function modulePath(name) {
    return require.resolve(`../../main/resources/templates/assets/${name}`);
}

function reloadJigModules() {
    ASSET_MODULES.forEach(m => delete require.cache[modulePath(m)]);
    delete globalThis.Jig;
    ASSET_MODULES.forEach(m => require(modulePath(m)));
}

test.describe('jig-dom.js', () => {
    let Jig;

    test.beforeEach(() => {
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
        // jig-dom.js 内の `new Event(...)` が jsdom の EventTarget と同じレルムになるようにする
        global.Event = dom.window.Event;

        global.marked = {
            parse: (text) => `<p>${text}</p>`
        };
        global.DOMPurify = require('dompurify')(dom.window);
        global.navigationData = undefined;

        reloadJigModules();
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

    test.describe('card.type titleSuffix', () => {
        test('titleSuffix を指定すると h3 末尾に追加される', () => {
            const suffix = Jig.dom.createElement('a', {className: 'source-link'});
            const card = Jig.dom.card.type({title: 'タイトル', kind: 'クラス', titleSuffix: suffix});
            const h3 = card.querySelector('h3');
            assert.equal(h3.children[h3.children.length - 1], suffix);
        });

        test('titleSuffix が null の場合は何も追加されない', () => {
            const card = Jig.dom.card.type({title: 'タイトル', kind: 'クラス', titleSuffix: null});
            const h3 = card.querySelector('h3');
            // kindBadge + title の2要素のみ
            assert.equal(h3.children.length, 2);
        });
    });

    test.describe('glossary.sourceLink', () => {
        test.afterEach(() => {
            delete globalThis.glossaryData;
            delete globalThis.summaryData;
        });

        test('blobUrlPrefix と sourcePath が揃う場合はリンク要素を返す', () => {
            globalThis.summaryData = {git: {blobUrlPrefix: 'https://github.com/foo/bar/blob/abc1234'}};
            globalThis.glossaryData = {terms: {}, sourcePaths: {'com.example.MyClass': 'src/main/java/com/example/MyClass.java'}};
            const link = Jig.glossary.sourceLink('com.example.MyClass');
            assert.equal(link.tagName, 'A');
            assert.equal(link.getAttribute('href'), 'https://github.com/foo/bar/blob/abc1234/src/main/java/com/example/MyClass.java');
            assert.equal(link.getAttribute('target'), '_blank');
        });

        test('用語がない型でも sourcePath があればリンク要素を返す', () => {
            globalThis.summaryData = {git: {blobUrlPrefix: 'https://github.com/foo/bar/blob/abc1234'}};
            globalThis.glossaryData = {terms: {}, sourcePaths: {'com.example.NoJavadocClass': 'src/main/java/com/example/NoJavadocClass.java'}};
            const link = Jig.glossary.sourceLink('com.example.NoJavadocClass');
            assert.equal(link.getAttribute('href'), 'https://github.com/foo/bar/blob/abc1234/src/main/java/com/example/NoJavadocClass.java');
        });

        test('メソッド FQN は型の sourcePath にフォールバックして解決する', () => {
            globalThis.summaryData = {git: {blobUrlPrefix: 'https://github.com/foo/bar/blob/abc1234'}};
            globalThis.glossaryData = {terms: {}, sourcePaths: {'com.example.MyClass': 'src/main/java/com/example/MyClass.java'}};
            const link = Jig.glossary.sourceLink('com.example.MyClass#doSomething()');
            assert.equal(link.getAttribute('href'), 'https://github.com/foo/bar/blob/abc1234/src/main/java/com/example/MyClass.java');
        });

        test('blobUrlPrefix がない場合は null を返す', () => {
            globalThis.summaryData = {git: {}};
            globalThis.glossaryData = {terms: {}, sourcePaths: {'com.example.MyClass': 'src/main/java/com/example/MyClass.java'}};
            assert.equal(Jig.glossary.sourceLink('com.example.MyClass'), null);
        });

        test('sourcePath がない場合は null を返す', () => {
            globalThis.summaryData = {git: {blobUrlPrefix: 'https://github.com/foo/bar/blob/abc1234'}};
            globalThis.glossaryData = {terms: {'com.example.MyClass': {title: 'MyClass'}}, sourcePaths: {}};
            assert.equal(Jig.glossary.sourceLink('com.example.MyClass'), null);
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

    test.describe('sanitizeHtml', () => {
        test('DOMPurify がある場合、危険なマークアップを除去する', () => {
            const result = Jig.dom.sanitizeHtml('<p>ok</p><img src=x onerror=alert(1)>');
            assert.equal(result.includes('onerror'), false);
            assert.equal(result.includes('<p>ok</p>'), true);
        });

        test('DOMPurify が無い場合、null を返す', () => {
            const original = global.DOMPurify;
            global.DOMPurify = undefined;
            try {
                assert.equal(Jig.dom.sanitizeHtml('<p>ok</p>'), null);
            } finally {
                global.DOMPurify = original;
            }
        });
    });

    test.describe('createMarkdownElement', () => {
        test('DIV.markdown を生成する', () => {
            const el = Jig.dom.createMarkdownElement('plain');
            assert.equal(el.tagName, 'DIV');
            assert.equal(el.className, 'markdown');
        });

        test('parseMarkdown の結果を innerHTML に設定する', () => {
            const el = Jig.dom.createMarkdownElement('# Heading');
            assert.equal(el.innerHTML, '<p># Heading</p>');
        });

        test('スクリプト実行につながるマークアップはサニタイズされる', () => {
            const el = Jig.dom.createMarkdownElement('<img src=x onerror=alert(1)><script>alert(2)</script>text');
            assert.equal(el.innerHTML.includes('onerror'), false);
            assert.equal(el.innerHTML.includes('<script'), false);
            assert.equal(el.textContent.includes('text'), true);
        });

        test('DOMPurify が無い場合、innerHTML ではなく textContent として扱う', () => {
            const original = global.DOMPurify;
            global.DOMPurify = undefined;
            try {
                const el = Jig.dom.createMarkdownElement('<img src=x onerror=alert(1)>text');
                assert.equal(el.querySelector('img'), null);
                assert.equal(el.textContent, '<img src=x onerror=alert(1)>text');
            } finally {
                global.DOMPurify = original;
            }
        });

        test('Jig.mermaid があれば mermaid 変換を生成要素に対して呼ぶ', () => {
            let received = null;
            globalThis.Jig.mermaid = {
                renderMarkdownDiagrams: (element) => {
                    received = element;
                }
            };
            try {
                const el = Jig.dom.createMarkdownElement('text');
                assert.equal(received, el);
            } finally {
                delete globalThis.Jig.mermaid;
            }
        });

        test('Jig.mermaid が無くても例外にならない', () => {
            assert.doesNotThrow(() => Jig.dom.createMarkdownElement('text'));
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

    test.describe('sidebar.section', () => {
        test('空の items 配列は null を返す', () => {
            const section = Jig.dom.sidebar.section([]);
            assert.equal(section, null);
        });

        test('null items は null を返す', () => {
            const section = Jig.dom.sidebar.section(null);
            assert.equal(section, null);
        });

        test('正常系で見出しなしの <section> を生成', () => {
            const items = [
                {id: 'sec1', label: 'Section 1'},
                {id: 'sec2', label: 'Section 2'}
            ];
            const section = Jig.dom.sidebar.section(items);

            assert.equal(section.tagName, 'SECTION');
            assert.equal(section.className, 'in-page-sidebar__section');
            assert.equal(section.querySelector('p'), null);

            const ul = section.querySelector('ul');
            assert.ok(ul);
            assert.equal(ul.className, 'in-page-sidebar__links');

            const items_el = ul.querySelectorAll('li');
            assert.equal(items_el.length, 2);
            assert.equal(items_el[0].querySelector('a').textContent, 'Section 1');
            assert.equal(items_el[0].querySelector('a').getAttribute('href'), '#sec1');
        });
    });

    test.describe('sidebar.leaf', () => {
        test('href とラベルを持つliを生成する', () => {
            const leaf = Jig.dom.sidebar.leaf('#a', 'A');
            assert.equal(leaf.tagName, 'LI');
            assert.equal(leaf.className, 'in-page-sidebar__item');
            const link = leaf.querySelector('a');
            assert.equal(link.className, 'in-page-sidebar__link');
            assert.equal(link.getAttribute('href'), '#a');
            assert.equal(link.textContent, 'A');
        });
    });

    test.describe('sidebar.renderSection', () => {
        test('container が null の場合、何もしない', () => {
            const items = [{id: 'item1', label: 'Item'}];
            Jig.dom.sidebar.renderSection(null, items);
            // エラーが発生しないことを確認
            assert.ok(true);
        });

        test('container に見出しなしの section を追加', () => {
            const container = Jig.dom.createElement('div');
            const items = [{id: 'item1', label: 'Item 1'}];
            Jig.dom.sidebar.renderSection(container, items);

            assert.equal(container.children.length, 1);
            assert.equal(container.children[0].tagName, 'SECTION');
            assert.equal(container.children[0].querySelector('p'), null);
        });

        test('複数回呼び出すと複数セクションが追加', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderSection(container, [{id: 'a', label: 'A'}]);
            Jig.dom.sidebar.renderSection(container, [{id: 'b', label: 'B'}]);

            assert.equal(container.children.length, 2);
        });
    });

    test.describe('sidebar.renderTreeSection', () => {
        const renderLeaf = item => Jig.dom.createElement('li', {
            className: 'in-page-sidebar__item',
            children: [Jig.dom.createElement('a', {
                className: 'in-page-sidebar__link',
                attributes: {href: '#' + item.fqn},
                textContent: item.fqn
            })]
        });
        const options = {title: 'グループ', getFqn: item => item.fqn, renderLeaf};

        test('container が null または items が空の場合、何もしない', () => {
            assert.doesNotThrow(() => Jig.dom.sidebar.renderTreeSection(null, {...options, items: [{fqn: 'com.example.A'}]}));
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: []});
            assert.equal(container.children.length, 0);
        });

        test('showTitle: false の場合、見出し・背景色・ピン留めなしでul.in-page-sidebar__linksを直接追加する', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, showTitle: false, items: [{fqn: 'com.example.A'}]});

            assert.equal(container.children.length, 1);
            const list = container.children[0];
            assert.equal(list.tagName, 'UL');
            assert.equal(list.className, 'in-page-sidebar__links');
            assert.equal(container.querySelectorAll('.in-page-sidebar__section--group').length, 0);
            assert.equal(container.querySelectorAll('.in-page-sidebar__title--group').length, 0);
            assert.ok(container.querySelector('a[href="#com.example.A"]'));
        });

        test('グループは背景区別用のクラスを持つ折りたたみ可能なsectionになる', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [{fqn: 'com.example.A'}]});

            assert.equal(container.children.length, 1);
            const section = container.children[0];
            assert.equal(section.tagName, 'SECTION');
            assert.equal(section.className, 'in-page-sidebar__section in-page-sidebar__section--group');
            const title = section.querySelector('p.in-page-sidebar__title--group');
            assert.ok(title);
            assert.ok(title.classList.contains('in-page-sidebar__title--collapsible'));
            assert.equal(title.querySelector('span').textContent, 'グループ');
            assert.ok(title.querySelector('button.in-page-sidebar__toggle'));
        });

        test('用語のない単児チェーンのパッケージは最深の1ノードのみ表示される', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [{fqn: 'com.example.app.A'}]});

            const headers = container.querySelectorAll('.in-page-sidebar__item-header');
            assert.equal(headers.length, 1);
            assert.equal(headers[0].querySelector('span').textContent, 'app');
            assert.ok(container.querySelector('a[href="#com.example.app.A"]'));
        });

        test('用語が設定された中間パッケージは省略されず階層が分かれる', () => {
            globalThis.glossaryData = {terms: {'com.example': {title: '用語パッケージ', kind: 'パッケージ', description: ''}}};
            try {
                const container = Jig.dom.createElement('div');
                Jig.dom.sidebar.renderTreeSection(container, {...options, items: [{fqn: 'com.example.app.A'}]});

                const headers = container.querySelectorAll('.in-page-sidebar__item-header');
                assert.equal(headers.length, 2);
                assert.equal(headers[0].querySelector('span').textContent, '用語パッケージ');
                assert.equal(headers[1].querySelector('span').textContent, 'app');
            } finally {
                delete globalThis.glossaryData;
            }
        });

        test('分岐するパッケージはネストしたliになる', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [
                {fqn: 'com.example.foo.A'},
                {fqn: 'com.example.bar.B'},
            ]});

            const headers = container.querySelectorAll('.in-page-sidebar__item-header');
            assert.equal(headers[0].querySelector('span').textContent, 'example');
            assert.equal(headers[1].querySelector('span').textContent, 'bar');
            assert.equal(headers[2].querySelector('span').textContent, 'foo');
        });

        test('packageHref を指定するとパッケージノードがリンクになる', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {
                ...options,
                items: [{fqn: 'com.example.A'}],
                packageHref: node => '#pkg-' + node.fqn
            });

            const link = container.querySelector('.in-page-sidebar__item-header a.in-page-sidebar__package-link');
            assert.ok(link);
            assert.equal(link.getAttribute('href'), '#pkg-com.example');
            assert.equal(link.textContent, 'example');
        });

        test('トグルで子リストを折りたたむ', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [{fqn: 'com.example.A'}]});

            const toggle = container.querySelector('.in-page-sidebar__title--group .in-page-sidebar__toggle');
            const list = container.querySelector('ul');
            toggle.dispatchEvent(new window.Event('click'));
            assert.ok(list.classList.contains('in-page-sidebar__links--hidden'));
            assert.equal(toggle.getAttribute('aria-expanded'), 'false');
        });

        test('上位階層を閉じると配下もすべて閉じ、再度開くと1階層だけ開く', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [
                {fqn: 'com.example.foo.A'},
                {fqn: 'com.example.bar.B'},
            ]});

            const groupToggle = container.querySelector('.in-page-sidebar__title--group .in-page-sidebar__toggle');
            const allLists = [...container.querySelectorAll('ul.in-page-sidebar__links')];
            const groupList = allLists[0];
            const descendantLists = allLists.slice(1);
            assert.ok(descendantLists.length > 0);

            // 上位階層を閉じると配下のリストもすべて閉じ、トグルのariaも同期する
            groupToggle.dispatchEvent(new window.Event('click'));
            allLists.forEach(list => assert.ok(list.classList.contains('in-page-sidebar__links--hidden')));
            container.querySelectorAll('.in-page-sidebar__item-header .in-page-sidebar__toggle')
                .forEach(toggle => assert.equal(toggle.getAttribute('aria-expanded'), 'false'));

            // 再度開くと直下の1階層だけ開き、配下は閉じたまま
            groupToggle.dispatchEvent(new window.Event('click'));
            assert.ok(!groupList.classList.contains('in-page-sidebar__links--hidden'));
            descendantLists.forEach(list => assert.ok(list.classList.contains('in-page-sidebar__links--hidden')));
        });

        test('Alt+クリックで開くと配下もすべて開く', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [
                {fqn: 'com.example.foo.A'},
                {fqn: 'com.example.bar.B'},
            ]});

            const groupToggle = container.querySelector('.in-page-sidebar__title--group .in-page-sidebar__toggle');
            const allLists = [...container.querySelectorAll('ul.in-page-sidebar__links')];
            assert.ok(allLists.length > 1);

            groupToggle.dispatchEvent(new window.Event('click'));
            groupToggle.dispatchEvent(new window.MouseEvent('click', {altKey: true}));
            allLists.forEach(list => assert.ok(!list.classList.contains('in-page-sidebar__links--hidden')));
            container.querySelectorAll('.in-page-sidebar__item-header .in-page-sidebar__toggle')
                .forEach(toggle => assert.equal(toggle.getAttribute('aria-expanded'), 'true'));
        });

        test('グループ見出しに下部積み重ね用のオフセットが設定される', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [{fqn: 'com.a.A'}]});
            Jig.dom.sidebar.renderTreeSection(container, {...options, title: 'グループ2', items: [{fqn: 'com.b.B'}]});

            const titles = container.querySelectorAll('.in-page-sidebar__title--group');
            assert.equal(titles.length, 2);
            assert.equal(titles[0].style.bottom, 'calc(1 * var(--group-title-height))');
            assert.equal(titles[1].style.bottom, 'calc(0 * var(--group-title-height))');
        });

        test('内容がスクロール範囲外のグループ見出しはピン留め状態になり、クリックで展開位置へスクロールする', () => {
            const container = Jig.dom.createElement('div');
            document.body.appendChild(container);
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [{fqn: 'com.example.A'}]});

            const title = container.querySelector('.in-page-sidebar__title--group');
            const list = container.querySelector('ul.in-page-sidebar__links');

            // 内容（ul）がスクロール範囲の下にあるレイアウトを再現
            container.getBoundingClientRect = () => ({top: 0, bottom: 300, height: 300});
            title.getBoundingClientRect = () => ({top: 268, bottom: 300, height: 32});
            list.getBoundingClientRect = () => ({top: 500, bottom: 600, height: 100});

            container.dispatchEvent(new window.Event('scroll'));
            assert.ok(title.classList.contains('in-page-sidebar__title--pinned'));

            // クリックで見出しが上部に来る位置までスクロールする
            // （リスト位置500 - 領域上端0 - 見出し高さ32 - gap（jsdomでは算出不可のため0） = 468）
            title.dispatchEvent(new window.Event('click'));
            assert.equal(container.scrollTop, 468);
        });

        test('折りたたまれたグループは見出しクリックで展開される', () => {
            const container = Jig.dom.createElement('div');
            document.body.appendChild(container);
            Jig.dom.sidebar.renderTreeSection(container, {...options, items: [{fqn: 'com.example.A'}]});

            const title = container.querySelector('.in-page-sidebar__title--group');
            const toggle = title.querySelector('.in-page-sidebar__toggle');
            const list = container.querySelector('ul.in-page-sidebar__links');
            toggle.dispatchEvent(new window.Event('click'));
            assert.ok(list.classList.contains('in-page-sidebar__links--hidden'));

            title.dispatchEvent(new window.Event('click'));
            assert.ok(!list.classList.contains('in-page-sidebar__links--hidden'));
            assert.equal(toggle.getAttribute('aria-expanded'), 'true');
        });
    });

    test.describe('sidebar.renderLinkGroup', () => {
        test('containerがnullの場合、何もしない', () => {
            assert.doesNotThrow(() => Jig.dom.sidebar.renderLinkGroup(null, {title: 'A', href: '#a'}));
        });

        test('他のグループと同じ見出しクラスを持つ単一リンクのsectionを追加する', () => {
            const container = Jig.dom.createElement('div');
            Jig.dom.sidebar.renderLinkGroup(container, {title: '永続化(CRUD)', href: '#outbound-crud-panel'});

            assert.equal(container.children.length, 1);
            const section = container.children[0];
            assert.equal(section.className, 'in-page-sidebar__section in-page-sidebar__section--group');
            const title = section.querySelector('p.in-page-sidebar__title--group');
            assert.ok(title);
            const link = title.querySelector('a.in-page-sidebar__link');
            assert.ok(link);
            assert.equal(link.getAttribute('href'), '#outbound-crud-panel');
            assert.equal(link.textContent, '永続化(CRUD)');
            // 開閉トグルやサブリストは持たない（単一リンクなので展開の必要がない）
            assert.equal(section.querySelector('.in-page-sidebar__toggle'), null);
            assert.equal(section.querySelector('ul'), null);
        });

        test('renderTreeSectionのグループと積み重ねオフセットを共有する', () => {
            const container = Jig.dom.createElement('div');
            document.body.appendChild(container);
            Jig.dom.sidebar.renderTreeSection(container, {
                title: 'グループ1',
                items: [{fqn: 'com.example.A'}],
                getFqn: item => item.fqn,
                renderLeaf: item => Jig.dom.sidebar.leaf('#' + item.fqn, item.fqn)
            });
            Jig.dom.sidebar.renderLinkGroup(container, {title: 'リンクグループ', href: '#link'});

            const titles = container.querySelectorAll('.in-page-sidebar__title--group');
            assert.equal(titles.length, 2);
            assert.equal(titles[0].style.bottom, 'calc(1 * var(--group-title-height))');
            assert.equal(titles[1].style.bottom, 'calc(0 * var(--group-title-height))');
        });
    });

    test.describe('sidebar.syncActiveLink', () => {
        function setupSidebar(innerHtml) {
            document.body.innerHTML =
                `<nav class="in-page-sidebar"><div class="in-page-sidebar__list">${innerHtml}</div></nav>`;
        }

        test('hash がない場合は active を付与しない', () => {
            setupSidebar('<a class="in-page-sidebar__link" href="#a">A</a>');
            location.hash = '';
            Jig.dom.sidebar.syncActiveLink();
            assert.equal(document.querySelector('.in-page-sidebar__link--active'), null);
        });

        test('hash に一致するリンクへ active を付与', () => {
            setupSidebar('<a class="in-page-sidebar__link" href="#a">A</a>'
                + '<a class="in-page-sidebar__link" href="#b">B</a>');
            location.hash = '#b';
            Jig.dom.sidebar.syncActiveLink();
            const active = document.querySelectorAll('.in-page-sidebar__link--active');
            assert.equal(active.length, 1);
            assert.equal(active[0].getAttribute('href'), '#b');
        });

        test('一致するリンクがなければ active を付与しない', () => {
            setupSidebar('<a class="in-page-sidebar__link" href="#a">A</a>');
            location.hash = '#zzz';
            Jig.dom.sidebar.syncActiveLink();
            assert.equal(document.querySelector('.in-page-sidebar__link--active'), null);
        });

        test('パッケージリンクにも active を付与する', () => {
            setupSidebar('<a class="in-page-sidebar__package-link" href="#package_x">pkg</a>'
                + '<a class="in-page-sidebar__link" href="#a">A</a>');
            location.hash = '#package_x';
            Jig.dom.sidebar.syncActiveLink();
            const active = document.querySelectorAll('.in-page-sidebar__link--active');
            assert.equal(active.length, 1);
            assert.equal(active[0].getAttribute('href'), '#package_x');
        });

        test('同一アンカーへの複数リンクは全て active になる', () => {
            setupSidebar('<a class="in-page-sidebar__link" href="#a">A(グループ1)</a>'
                + '<a class="in-page-sidebar__link" href="#a">A(グループ2)</a>');
            location.hash = '#a';
            Jig.dom.sidebar.syncActiveLink();
            assert.equal(document.querySelectorAll('.in-page-sidebar__link--active').length, 2);
        });

        test('再同期で以前の active を解除する', () => {
            setupSidebar('<a class="in-page-sidebar__link" href="#a">A</a>'
                + '<a class="in-page-sidebar__link" href="#b">B</a>');
            location.hash = '#a';
            Jig.dom.sidebar.syncActiveLink();
            location.hash = '#b';
            Jig.dom.sidebar.syncActiveLink();
            const active = document.querySelectorAll('.in-page-sidebar__link--active');
            assert.equal(active.length, 1);
            assert.equal(active[0].getAttribute('href'), '#b');
        });

        test('折りたたまれた祖先を展開しトグルの状態も更新する', () => {
            setupSidebar(`
                <section class="in-page-sidebar__section">
                    <p class="in-page-sidebar__title in-page-sidebar__title--collapsible">
                        <span>Pkg</span>
                        <button class="in-page-sidebar__toggle" aria-expanded="false" aria-label="展開"></button>
                    </p>
                    <ul class="in-page-sidebar__links in-page-sidebar__links--hidden">
                        <li><a class="in-page-sidebar__link" href="#child">Child</a></li>
                    </ul>
                </section>
            `);
            location.hash = '#child';
            Jig.dom.sidebar.syncActiveLink();

            const ul = document.querySelector('.in-page-sidebar__links');
            assert.ok(!ul.classList.contains('in-page-sidebar__links--hidden'));
            const toggle = document.querySelector('.in-page-sidebar__toggle');
            assert.equal(toggle.getAttribute('aria-expanded'), 'true');
            assert.equal(toggle.getAttribute('aria-label'), '折りたたむ');
        });

        test('サイドバーが無くてもエラーにならない', () => {
            document.body.innerHTML = '';
            location.hash = '#a';
            assert.doesNotThrow(() => Jig.dom.sidebar.syncActiveLink());
        });
    });

    test.describe('sidebar.initClickHighlight', () => {
        function setupSidebar(innerHtml) {
            document.body.innerHTML =
                `<nav class="in-page-sidebar"><div class="in-page-sidebar__list">${innerHtml}</div></nav>`;
            return document.querySelector('.in-page-sidebar');
        }

        test('クリック時、hashが未更新でもクリックしたリンクへ即座にactiveを付与する', () => {
            const sidebar = setupSidebar('<a class="in-page-sidebar__link" href="#a">A</a>'
                + '<a class="in-page-sidebar__link" href="#b">B</a>');
            location.hash = '';
            Jig.dom.sidebar.initClickHighlight(sidebar);

            sidebar.querySelector('a[href="#b"]').click();

            const active = document.querySelectorAll('.in-page-sidebar__link--active');
            assert.equal(active.length, 1);
            assert.equal(active[0].getAttribute('href'), '#b');
        });

        test('サイドバーが無くてもエラーにならない', () => {
            assert.doesNotThrow(() => Jig.dom.sidebar.initClickHighlight(null));
        });

        test('同じサイドバーへ二重初期化してもリスナーは1つだけ登録される', () => {
            const sidebar = setupSidebar('<a class="in-page-sidebar__link" href="#a">A</a>');
            Jig.dom.sidebar.initClickHighlight(sidebar);
            Jig.dom.sidebar.initClickHighlight(sidebar);

            sidebar.querySelector('a[href="#a"]').click();
            assert.equal(document.querySelectorAll('.in-page-sidebar__link--active').length, 1);
        });
    });

    test.describe('sidebar.initAltKeyIndicator', () => {
        test.beforeEach(() => {
            delete document.body.dataset.altKeyIndicatorInitialized;
            document.body.classList.remove('jig-alt-held');
        });

        test('Altキー押下でbodyにjig-alt-heldクラスが付与され、離すと外れる', () => {
            Jig.dom.sidebar.initAltKeyIndicator();

            window.dispatchEvent(new window.KeyboardEvent('keydown', {key: 'Alt'}));
            assert.ok(document.body.classList.contains('jig-alt-held'));

            window.dispatchEvent(new window.KeyboardEvent('keyup', {key: 'Alt'}));
            assert.ok(!document.body.classList.contains('jig-alt-held'));
        });

        test('Alt以外のキーでは反応しない', () => {
            Jig.dom.sidebar.initAltKeyIndicator();

            window.dispatchEvent(new window.KeyboardEvent('keydown', {key: 'Shift'}));
            assert.ok(!document.body.classList.contains('jig-alt-held'));
        });

        test('ウィンドウがblurするとjig-alt-heldが解除される', () => {
            Jig.dom.sidebar.initAltKeyIndicator();

            window.dispatchEvent(new window.KeyboardEvent('keydown', {key: 'Alt'}));
            window.dispatchEvent(new window.Event('blur'));
            assert.ok(!document.body.classList.contains('jig-alt-held'));
        });

        test('二重初期化してもリスナーは1つだけ登録される', () => {
            Jig.dom.sidebar.initAltKeyIndicator();
            Jig.dom.sidebar.initAltKeyIndicator();

            window.dispatchEvent(new window.KeyboardEvent('keydown', {key: 'Alt'}));
            window.dispatchEvent(new window.KeyboardEvent('keyup', {key: 'Alt'}));
            assert.ok(!document.body.classList.contains('jig-alt-held'));
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

    test.describe('type.refElement', () => {
        test('resolver なし、型引数なし → <span> を生成', () => {
            const typeRef = {fqn: 'java.lang.String'};
            const el = Jig.dom.type.refElement(typeRef);
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
            const el = Jig.dom.type.refElement(typeRef);

            assert.equal(el.tagName, 'A');
            assert.equal(el.getAttribute('href'), '/types/java.util.List');
            assert.equal(el.className, 'type-link');
            assert.equal(el.textContent, 'List');

            Jig.dom.type.clearResolver();
        });

        test('配列型（[] サフィックス）を処理', () => {
            const typeRef = {fqn: 'java.lang.String[]'};
            const el = Jig.dom.type.refElement(typeRef);
            assert.equal(el.textContent, 'String[]');
        });

        test('多次元配列型', () => {
            const typeRef = {fqn: 'int[][]'};
            const el = Jig.dom.type.refElement(typeRef);
            assert.equal(el.textContent, 'int[][]');
        });

        test('型引数あり → <span> 内にネストした要素を生成', () => {
            const typeRef = {
                fqn: 'java.util.List',
                typeArgumentRefs: [
                    {fqn: 'java.lang.String'}
                ]
            };
            const el = Jig.dom.type.refElement(typeRef);
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
            const el = Jig.dom.type.refElement(typeRef);
            assert.ok(el.textContent.includes('String'));
            assert.ok(el.textContent.includes('Integer'));
            assert.ok(el.textContent.includes(','));
        });

        test('className オプションを適用', () => {
            const typeRef = {fqn: 'java.lang.String'};
            const el = Jig.dom.type.refElement(typeRef, 'my-class');
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

    test.describe('initCommonUi', () => {
        test('既存 .markdown 要素は再変換しない（Markdownは描画前に変換する）', () => {
            const markdownHtml = `
                <div class="markdown"># Heading</div>
                <p>Regular paragraph</p>
                <div class="markdown">**Bold text**</div>
            `;
            document.body.innerHTML = markdownHtml;

            Jig.dom.initCommonUi();

            const markdowns = document.querySelectorAll('.markdown');
            assert.equal(markdowns[0].innerHTML, '# Heading');
            assert.equal(markdowns[1].innerHTML, '**Bold text**');
        });

        test('domainData がある場合、domain型にリンクが設定される', () => {
            globalThis.domainData = {types: [{fqn: 'com.example.Order', isDeprecated: false}]};
            globalThis.glossaryData = {terms: {'com.example.Order': {title: '注文'}}};

            Jig.dom.initCommonUi();

            const resolver = Jig.dom.type.getResolver();
            assert.ok(resolver, 'リゾルバーが設定されていること');
            const resolved = resolver('com.example.Order');
            assert.ok(resolved.href.includes('domain'), 'domain.htmlへのリンクであること');

            delete globalThis.domainData;
            delete globalThis.glossaryData;
        });

        test('usecaseData がある型はusecase.htmlへのリンクが設定される', () => {
            globalThis.usecaseData = {usecases: [{fqn: 'com.example.ServiceA'}]};
            globalThis.glossaryData = {terms: {}};

            Jig.dom.initCommonUi();

            const resolver = Jig.dom.type.getResolver();
            assert.ok(resolver, 'リゾルバーが設定されていること');
            const resolved = resolver('com.example.ServiceA');
            assert.ok(resolved && resolved.href.includes('usecase'), 'usecase.htmlへのリンクであること');

            delete globalThis.usecaseData;
            delete globalThis.glossaryData;
        });

        test('どのデータにもない型は weak クラスと短縮名を返す', () => {
            globalThis.domainData = {types: []};
            globalThis.usecaseData = {usecases: []};

            Jig.dom.initCommonUi();

            const resolver = Jig.dom.type.getResolver();
            assert.ok(resolver, 'リゾルバーが設定されていること');
            const result = resolver('java.lang.String');
            assert.equal(result.className, 'weak');
            assert.equal(result.text, 'String');

            delete globalThis.domainData;
            delete globalThis.usecaseData;
        });

        test('domainData も usecaseData も glossaryData もない場合はリゾルバーは null のまま', () => {
            Jig.dom.initCommonUi();

            assert.equal(Jig.dom.type.getResolver(), null);
        });
    });

    test.describe('initCommonUi - setupDocumentHelp', () => {
        test('#jig-document-description が存在するとヘルプボタンが生成される', () => {
            // require キャッシュをクリア
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-util.js')];
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-data.js')];
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

            require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        require('../../main/resources/templates/assets/jig-glossary.js');
            require('../../main/resources/templates/assets/jig-dom.js');

            global.Jig.dom.initCommonUi();

            const helpButton = document.querySelector('.jig-help-button');
            assert.ok(helpButton);
            assert.equal(helpButton.getAttribute('aria-label'), 'ドキュメントの説明を表示');
        });

        test('#jig-document-description が空の場合、ヘルプボタンは生成されない', () => {
            // require キャッシュをクリア
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-util.js')];
            delete require.cache[require.resolve('../../main/resources/templates/assets/jig-data.js')];
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

            require('../../main/resources/templates/assets/jig-util.js');
        require('../../main/resources/templates/assets/jig-data.js');
        require('../../main/resources/templates/assets/jig-glossary.js');
            require('../../main/resources/templates/assets/jig-dom.js');

            global.Jig.dom.initCommonUi();

            const helpButton = document.querySelector('.jig-help-button');
            assert.equal(helpButton, null);
        });
    });

    test.describe('parameterElement', () => {
        test('nameSource が METHOD_PARAMETERS の場合、名前と型を表示', () => {
            const param = {name: 'orderId', nameSource: 'METHOD_PARAMETERS', typeRef: {fqn: 'com.example.OrderId'}};
            const el = Jig.dom.type.parameterElement(param);
            assert.ok(el.textContent.includes('orderId'));
            assert.ok(el.textContent.includes('OrderId'));
        });

        test('nameSource が POSITIONAL の場合、型のみ表示（名前は非表示）', () => {
            const param = {name: 'arg0', nameSource: 'POSITIONAL', typeRef: {fqn: 'com.example.OrderId'}};
            const el = Jig.dom.type.parameterElement(param);
            assert.ok(!el.textContent.includes('arg0'));
            assert.ok(el.textContent.includes('OrderId'));
        });
    });

    test.describe('initCommonUi - setupHeaderNavigation', () => {
        test('navigationData.links からナビゲーションドロップダウンを生成', () => {
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
            global.CustomEvent = dom.window.CustomEvent;
            global.navigationData = {
                links: [
                    {href: 'glossary.html', label: 'Glossary'},
                    {href: './domain.html', label: 'Domain Model'},
                    {href: 'insight.html', label: 'Insight'}
                ]
            };

            reloadJigModules();
            global.Jig.dom.initCommonUi();

            const nav = document.querySelector('.jig-header-nav');
            assert.ok(nav);

            const items = nav.querySelectorAll('.jig-header-nav__item');
            assert.ok(items.length > 0);
        });

        test('body.index クラスがある場合、ナビは生成されない', () => {
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
            global.CustomEvent = dom.window.CustomEvent;
            global.navigationData = {
                links: [{href: 'index.html', label: 'Index'}]
            };

            reloadJigModules();
            global.Jig.dom.initCommonUi();

            const nav = document.querySelector('.jig-header-nav:not(.jig-lang-switcher)');
            assert.equal(nav, null);
        });

        test('navigationData がない場合、ナビは生成されない', () => {
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
            global.CustomEvent = dom.window.CustomEvent;
            global.navigationData = undefined;

            reloadJigModules();
            global.Jig.dom.initCommonUi();

            const nav = document.querySelector('.jig-header-nav:not(.jig-lang-switcher)');
            assert.equal(nav, null);
        });

        test('nav の trigger とリンクに data-i18n が付与される', () => {
            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body>
                    <header class="top">
                        <div class="jig-page-title">ドメインモデル</div>
                    </header>
                </body>
                </html>
            `, {url: 'http://example.com/domain.html'});

            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            global.CustomEvent = dom.window.CustomEvent;
            global.navigationData = {
                locale: 'ja',
                links: [
                    {href: 'glossary.html', label: '用語集'},
                    {href: 'domain.html', label: 'ドメインモデル'}
                ]
            };

            reloadJigModules();
            global.Jig.dom.initCommonUi();

            const trigger = document.querySelector('.jig-header-nav__trigger');
            assert.ok(trigger);
            assert.ok(trigger.hasAttribute('data-i18n'));

            const navRoot = document.querySelector('.jig-header-nav:not(.jig-lang-switcher)');
            const linkEls = navRoot.querySelectorAll('.jig-header-nav__dropdown a, .jig-header-nav__dropdown span');
            assert.ok(linkEls.length > 0);
            linkEls.forEach(el => assert.ok(el.hasAttribute('data-i18n')));
        });
    });

    test.describe('initCommonUi - setupLanguageSwitcher', () => {
        test('ヘッダ右上に言語スイッチャーが生成される', () => {
            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body>
                    <header class="top">
                        <div class="jig-page-title">ドメインモデル</div>
                    </header>
                </body>
                </html>
            `, {url: 'http://example.com/domain.html'});
            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            global.CustomEvent = dom.window.CustomEvent;
            global.navigationData = {locale: 'ja', links: []};

            reloadJigModules();
            global.Jig.dom.initCommonUi();

            const switcher = document.querySelector('.jig-lang-switcher');
            assert.ok(switcher);
            const items = switcher.querySelectorAll('.jig-header-nav__item');
            assert.equal(items.length, 2);
        });

        test('スイッチャーのクリックで setLanguage が呼ばれ trigger が更新される', () => {
            const dom = new JSDOM(`
                <!DOCTYPE html>
                <html>
                <body>
                    <header class="top">
                        <div class="jig-page-title">ドメインモデル</div>
                    </header>
                    <h1 data-i18n>インサイト</h1>
                </body>
                </html>
            `, {url: 'http://example.com/domain.html'});
            global.window = dom.window;
            global.document = dom.window.document;
            global.location = dom.window.location;
            global.CustomEvent = dom.window.CustomEvent;
            global.navigationData = {
                locale: 'ja',
                links: [],
            };

            reloadJigModules();
            global.Jig.dom.initCommonUi();

            const switcher = document.querySelector('.jig-lang-switcher');
            const trigger = switcher.querySelector('.jig-header-nav__trigger');
            assert.equal(trigger.textContent, '日本語');

            const enLink = Array.from(switcher.querySelectorAll('a')).find(a => a.getAttribute('data-lang') === 'en');
            assert.ok(enLink);
            enLink.click();

            assert.equal(global.Jig.i18n.currentLanguage(), 'en');
            assert.equal(trigger.textContent, 'English');
            assert.equal(document.querySelector('h1').textContent, 'Insight');
        });
    });

    test.describe('escapeCsvValue', () => {
        test('CSV値はクォートし、改行とダブルクォートを処理する', () => {
            assert.equal(Jig.dom.escapeCsvValue('"a"\r\nline'), '"""a""\nline"');
            assert.equal(Jig.dom.escapeCsvValue(null), '""');
        });
    });

    test.describe('depthControl', () => {
        test('buildOptions: 集約オプションを組み立てる', () => {
            const options = Jig.dom.depthControl.buildOptions(2);

            assert.deepEqual(options, [
                {value: '0', text: '集約なし'},
                {value: '1', text: '深さ1'},
                {value: '2', text: '深さ2'},
            ]);
        });

        test('renderOptions: 選択肢を描画し値を範囲内に丸める', () => {
            const select = document.createElement('select');
            Jig.dom.depthControl.renderOptions(select, 2, 5);

            assert.deepEqual(Array.from(select.options).map(o => o.value), ['0', '1', '2']);
            assert.equal(select.value, '2');
        });

        test('updateButtonStates: 両端でボタンを無効化する', () => {
            const select = document.createElement('select');
            Jig.dom.depthControl.renderOptions(select, 2, 0);
            const upButton = document.createElement('button');
            const downButton = document.createElement('button');

            Jig.dom.depthControl.updateButtonStates(select, upButton, downButton);
            assert.equal(upButton.disabled, true);
            assert.equal(downButton.disabled, false);

            select.value = '2';
            Jig.dom.depthControl.updateButtonStates(select, upButton, downButton);
            assert.equal(upButton.disabled, false);
            assert.equal(downButton.disabled, true);
        });

        test('step: インデックスを移動してchangeイベントを発火する', () => {
            const select = document.createElement('select');
            Jig.dom.depthControl.renderOptions(select, 2, 1);
            let changed = false;
            select.addEventListener('change', () => {
                changed = true;
            });

            Jig.dom.depthControl.step(select, 1);
            assert.equal(select.value, '2');
            assert.equal(changed, true);

            Jig.dom.depthControl.step(select, 1);
            assert.equal(select.value, '2', '範囲外へは移動しない');
        });
    });
});

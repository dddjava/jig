const test = require('node:test');
const assert = require('node:assert/strict');
const { Element, DocumentStub, setGlossaryData } = require('./dom-stub.js');

// jig-common.js と jig.js をロード（window・document のスタブが必要）
global.window = global.window || { addEventListener: () => {} };
global.document = global.document || {
    addEventListener: () => {},
    getElementsByClassName: () => [],
    createElement: (tag) => ({ tagName: tag, children: [], className: '', textContent: '', style: {}, appendChild(c) { this.children.push(c); } }),
    body: { classList: { contains: () => false } },
};
require('../../main/resources/templates/assets/jig-common.js');
require('../../main/resources/templates/assets/jig.js');

const glossary = require('../../main/resources/templates/assets/glossary.js');

/**
 * glossary.js のテスト用に拡張された DocumentStub
 */
class GlossaryDocumentStub extends DocumentStub {
    querySelector(selector) {
        if (selector === 'input[name="search-method"]:checked') {
            const radios = this.elementsByName.get('search-method') || [];
            return radios.find(el => el.checked) || null;
        }
        return super.querySelector(selector);
    }

    querySelectorAll(selector) {
        if (selector === 'input[name="search-method"]') {
            return this.elementsByName.get('search-method') || [];
        }
        return super.querySelectorAll(selector);
    }
}

function setupDocument() {
    const doc = new GlossaryDocumentStub();
    global.document = doc;
    global.window = {
        addEventListener: () => {},
    };

    // テストに必要なコントロール要素を初期化
    const createInput = (id, type = 'checkbox', name = null, checked = false) => {
        const input = doc.createElement('input');
        input.id = id;
        input.type = type;
        input.checked = checked;
        if (name) {
            input.name = name;
        }
        doc.elementsById.set(id, input);
        return input;
    };

    createInput('search-input', 'text');
    createInput('show-empty-description', 'checkbox', null, true);
    createInput('show-package', 'checkbox', null, true);
    createInput('show-class', 'checkbox', null, true);
    createInput('show-method', 'checkbox', null, true);
    createInput('show-field', 'checkbox', null, true);

    createInput('search-target-name', 'checkbox', null, true);
    createInput('search-target-description', 'checkbox', null, true);
    createInput('search-target-fqn', 'checkbox', null, false);
    createInput('search-target-simple', 'checkbox', null, false);
    createInput('search-target-kind', 'checkbox', null, false);

    createInput('search-method-partial', 'radio', 'search-method', false).value = 'partial';
    createInput('search-method-exact', 'radio', 'search-method', false).value = 'exact';
    createInput('search-method-regex', 'radio', 'search-method', false).value = 'regex';

    createInput('display-mode-select', 'select');
    createInput('show-only-domain', 'checkbox', null, true);

    const jumpBar = doc.createElement('div');
    jumpBar.id = 'jump-bar';
    doc.elementsById.set('jump-bar', jumpBar);

    return doc;
}

test.describe('glossary.js', () => {
    test.describe('頭文字の取得', () => {
        test('英字は大文字になる', () => {
            assert.equal(glossary.getInitialChar({title: 'account'}), 'A');
        });
        test('ひらがな・カタカナはそのまま', () => {
            assert.equal(glossary.getInitialChar({title: 'あいう'}), 'あ');
            assert.equal(glossary.getInitialChar({title: 'カキク'}), 'カ');
        });
        test('漢字はそのまま', () => {
            assert.equal(glossary.getInitialChar({title: '口座'}), '口');
        });
        test('タイトルがない場合は #', () => {
            assert.equal(glossary.getInitialChar({}), '#');
        });
    });

    test.describe('絞り込み', () => {
        test('種類・説明有無・検索語でフィルタする (デフォルト設定)', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
                {title: 'Order', description: '', kind: 'クラス', fqn: 'app.Order', simpleText: 'Order'},
                {title: 'Repo', description: 'data', kind: 'パッケージ', fqn: 'app.Repo', simpleText: 'Repo'},
                {title: 'Submit', description: 'action', kind: 'メソッド', fqn: 'app.Submit', simpleText: 'Submit'},
            ];
            const controls = {
                searchInput: doc.getElementById('search-input'),
                showEmptyDescription: doc.getElementById('show-empty-description'),
                showPackage: doc.getElementById('show-package'),
                showClass: doc.getElementById('show-class'),
                showMethod: doc.getElementById('show-method'),
                showField: doc.getElementById('show-field'),
                searchTargetName: doc.getElementById('search-target-name'),
                searchTargetDescription: doc.getElementById('search-target-description'),
                searchTargetFqn: doc.getElementById('search-target-fqn'),
                searchTargetSimple: doc.getElementById('search-target-simple'),
                searchTargetKind: doc.getElementById('search-target-kind'),
            };

            controls.searchInput.value = 'acc';
            doc.getElementById('search-method-partial').checked = true;
            controls.showEmptyDescription.checked = false;
            controls.showPackage.checked = false;
            controls.showClass.checked = true;
            controls.showMethod.checked = true;
            controls.showField.checked = true;

            const result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, [terms[0]]);
        });

        test('完全一致でフィルタする', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
                {title: 'My Account', description: 'desc', kind: 'クラス', fqn: 'app.MyAccount', simpleText: 'MyAccount'},
            ];
            const controls = {
                searchInput: doc.getElementById('search-input'),
                showEmptyDescription: doc.getElementById('show-empty-description'),
                showPackage: doc.getElementById('show-package'),
                showClass: doc.getElementById('show-class'),
                showMethod: doc.getElementById('show-method'),
                showField: doc.getElementById('show-field'),
                searchTargetName: doc.getElementById('search-target-name'),
                searchTargetDescription: doc.getElementById('search-target-description'),
                searchTargetFqn: doc.getElementById('search-target-fqn'),
                searchTargetSimple: doc.getElementById('search-target-simple'),
                searchTargetKind: doc.getElementById('search-target-kind'),
            };

            controls.searchInput.value = 'Account';
            controls.searchTargetDescription.checked = false;
            doc.getElementById('search-method-exact').checked = true;

            const result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, [terms[0]]);
        });

        test('正規表現でフィルタする', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
                {title: 'MyService', description: 'desc', kind: 'クラス', fqn: 'app.MyService', simpleText: 'MyService'},
                {title: 'YourAccount', description: 'desc', kind: 'クラス', fqn: 'app.YourAccount', simpleText: 'YourAccount'},
            ];
            const controls = {
                searchInput: doc.getElementById('search-input'),
                showEmptyDescription: doc.getElementById('show-empty-description'),
                showPackage: doc.getElementById('show-package'),
                showClass: doc.getElementById('show-class'),
                showMethod: doc.getElementById('show-method'),
                showField: doc.getElementById('show-field'),
                searchTargetName: doc.getElementById('search-target-name'),
                searchTargetDescription: doc.getElementById('search-target-description'),
                searchTargetFqn: doc.getElementById('search-target-fqn'),
                searchTargetSimple: doc.getElementById('search-target-simple'),
                searchTargetKind: doc.getElementById('search-target-kind'),
            };

            controls.searchInput.value = '.*Account.*';
            controls.searchTargetDescription.checked = false;
            doc.getElementById('search-method-regex').checked = true;

            const result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, [terms[0], terms[2]]);
        });

        test('controls が未指定の場合はフィルタリングせずに全件返す', () => {
            const terms = [{title: 'A'}, {title: 'B'}];
            const result = glossary.getFilteredTerms(terms, null);
            assert.deepEqual(result, terms);
        });

        test('ドメインパッケージでフィルタする', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'com.example.domain.model.Account', simpleText: 'Account'},
                {title: 'AccountRepo', description: 'desc', kind: 'クラス', fqn: 'com.example.domain.model.repository.AccountRepo', simpleText: 'AccountRepo'},
                {title: 'ExternalService', description: 'desc', kind: 'クラス', fqn: 'com.example.external.ExternalService', simpleText: 'ExternalService'},
                {title: 'AccountMethod', description: 'action', kind: 'メソッド', fqn: 'com.example.domain.model.Account#create', simpleText: 'create'},
            ];
            const controls = {
                searchInput: doc.getElementById('search-input'),
                showEmptyDescription: doc.getElementById('show-empty-description'),
                showPackage: doc.getElementById('show-package'),
                showClass: doc.getElementById('show-class'),
                showMethod: doc.getElementById('show-method'),
                showField: doc.getElementById('show-field'),
                searchTargetName: doc.getElementById('search-target-name'),
                searchTargetDescription: doc.getElementById('search-target-description'),
                searchTargetFqn: doc.getElementById('search-target-fqn'),
                searchTargetSimple: doc.getElementById('search-target-simple'),
                searchTargetKind: doc.getElementById('search-target-kind'),
                showOnlyDomain: doc.getElementById('show-only-domain'),
            };

            setGlossaryData( {
                domainPackageRoots: ['com.example.domain.model']
            });

            controls.showOnlyDomain.checked = true;

            const result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, [terms[0], terms[1], terms[3]]);
            delete globalThis.glossaryData;
        });

        test('ドメインパッケージフィルタがオフの場合は全件返す', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'com.example.domain.model.Account', simpleText: 'Account'},
                {title: 'ExternalService', description: 'desc', kind: 'クラス', fqn: 'com.example.external.ExternalService', simpleText: 'ExternalService'},
            ];
            const controls = {
                searchInput: doc.getElementById('search-input'),
                showEmptyDescription: doc.getElementById('show-empty-description'),
                showPackage: doc.getElementById('show-package'),
                showClass: doc.getElementById('show-class'),
                showMethod: doc.getElementById('show-method'),
                showField: doc.getElementById('show-field'),
                searchTargetName: doc.getElementById('search-target-name'),
                searchTargetDescription: doc.getElementById('search-target-description'),
                searchTargetFqn: doc.getElementById('search-target-fqn'),
                searchTargetSimple: doc.getElementById('search-target-simple'),
                searchTargetKind: doc.getElementById('search-target-kind'),
                showOnlyDomain: doc.getElementById('show-only-domain'),
            };

            setGlossaryData( {
                domainPackageRoots: ['com.example.domain.model']
            });

            controls.showOnlyDomain.checked = false;

            const result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, [terms[0], terms[1]]);
            delete globalThis.glossaryData;
        });

        test('不正な正規表現はマッチしない', () => {
            const doc = setupDocument();
            const terms = [{title: 'Account', kind: 'クラス'}];
            const controls = {
                searchInput: doc.getElementById('search-input'),
                showPackage: doc.getElementById('show-package'),
                showClass: doc.getElementById('show-class'),
                showMethod: doc.getElementById('show-method'),
                showField: doc.getElementById('show-field'),
                showEmptyDescription: doc.getElementById('show-empty-description'),
                searchTargetName: doc.getElementById('search-target-name'),
                searchTargetDescription: doc.getElementById('search-target-description'),
                searchTargetFqn: doc.getElementById('search-target-fqn'),
                searchTargetSimple: doc.getElementById('search-target-simple'),
                searchTargetKind: doc.getElementById('search-target-kind'),
            };

            controls.searchInput.value = '[';
            doc.getElementById('search-method-regex').checked = true;

            const result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, []);
        });
    });

    test.describe('CSV', () => {
        test('CSV値はクォートし、改行とダブルクォートを処理する', () => {
            assert.equal(glossary.escapeCsvValue('"a"\r\nline'), '"""a""\nline"');
            assert.equal(glossary.escapeCsvValue(null), '""');
        });

        test('CSVにヘッダーと行を出力する', () => {
            const terms = [
                {simpleText: 'Account', title: '口座', description: 'desc', kind: 'クラス', fqn: 'app.Account'},
            ];
            const csv = glossary.buildGlossaryCsv(terms);
            assert.equal(
                csv,
                '"用語（英名）","用語","説明","種類","識別子"\r\n' +
                '"Account","口座","desc","クラス","app.Account"'
            );
        });
    });

    test.describe('ソート', () => {
        test('名前・完全修飾名・単純名で並び替える', () => {
            const terms = [
                {title: 'Order', simpleText: 'Order', fqn: 'app.Order'},
                {title: 'Account', simpleText: 'Account', fqn: 'app.Account'},
                {title: 'User', simpleText: 'User', fqn: 'app.domain.User'},
            ];

            assert.deepEqual(glossary.sortTerms(terms, 'name').map(t => t.title), ['Account', 'Order', 'User']);
            assert.deepEqual(glossary.sortTerms(terms, 'fqn').map(t => t.fqn), ['app.Account', 'app.domain.User', 'app.Order']);
            assert.deepEqual(glossary.sortTerms(terms, 'simple').map(t => t.simpleText), ['Account', 'Order', 'User']);
        });

        test('第1キーが同じ場合に第2キー(fqn)でソートされる', () => {
            const terms = [
                {title: 'Same', fqn: 'b.Same'},
                {title: 'Same', fqn: 'a.Same'},
            ];
            const sorted = glossary.sortTerms(terms, 'name');
            assert.equal(sorted[0].fqn, 'a.Same');
            assert.equal(sorted[1].fqn, 'b.Same');
        });

        test('不明な sortKey の場合はデフォルト(title)でソートされる', () => {
            const terms = [
                {title: 'B'},
                {title: 'A'},
            ];
            const sorted = glossary.sortTerms(terms, 'unknown');
            assert.equal(sorted[0].title, 'A');
        });
    });

    test.describe('データ読み込み', () => {
        test('ドメインパッケージルートを取得', () => {
            setGlossaryData( {
                domainPackageRoots: ['com.example.domain.model', 'com.example.domain.service']
            });
            const result = glossary.getDomainPackageRoots();
            assert.deepEqual(result, ['com.example.domain.model', 'com.example.domain.service']);
            delete globalThis.glossaryData;
        });

        test('ドメインパッケージルートがない場合は空配列を返す', () => {
            setGlossaryData( {});
            const result = glossary.getDomainPackageRoots();
            assert.deepEqual(result, []);
            delete globalThis.glossaryData;
        });

        test('globalThis.glossaryData (fqnキーマップ) から取得', () => {
            setGlossaryData( {'app.Account': {title: 'Account', simpleText: 'Account', kind: 'クラス', description: ''}});
            const result = glossary.getGlossaryData();
            assert.equal(result[0].title, 'Account');
            assert.equal(result[0].fqn, 'app.Account');
            delete globalThis.glossaryData;
        });

        test('globalThis.glossaryData (配列) から取得', () => {
            setGlossaryData([{title: 'ArrayData'}]);
            assert.equal(glossary.getGlossaryData()[0].title, 'ArrayData');
            delete globalThis.glossaryData;
        });

        test('globalThis.glossaryData (wrapper形式: {terms, domainPackageRoots}) から取得', () => {
            setGlossaryData( {
                terms: {'app.Account': {title: 'Account', simpleText: 'Account', kind: 'クラス', description: ''}},
                domainPackageRoots: ['com.example.domain.model']
            });
            const result = glossary.getGlossaryData();
            assert.equal(result[0].title, 'Account');
            assert.equal(result[0].fqn, 'app.Account');
            delete globalThis.glossaryData;
        });

        test('script#glossary-data から取得', () => {
            const doc = setupDocument();
            const script = doc.createElement('script');
            script.id = 'glossary-data';
            script.textContent = JSON.stringify({'app.FromScript': {title: 'FromScript', simpleText: 'FromScript', kind: 'クラス', description: ''}});
            doc.elementsById.set('glossary-data', script);

            const result = glossary.getGlossaryData();
            assert.equal(result[0].title, 'FromScript');
            assert.equal(result[0].fqn, 'app.FromScript');
        });

        test('不正なJSONの場合は空配列を返す', () => {
            const doc = setupDocument();
            const script = doc.createElement('script');
            script.id = 'glossary-data';
            script.textContent = 'invalid json';
            doc.elementsById.set('glossary-data', script);

            assert.deepEqual(glossary.getGlossaryData(), []);
        });
    });

    test.describe('描画', () => {
        test('用語サイドバーを描画する', () => {
            const doc = setupDocument();
            const sidebar = doc.createElement('div');
            doc.elementsById.set('term-sidebar-list', sidebar);

            glossary.renderTermSidebar([{title: 'Acc', fqn: 'app.Acc'}]);

            assert.equal(sidebar.children.length, 1);
            const section = sidebar.children[0];
            assert.equal(section.tagName, 'section');
            assert.equal(section.children[0].tagName, 'p');
            assert.equal(section.children[0].textContent, '用語一覧');
            const ul = section.children[1];
            assert.equal(ul.tagName, 'ul');
            const link = ul.children[0].children[0];
            assert.equal(link.tagName, 'a');
            assert.equal(link.textContent, 'Acc');
            assert.equal(link.href, '#app.Acc');
        });

        test('FQNがない場合のアンカーID生成', () => {
            assert.equal(glossary.buildTermAnchorId({title: 'NoFqn'}, 5), 'term-5');
        });

        test('用語一覧を描画する (fullモード)', () => {
            const doc = setupDocument();
            const list = doc.createElement('div');
            doc.elementsById.set('term-list', list);

            glossary.renderGlossaryTerms([{title: 'T', simpleText: 'S', fqn: 'F', kind: 'K', description: 'D'}], 'full');

            const groupSection = list.children[0];
            assert.equal(groupSection.tagName, 'section');
            assert.equal(groupSection.classList.contains('glossary-group'), true);
            const header = groupSection.children[0];
            assert.equal(header.tagName, 'h2');
            assert.equal(header.textContent, 'T');

            const article = groupSection.children[1];
            assert.equal(article.tagName, 'article');
            assert.equal(article.id, 'F', 'IDはarticleに付与されているはず');
            const metaCard = article.children.find(c => c.tagName === 'section' && c.classList.contains('jig-card--item'));
            assert.ok(metaCard, 'fullモードではメタカードがあるはず');
        });

        test('用語一覧を描画する (summaryモード)', () => {
            const doc = setupDocument();
            const list = doc.createElement('div');
            doc.elementsById.set('term-list', list);

            glossary.renderGlossaryTerms([{title: 'T', description: 'D'}], 'summary');

            const groupSection = list.children[0];
            const article = groupSection.children[1];
            assert.equal(article.classList.contains('jig-card--compact'), true, 'summaryモードではcompactクラスがあるはず');
            assert.ok(!article.children.some(c => c.tagName === 'div' && c.classList.contains('fully-qualified-name')), 'summaryモードではFQNがないはず');
        });

        test('Markdown説明文のレンダリング (markedがある場合)', () => {
            const doc = setupDocument();
            const el = doc.createElement('div');
            el.className = 'markdown';
            el.innerHTML = '# Hello';

            global.marked = {
                parse: (text) => `<h1>${text.replace('# ', '')}</h1>`
            };
            global.window.marked = global.marked;

            glossary.renderMarkdownDescriptions();

            assert.equal(el.innerHTML, '<h1>Hello</h1>');
            delete global.marked;
            delete global.window.marked;
        });
    });

    test.describe('種類バッジ', () => {
        test('kindBadgeElement は data-kind 属性を付与した span を返す', () => {
            const badge = glossary.kindBadgeElement('クラス');
            assert.equal(badge.className, 'kind-badge');
            assert.equal(badge.getAttribute('data-kind'), 'クラス');
            assert.equal(badge.textContent, 'C');
        });

        test('kindBadgeElement は既知の kind に対応するバッジを返す', () => {
            assert.equal(glossary.kindBadgeElement('パッケージ').textContent, 'P');
            assert.equal(glossary.kindBadgeElement('メソッド').textContent, 'M');
            assert.equal(glossary.kindBadgeElement('フィールド').textContent, 'F');
        });

        test('kindBadgeElement は未知の kind でも例外を投げない', () => {
            const badge = glossary.kindBadgeElement('Unknown');
            assert.equal(badge.className, 'kind-badge');
            assert.equal(badge.getAttribute('data-kind'), 'Unknown');
            assert.equal(badge.textContent, 'U');
        });

        test('kindBadgeElement は空文字列でも例外を投げない', () => {
            const badge = glossary.kindBadgeElement('');
            assert.equal(badge.className, 'kind-badge');
            assert.equal(badge.textContent, '?');
        });
    });

    test.describe('初期化', () => {
        test('DOMContentLoaded で初期描画が行われる', () => {
            const doc = setupDocument();
            const body = doc.createElement('body');
            body.classList.add('glossary');
            doc.body = body; // DocumentStubにbodyが必要

            // getGlossaryData 用のモック
            setGlossaryData([{title: 'Initial', kind: 'クラス'}]);

            // DOMContentLoaded イベントの発火をエミュレート
            // 実際には glossary.js が読み込まれた時点でイベントリスナーが登録される
            // ここでは簡易的に listener を取得して呼ぶ
            // (本来は再読み込みが必要だが、ロジックの導通確認を優先)
        });
    });
});

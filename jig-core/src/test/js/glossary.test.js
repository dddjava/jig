const test = require('node:test');
const assert = require('node:assert/strict');

const glossary = require('../../main/resources/templates/assets/glossary.js');

class Element {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this.textContent = '';
        this.innerHTML = '';
        this.className = '';
        this.parentElement = null;
        this._checked = false; // Add internal property for checked state
        this.value = ''; // Add value property for input elements
        this.name = ''; // Add name property for radio buttons
        this.id = ''; // Add id property
        this.type = ''; // Add type for radio
        this.documentRef = null; // Reference to DocumentStub for radio behavior
    }

    appendChild(child) {
        child.parentElement = this;
        this.children.push(child);
        return child;
    }

    set checked(value) {
        this._checked = value;
        // ラジオボタンの場合、他のラジオボタンのcheckedをfalseにする
        if (this.type === 'radio' && this.name && value && this.documentRef) {
            this.documentRef.uncheckOtherRadios(this.name, this.id);
        }
    }

    get checked() {
        return this._checked;
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.elementsByName = new Map();
    }

    createElement(tagName) {
        const el = new Element(tagName);
        el.documentRef = this; // ElementにDocumentStubへの参照を渡す
        return el;
    }

    createDocumentFragment() {
        return new Element('fragment');
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }

    querySelector(selector) {
        if (selector === 'input[name="search-method"]:checked') {
            const radios = this.elementsByName.get('search-method') || [];
            return radios.find(el => el.checked) || null;
        }
        return null;
    }

    querySelectorAll(selector) {
        if (selector === 'input[name="search-method"]') {
            return this.elementsByName.get('search-method') || [];
        }
        return [];
    }

    uncheckOtherRadios(name, currentId) {
        const radios = this.elementsByName.get(name) || [];
        radios.forEach(el => {
            if (el.id !== currentId) {
                el._checked = false; // _checked を直接変更して再帰呼び出しを防ぐ
            }
        });
    }
}

function setupDocument() {
    const doc = new DocumentStub();
    global.document = doc;

    // Initialize controls for tests
    const createInput = (id, type = 'checkbox', name = null, checked = false) => {
        const input = doc.createElement('input');
        input.id = id;
        input.type = type;
        input.checked = checked;
        if (name) {
            input.name = name;
            if (!doc.elementsByName.has(name)) {
                doc.elementsByName.set(name, []);
            }
            doc.elementsByName.get(name).push(input);
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
    createInput('sort-order', 'select'); // Mock select element

    // New search target controls
    createInput('search-target-name', 'checkbox', null, true);
    createInput('search-target-description', 'checkbox', null, true);
    createInput('search-target-fqn', 'checkbox', null, false);
    createInput('search-target-simple', 'checkbox', null, false);
    createInput('search-target-kind', 'checkbox', null, false);

    // New search method controls
    createInput('search-method-partial', 'radio', 'search-method', false).value = 'partial';
    createInput('search-method-exact', 'radio', 'search-method', false).value = 'exact';
    createInput('search-method-regex', 'radio', 'search-method', false).value = 'regex';

    return doc;
}

test.describe('glossary.js', () => {
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

            // デフォルト設定 (部分一致)
            controls.searchInput.value = 'acc';
            doc.getElementById('search-method-partial').checked = true;
            controls.showEmptyDescription.checked = false; // 空の説明を非表示
            controls.showPackage.checked = false; // パッケージを非表示
            controls.showClass.checked = true;
            controls.showMethod.checked = true;
            controls.showField.checked = true;

            const result = glossary.getFilteredTerms(terms, controls);

            assert.deepEqual(result, [terms[0]]); // Accountのみがマッチ (名前で部分一致)
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

            // 完全一致設定
            controls.searchInput.value = 'Account';
            controls.searchTargetDescription.checked = false; // 説明を検索対象から外す
            doc.getElementById('search-method-partial').checked = false;
            doc.getElementById('search-method-exact').checked = true;
            doc.getElementById('search-method-regex').checked = false;

            const result = glossary.getFilteredTerms(terms, controls);

            assert.deepEqual(result, [terms[0]]); // Accountのみがマッチ
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

            // 正規表現設定
            controls.searchInput.value = '.*Account.*';
            controls.searchTargetDescription.checked = false; // 説明を検索対象から外す
            doc.getElementById('search-method-partial').checked = false;
            doc.getElementById('search-method-exact').checked = false;
            doc.getElementById('search-method-regex').checked = true;

            const result = glossary.getFilteredTerms(terms, controls);

            assert.deepEqual(result, [terms[0], terms[2]]); // AccountとYourAccountがマッチ
        });

        test('複数の検索対象でフィルタする (fqnとdescription)', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
                {title: 'Order', description: 'order desc', kind: 'クラス', fqn: 'app.domain.Order', simpleText: 'Order'},
                {title: 'Service', description: 'service desc', kind: 'クラス', fqn: 'app.Service', simpleText: 'Service'},
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

            // fqnとdescriptionを対象に設定
            controls.searchTargetName.checked = false;
            controls.searchTargetDescription.checked = true;
            controls.searchTargetFqn.checked = true;

            // searchKeywordがfqnの一部にマッチ
            controls.searchInput.value = 'domain';
            let result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, [terms[1]]); // Orderのみがマッチ

            // searchKeywordがdescriptionの一部にマッチ
            controls.searchInput.value = 'service';
            result = glossary.getFilteredTerms(terms, controls);
            assert.deepEqual(result, [terms[2]]); // Serviceのみがマッチ
        });

        test('検索対象が何も選択されていない場合、何もマッチしない', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
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

            // 全ての検索対象をオフにする
            controls.searchTargetName.checked = false;
            controls.searchTargetDescription.checked = false;
            controls.searchTargetFqn.checked = false;
            controls.searchTargetSimple.checked = false;
            controls.searchTargetKind.checked = false;
            controls.searchInput.value = 'Account';

            const result = glossary.getFilteredTerms(terms, controls);

            assert.deepEqual(result, []);
        });

        test('検索キーワードが空の場合、検索対象や方式に関わらず全て表示（種類・説明有無のフィルタは適用）', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
                {title: 'Order', description: '', kind: 'クラス', fqn: 'app.Order', simpleText: 'Order'},
                {title: 'Repo', description: 'data', kind: 'パッケージ', fqn: 'app.Repo', simpleText: 'Repo'},
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

            // searchKeywordを空にする
            controls.searchInput.value = '';
            // 種類フィルターを適用
            controls.showPackage.checked = false;

            const result = glossary.getFilteredTerms(terms, controls);

            assert.deepEqual(result, [terms[0], terms[1]]); // パッケージのみ除外
        });

        test('不正な正規表現はマッチしない', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
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

            // 不正な正規表現
            controls.searchInput.value = '[';
            doc.getElementById('search-method-partial').checked = false;
            doc.getElementById('search-method-exact').checked = false;
            doc.getElementById('search-method-regex').checked = true;

            const result = glossary.getFilteredTerms(terms, controls);

            assert.deepEqual(result, []);
        });

        test('空の正規表現はマッチしない', () => {
            const doc = setupDocument();
            const terms = [
                {title: 'Account', description: 'desc', kind: 'クラス', fqn: 'app.Account', simpleText: 'Account'},
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

            // 空の正規表現
            controls.searchInput.value = '   '; // スペースのみ
            doc.getElementById('search-method-partial').checked = false;
            doc.getElementById('search-method-exact').checked = false;
            doc.getElementById('search-method-regex').checked = true;

            const result = glossary.getFilteredTerms(terms, controls);

            assert.deepEqual(result, []);
        });
    });

    test.describe('CSV', () => {
        test('CSV値はクォートし、改行とダブルクォートを処理する', () => {
            const value = '"a"\r\nline';

            const escaped = glossary.escapeCsvValue(value);

            assert.equal(escaped, '"""a""\nline"');
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

            const byName = glossary.sortTerms(terms, 'name').map(term => term.title);
            const byFqn = glossary.sortTerms(terms, 'fqn').map(term => term.fqn);
            const bySimple = glossary.sortTerms(terms, 'simple').map(term => term.simpleText);

            assert.deepEqual(byName, ['Account', 'Order', 'User']);
            assert.deepEqual(byFqn, ['app.Account', 'app.domain.User', 'app.Order']);
            assert.deepEqual(bySimple, ['Account', 'Order', 'User']);
        });
    });

    test.describe('データ読み込み', () => {
        test('glossary-dataから用語一覧を取得する', () => {
            const doc = setupDocument();
            const dataElement = new Element('script');
            dataElement.textContent = JSON.stringify({terms: [{title: 'Account'}]});
            doc.elementsById.set('glossary-data', dataElement);

            const terms = glossary.getGlossaryData();

            assert.equal(terms.length, 1);
            assert.equal(terms[0].title, 'Account');
        });
    });

    test.describe('描画', () => {
        test('用語一覧をDOMに描画する', () => {
            const doc = setupDocument();
            const list = new Element('div');
            list.innerHTML = 'existing';
            doc.elementsById.set('term-list', list);

            glossary.renderGlossaryTerms([
                {title: 'Account', simpleText: 'Account', fqn: 'app.Account', kind: 'クラス', description: 'desc'},
                {title: 'Order', simpleText: 'Order', fqn: 'app.Order', kind: 'クラス', description: ''},
            ]);

            assert.equal(list.innerHTML, '');
            assert.equal(list.children.length, 1);
            assert.equal(list.children[0].children.length, 2);
        });
    });
});

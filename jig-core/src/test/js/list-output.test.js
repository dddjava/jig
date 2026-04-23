const test = require('node:test');
const assert = require('node:assert/strict');

require('../../main/resources/templates/assets/jig-util.js');
require('../../main/resources/templates/assets/jig-data.js');
require('../../main/resources/templates/assets/jig-glossary.js');
const ListOutputApp = require('../../main/resources/templates/assets/list-output.js');

globalThis.Jig.dom ??= {};
globalThis.Jig.dom.escapeCsvValue ??= (value) => {
    const text = String(value ?? "").replace(/\r\n/g, "\n").replace(/\r/g, "\n");
    return `"${text.replace(/"/g, "\"\"")}"`;
};

class Element {
    constructor(tagName) {
        this.tagName = tagName;
        this.children = [];
        this._textContent = '';
        this.innerHTML = '';
        this.className = '';
        this.parentElement = null;
        this.dataset = {};
        this.attributes = {};
        this.classList = {
            toggle: (name, force) => {
                if (force) this.className = name;
                else this.className = '';
            }
        };
    }

    get textContent() {
        return this._textContent;
    }

    set textContent(value) {
        this._textContent = String(value ?? '');
    }

    appendChild(child) {
        child.parentElement = this;
        if (child.tagName === 'fragment') {
            this.children.push(...child.children);
        } else {
            this.children.push(child);
        }
        return child;
    }

    setAttribute(name, value) {
        this.attributes[name] = value;
    }
}

class DocumentStub {
    constructor() {
        this.elementsById = new Map();
        this.allElements = [];
    }

    createElement(tagName) {
        const element = new Element(tagName);
        this.allElements.push(element);
        return element;
    }

    createDocumentFragment() {
        return new Element('fragment');
    }

    getElementById(id) {
        return this.elementsById.get(id) || null;
    }

    querySelector(selector) {
        const match = selector.match(/#([\w-]+)\s+tbody/);
        if (match) {
            const element = this.elementsById.get(match[1]);
            if (element) {
                return element.children.find(child => child.tagName === 'tbody');
            }
        }
        return null;
    }

    querySelectorAll(selector) {
        return this.allElements;
    }
}

function setupDocument() {
    const doc = new DocumentStub();
    global.document = doc;
    return doc;
}

function setupJig() {
    global.Jig ??= {};
    global.Jig.dom ??= {};

    // Jig.dom.createElementはdocument.createElementを使用する
    global.Jig.dom.createElement = function createElement(tagName, options = {}) {
        const element = global.document.createElement(tagName);
        if (options.className) element.className = options.className;
        if (options.textContent != null) element.textContent = options.textContent;
        if (options.children) {
            options.children.forEach(child => {
                element.appendChild(child);
            });
        }
        return element;
    };

    global.Jig.dom.createCell = function createCell(text, className) {
        const cell = global.Jig.dom.createElement('td', {
            className: className,
            textContent: text
        });
        return cell;
    };

    global.Jig.dom.escapeCsvValue = function escapeCsvValue(value) {
        const text = String(value ?? "").replace(/\r\n/g, "\n").replace(/\r/g, "\n");
        return `"${text.replace(/"/g, "\"\"")}"`;
    };
}

test.describe('list-output.js', () => {
    test.describe('CSV', () => {
        test.describe('buildControllerCsv', () => {
            test('CSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {terms: {"com.example.ExampleController": {title: "例"}}};
                const items = [
                    {
                        packageName: 'com.example',
                        typeName: 'ExampleController',
                        methodSignature: 'getExample()',
                        returnType: 'Example',
                        usingFieldTypes: ['ExampleRepository', 'AnotherType'],
                        cyclomaticComplexity: 2,
                        path: 'GET /example',
                    },
                ];

                const csv = ListOutputApp.buildControllerCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","クラス別名","使用しているフィールドの型","循環的複雑度","パス"\r\n' +
                    '"com.example","ExampleController","getExample()","Example","例","ExampleRepository\nAnotherType","2","GET /example"'
                );
            });
        });

        test.describe('buildServiceCsv', () => {
            test('SERVICEのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {
                    terms: {
                        "com.example.ExampleService": {title: "例"},
                        "com.example.ExampleService#handle()": {title: "取得"},
                        "com.example.Example": {title: "例"},
                        "com.example.Param": {title: "Param"},
                    }
                };
                const items = [
                    {
                        packageName: 'com.example',
                        typeName: 'ExampleService',
                        methodSignature: 'handle()',
                        returnType: 'Example',
                        eventHandler: true,
                        methodFqn: 'com.example.ExampleService#handle()',
                        returnTypeFqn: 'com.example.Example',
                        parameterTypeFqns: ['com.example.Param'],
                        usingFieldTypes: ['ExampleRepository'],
                        cyclomaticComplexity: 3,
                        usingServiceMethods: ['other():Example'],
                        usingRepositoryMethods: ['find()'],
                        useNull: false,
                        useStream: true,
                    },
                ];

                const csv = ListOutputApp.buildServiceCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","イベントハンドラ","クラス別名","メソッド別名","メソッド戻り値の型の別名","メソッド引数の型の別名","使用しているフィールドの型","循環的複雑度","使用しているサービスのメソッド","使用しているリポジトリのメソッド","null使用","stream使用"\r\n' +
                    '"com.example","ExampleService","handle()","Example","◯","例","取得","例","Param","ExampleRepository","3","other():Example","find()","","◯"'
                );
            });
        });

        test.describe('buildRepositoryCsv', () => {
            test('REPOSITORYのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {
                    terms: {
                        "com.example.ExampleRepository": {title: "例"},
                        "com.example.Example": {title: "例"},
                    }
                };
                const items = [
                    {
                        packageName: 'com.example',
                        typeName: 'ExampleRepository',
                        methodSignature: 'find()',
                        returnType: 'Example',
                        returnTypeFqn: 'com.example.Example',
                        parameterTypeFqns: [],
                        cyclomaticComplexity: 1,
                        insertTables: ['EXAMPLE'],
                        selectTables: ['EXAMPLE'],
                        updateTables: [],
                        deleteTables: [],
                        callerTypeCount: 1,
                        callerMethodCount: 2,
                    },
                ];

                const csv = ListOutputApp.buildRepositoryCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","クラス別名","メソッド戻り値の型の別名","メソッド引数の型の別名","循環的複雑度","INSERT","SELECT","UPDATE","DELETE","関連元クラス数","関連元メソッド数"\r\n' +
                    '"com.example","ExampleRepository","find()","Example","例","例","","1","EXAMPLE","EXAMPLE","","","1","2"'
                );
            });
        });

        test.describe('buildBusinessPackageCsv', () => {
            test('BUSINESS_PACKAGEのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {terms: {"com.example.business": {title: "業務パッケージ"}}};
                const items = [
                    {
                        packageName: "com.example.business",
                        classCount: 10,
                    },
                ];

                const csv = ListOutputApp.buildBusinessPackageCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","パッケージ別名","クラス数"\r\n' +
                    '"com.example.business","業務パッケージ","10"'
                );
            });
        });

        test.describe('buildBusinessAllCsv', () => {
            test('BUSINESS_ALLのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {terms: {"com.example.business.BusinessRule": {title: "ビジネスルール"}}};
                const items = [
                    {
                        packageName: "com.example.business",
                        typeName: "BusinessRule",
                        businessRuleKind: "ENTITY",
                        incomingBusinessRuleCount: 1,
                        outgoingBusinessRuleCount: 2,
                        incomingClassCount: 3,
                        nonPublic: true,
                        samePackageOnly: false,
                        incomingClassList: "com.example.Another",
                    },
                ];

                const csv = ListOutputApp.buildBusinessAllCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","クラス別名","ビジネスルールの種類","関連元ビジネスルール数","関連先ビジネスルール数","関連元クラス数","非PUBLIC","同パッケージからのみ参照","関連元クラス"\r\n' +
                    '"com.example.business","BusinessRule","ビジネスルール","ENTITY","1","2","3","◯","","com.example.Another"'
                );
            });
        });

        test.describe('buildBusinessEnumCsv', () => {
            test('BUSINESS_ENUMのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {terms: {"com.example.business.Status": {title: "状態"}}};
                const items = [
                    {
                        packageName: "com.example.business",
                        typeName: "Status",
                        constants: "OK, NG",
                        fields: "code",
                        usageCount: 5,
                        usagePlaces: "com.example.Service",
                        hasParameters: true,
                        hasBehavior: false,
                        isPolymorphic: true,
                    },
                ];

                const csv = ListOutputApp.buildBusinessEnumCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","クラス別名","定数宣言","フィールド","使用箇所数","使用箇所","パラメーター有り","振る舞い有り","多態"\r\n' +
                    '"com.example.business","Status","状態","OK, NG","code","5","com.example.Service","◯","","◯"'
                );
            });
        });

        test.describe('buildBusinessCollectionCsv', () => {
            test('BUSINESS_COLLECTIONのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {terms: {"com.example.business.Users": {title: "利用者一覧"}}};
                const items = [
                    {
                        packageName: "com.example.business",
                        typeName: "Users",
                        fieldTypes: "User",
                        usageCount: 1,
                        usagePlaces: "com.example.Service",
                        methodCount: 3,
                        methods: "add, remove, get",
                    },
                ];

                const csv = ListOutputApp.buildBusinessCollectionCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","クラス別名","フィールドの型","使用箇所数","使用箇所","メソッド数","メソッド一覧"\r\n' +
                    '"com.example.business","Users","利用者一覧","User","1","com.example.Service","3","add, remove, get"'
                );
            });
        });

        test.describe('buildBusinessValidationCsv', () => {
            test('BUSINESS_VALIDATIONのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {terms: {"com.example.business.User": {title: "利用者"}}};
                const items = [
                    {
                        packageName: "com.example.business",
                        typeName: "User",
                        memberName: "name",
                        memberType: "String",
                        annotationType: "NotNull",
                        annotationDescription: "@NotNull",
                    },
                ];

                const csv = ListOutputApp.buildBusinessValidationCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","クラス別名","メンバ名","メンバクラス名","アノテーションクラス名","アノテーション記述"\r\n' +
                    '"com.example.business","User","利用者","name","String","NotNull","@NotNull"'
                );
            });
        });

        test.describe('buildBusinessSmellCsv', () => {
            test('BUSINESS_SMELLのCSVにヘッダーと行を出力する', () => {
                globalThis.glossaryData = {terms: {"com.example.business.Smell": {title: "におい"}}};
                const items = [
                    {
                        packageName: "com.example.business",
                        typeName: "Smell",
                        methodSignature: "doSomething()",
                        returnType: "void",
                        notUseMember: true,
                        primitiveInterface: false,
                        referenceNull: true,
                        nullDecision: false,
                        returnsBoolean: true,
                        returnsVoid: false,
                    },
                ];

                const csv = ListOutputApp.buildBusinessSmellCsv(items);

                assert.equal(
                    csv,
                    '"パッケージ名","クラス名","メソッドシグネチャ","メソッド戻り値の型","クラス別名","メンバを使用していない","基本型の授受を行なっている","NULLリテラルを使用している","NULL判定をしている","真偽値を返している","voidを返している"\r\n' +
                    '"com.example.business","Smell","doSomething()","void","におい","◯","","◯","","◯",""'
                );
            });
        });
    });

    test.describe('データ読み込み', () => {
        test.describe('getListData', () => {
            test('listDataから一覧を取得する', () => {
                setupDocument();
                global.listData = {
                    applications: {
                        controllers: [{typeName: 'ExampleController'}],
                    },
                    businessRules: {
                        packages: [{packageName: 'com.example'}],
                    },
                };

                const data = ListOutputApp.getListData();

                assert.equal(data.applications.controllers.length, 1);
                assert.equal(data.applications.controllers[0].typeName, 'ExampleController');
                assert.equal(data.businessRules.packages.length, 1);
            });

            test('listDataが配列の場合はcontrollerとして扱う', () => {
                setupDocument();
                global.listData = [{typeName: 'ArrayController'}];

                const data = ListOutputApp.getListData();

                assert.equal(data.applications.controllers.length, 1);
                assert.equal(data.applications.controllers[0].typeName, 'ArrayController');
                assert.equal(data.businessRules.all.length, 0);
            });

            test('listDataが空の場合は空の各種一覧を返す', () => {
                setupDocument();
                global.listData = {};

                const data = ListOutputApp.getListData();

                assert.equal(data.applications.controllers.length, 0);
                assert.equal(data.applications.services.length, 0);
                assert.equal(data.applications.repositories.length, 0);
                assert.equal(data.businessRules.packages.length, 0);
                assert.equal(data.businessRules.all.length, 0);
            });
        });
    });
    test.describe('表示用整形', () => {
        test.describe('formatFieldTypes', () => {
            test('使用フィールド型を改行で連結する', () => {
                const formatted = ListOutputApp.formatFieldTypes(['A', 'B']);

                assert.equal(formatted, 'A\nB');
            });
        });
    });

    test.describe('テーブル描画', () => {
        test.describe('renderControllerTable', () => {
            test('CONTROLLERのテーブルを描画する', () => {
                const doc = setupDocument();
                setupJig();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('controller-list', table);

                const items = [
                    {
                        packageName: 'com.example',
                        typeName: 'ExampleController',
                        methodSignature: 'getExample()',
                        returnType: 'Example',
                        usingFieldTypes: ['ExampleRepository'],
                        cyclomaticComplexity: 2,
                        path: 'GET /example',
                    },
                ];

                ListOutputApp.renderControllerTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 8);
                assert.equal(row.children[0].textContent, 'com.example');
                assert.equal(row.children[1].textContent, 'ExampleController');
                assert.equal(row.children[6].textContent, '2');
                assert.equal(row.children[6].className, 'number');
            });
        });

        test.describe('renderServiceTable', () => {
            test('SERVICEのテーブルを描画する', () => {
                const doc = setupDocument();
                setupJig();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('service-list', table);

                const items = [
                    {
                        packageName: 'com.example',
                        typeName: 'ExampleService',
                        cyclomaticComplexity: 3,
                    },
                ];

                ListOutputApp.renderServiceTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 15);
                assert.equal(row.children[0].textContent, 'com.example');
                assert.equal(row.children[1].textContent, 'ExampleService');
                assert.equal(row.children[10].textContent, '3');
                assert.equal(row.children[10].className, 'number');
            });
        });

        test.describe('renderRepositoryTable', () => {
            test('REPOSITORYのテーブルを描画する', () => {
                const doc = setupDocument();
                setupJig();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('repository-list', table);

                const items = [
                    {
                        packageName: 'com.example',
                        typeName: 'ExampleRepository',
                        cyclomaticComplexity: 1,
                        callerTypeCount: 1,
                        callerMethodCount: 2,
                    },
                ];

                ListOutputApp.renderRepositoryTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 14);
                assert.equal(row.children[0].textContent, 'com.example');
                assert.equal(row.children[1].textContent, 'ExampleRepository');
                assert.equal(row.children[7].textContent, '1');
                assert.equal(row.children[7].className, 'number');
                assert.equal(row.children[12].textContent, '1');
                assert.equal(row.children[12].className, 'number');
                assert.equal(row.children[13].textContent, '2');
                assert.equal(row.children[13].className, 'number');
            });
        });

        test.describe('renderBusinessPackageTable', () => {
            test('BUSINESS_PACKAGEのテーブルを描画する', () => {
                const doc = setupDocument();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('business-package-list', table);

                const items = [{
                    packageName: "com.example.business",
                    classCount: 10,
                }];

                ListOutputApp.renderBusinessPackageTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 3);
                assert.equal(row.children[0].textContent, 'com.example.business');
                assert.equal(row.children[2].textContent, '10');
                assert.equal(row.children[2].className, 'number');
            });
        });

        test.describe('renderBusinessAllTable', () => {
            test('BUSINESS_ALLのテーブルを描画する', () => {
                const doc = setupDocument();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('business-all-list', table);

                const items = [{
                    packageName: "com.example.business",
                    typeName: "BusinessRule",
                    incomingBusinessRuleCount: 1,
                    outgoingBusinessRuleCount: 2,
                    incomingClassCount: 3,
                }];

                ListOutputApp.renderBusinessAllTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 10);
                assert.equal(row.children[0].textContent, 'com.example.business');
                assert.equal(row.children[4].textContent, '1');
                assert.equal(row.children[4].className, 'number');
                assert.equal(row.children[5].textContent, '2');
                assert.equal(row.children[5].className, 'number');
                assert.equal(row.children[6].textContent, '3');
                assert.equal(row.children[6].className, 'number');
            });
        });

        test.describe('renderBusinessEnumTable', () => {
            test('BUSINESS_ENUMのテーブルを描画する', () => {
                const doc = setupDocument();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('business-enum-list', table);

                const items = [{
                    packageName: "com.example.business",
                    typeName: "Status",
                    usageCount: 5,
                }];

                ListOutputApp.renderBusinessEnumTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 10);
                assert.equal(row.children[0].textContent, 'com.example.business');
                assert.equal(row.children[5].textContent, '5');
                assert.equal(row.children[5].className, 'number');
            });
        });

        test.describe('renderBusinessCollectionTable', () => {
            test('BUSINESS_COLLECTIONのテーブルを描画する', () => {
                const doc = setupDocument();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('business-collection-list', table);

                const items = [{
                    packageName: "com.example.business",
                    typeName: "Users",
                    usageCount: 1,
                    methodCount: 3,
                }];

                ListOutputApp.renderBusinessCollectionTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 8);
                assert.equal(row.children[0].textContent, 'com.example.business');
                assert.equal(row.children[4].textContent, '1');
                assert.equal(row.children[4].className, 'number');
                assert.equal(row.children[6].textContent, '3');
                assert.equal(row.children[6].className, 'number');
            });
        });

        test.describe('renderBusinessValidationTable', () => {
            test('BUSINESS_VALIDATIONのテーブルを描画する', () => {
                const doc = setupDocument();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('business-validation-list', table);

                const items = [{
                    packageName: "com.example.business",
                    typeName: "User",
                    memberName: "name",
                }];

                ListOutputApp.renderBusinessValidationTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 7);
                assert.equal(row.children[0].textContent, 'com.example.business');
                assert.equal(row.children[3].textContent, 'name');
            });
        });

        test.describe('renderBusinessSmellTable', () => {
            test('BUSINESS_SMELLのテーブルを描画する', () => {
                const doc = setupDocument();
                globalThis.glossaryData = {terms: {}};
                const table = doc.createElement('table');
                const tbody = doc.createElement('tbody');
                table.appendChild(tbody);
                doc.elementsById.set('business-smell-list', table);

                const items = [{
                    packageName: "com.example.business",
                    typeName: "Smell",
                    methodSignature: "doSomething()",
                }];

                ListOutputApp.renderBusinessSmellTable(items);

                assert.equal(tbody.children.length, 1);
                const row = tbody.children[0];
                assert.equal(row.children.length, 11);
                assert.equal(row.children[0].textContent, 'com.example.business');
                assert.equal(row.children[2].textContent, 'doSomething()');
            });
        });
    });
    test.describe('タブ切り替え', () => {
        test.describe('activateTabGroup', () => {
            test('タブを切り替える', () => {
                const doc = setupDocument();
                const tab1 = doc.createElement('div');
                tab1.dataset = {tabGroup: 'test', tab: 'one'};
                const tab2 = doc.createElement('div');
                tab2.dataset = {tabGroup: 'test', tab: 'two'};

                const button1 = doc.createElement('button');
                button1.dataset = {tabGroup: 'test', tab: 'one'};
                const button2 = doc.createElement('button');
                button2.dataset = {tabGroup: 'test', tab: 'two'};

                doc.allElements.push(tab1, tab2, button1, button2);

                ListOutputApp.activateTabGroup('test', 'two');

                assert.equal(tab1.className, '');
                assert.equal(tab2.className, 'is-active');
                assert.equal(button1.className, '');
                assert.equal(button1.attributes['aria-selected'], 'false');
                assert.equal(button2.className, 'is-active');
                assert.equal(button2.attributes['aria-selected'], 'true');
            });
        });
    });
});

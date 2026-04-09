const test = require('node:test');
const assert = require('node:assert/strict');

require('../../main/resources/templates/assets/jig-util.js');
const jigData = require('../../main/resources/templates/assets/jig-data.js');

test.describe('jig-data.js', () => {

    test.beforeEach(() => {
        delete globalThis.domainData;
        delete globalThis.glossaryData;
        delete globalThis.usecaseData;
        delete globalThis.inboundData;
        delete globalThis.outboundData;
        delete globalThis.packageData;
        delete globalThis.insightData;
        delete globalThis.listData;
        delete globalThis.navigationData;
        delete globalThis.typeRelationsData;
        jigData.resetCache();
    });

    test.describe('domain', () => {
        test('has() はデータ未設定時に false', () => {
            assert.equal(jigData.domain.has(), false);
        });

        test('getTypes() はデータ未設定時に空配列', () => {
            assert.deepEqual(jigData.domain.getTypes(), []);
        });

        test('getTypesMap() は FQN→type を返しメモ化する', () => {
            globalThis.domainData = {
                types: [
                    {fqn: 'com.example.Foo'},
                    {fqn: 'com.example.Bar'},
                ],
            };
            const map1 = jigData.domain.getTypesMap();
            assert.equal(map1.size, 2);
            assert.equal(map1.get('com.example.Foo').fqn, 'com.example.Foo');

            // メモ化の検証: 2 回目は同じインスタンス
            const map2 = jigData.domain.getTypesMap();
            assert.strictEqual(map1, map2);
        });

        test('getType(fqn) は単一の type を返す', () => {
            globalThis.domainData = {types: [{fqn: 'com.example.Foo', isDeprecated: true}]};
            assert.equal(jigData.domain.getType('com.example.Foo').isDeprecated, true);
            assert.equal(jigData.domain.getType('not.found'), undefined);
        });

        test('getDomainFqnSet() は FQN の Set を返す', () => {
            globalThis.domainData = {types: [{fqn: 'a.A'}, {fqn: 'b.B'}]};
            const set = jigData.domain.getDomainFqnSet();
            assert.equal(set.has('a.A'), true);
            assert.equal(set.has('b.B'), true);
            assert.equal(set.has('c.C'), false);
        });

        test('setPackages / getPackages でパッケージ一覧をストアできる', () => {
            const packages = [{fqn: 'a'}, {fqn: 'a.b'}];
            jigData.domain.setPackages(packages);
            assert.strictEqual(jigData.domain.getPackages(), packages);
        });

        test('setChildPackagesMap / getChildPackagesMap', () => {
            const map = new Map([['a', []]]);
            jigData.domain.setChildPackagesMap(map);
            assert.strictEqual(jigData.domain.getChildPackagesMap(), map);
        });

        test('resetCache() で typesMap/fqnSet/packages がクリアされる', () => {
            globalThis.domainData = {types: [{fqn: 'a.A'}]};
            const map1 = jigData.domain.getTypesMap();
            jigData.domain.setPackages([{fqn: 'a'}]);

            jigData.resetCache();

            // データを差し替え
            globalThis.domainData = {types: [{fqn: 'b.B'}]};
            const map2 = jigData.domain.getTypesMap();
            assert.notStrictEqual(map1, map2);
            assert.equal(map2.has('b.B'), true);
            assert.equal(map2.has('a.A'), false);
            assert.equal(jigData.domain.getPackages(), null);
        });
    });

    test.describe('glossary', () => {
        test('getTerm() はデータ未設定時に undefined', () => {
            assert.equal(jigData.glossary.getTerm('any'), undefined);
        });

        test('getTerm(fqn) は terms[fqn] を返す', () => {
            globalThis.glossaryData = {terms: {'a.A': {title: 'A'}}};
            assert.deepEqual(jigData.glossary.getTerm('a.A'), {title: 'A'});
        });
    });

    test.describe('inbound', () => {
        test('get() は未設定時 null', () => {
            assert.equal(jigData.inbound.get(), null);
        });

        test('getControllers() は controllers を返す、未設定時は空配列', () => {
            assert.deepEqual(jigData.inbound.getControllers(), []);
            globalThis.inboundData = {controllers: [{fqn: 'C'}]};
            assert.deepEqual(jigData.inbound.getControllers(), [{fqn: 'C'}]);
        });
    });

    test.describe('navigation', () => {
        test('getLinks() は未設定時に空配列', () => {
            assert.deepEqual(jigData.navigation.getLinks(), []);
        });

        test('getLinks() は links を返す', () => {
            globalThis.navigationData = {links: [{href: 'x'}]};
            assert.deepEqual(jigData.navigation.getLinks(), [{href: 'x'}]);
        });
    });

    test.describe('typeRelations', () => {
        test('getRelations() は未設定時に空配列', () => {
            assert.deepEqual(jigData.typeRelations.getRelations(), []);
        });

        test('getRelations() は relations を返す', () => {
            globalThis.typeRelationsData = {relations: [{from: 'a', to: 'b'}]};
            assert.deepEqual(jigData.typeRelations.getRelations(), [{from: 'a', to: 'b'}]);
        });
    });

    test.describe('usecase', () => {
        test('has() はデータ未設定時に false', () => {
            assert.equal(jigData.usecase.has(), false);
        });

        test('has() はデータ設定時に true', () => {
            globalThis.usecaseData = {usecases: []};
            assert.equal(jigData.usecase.has(), true);
        });

        test('getTypesMap() はデータ未設定時に空 Map', () => {
            assert.equal(jigData.usecase.getTypesMap().size, 0);
        });

        test('getTypesMap() は fqn→usecase の Map を返す', () => {
            globalThis.usecaseData = {
                usecases: [
                    {fqn: 'com.example.ServiceA'},
                    {fqn: 'com.example.ServiceB'},
                ]
            };
            const map = jigData.usecase.getTypesMap();
            assert.equal(map.size, 2);
            assert.equal(map.get('com.example.ServiceA').fqn, 'com.example.ServiceA');
        });

        test('getType(fqn) は単一の usecase を返す', () => {
            globalThis.usecaseData = {usecases: [{fqn: 'com.example.ServiceA'}]};
            assert.equal(jigData.usecase.getType('com.example.ServiceA').fqn, 'com.example.ServiceA');
            assert.equal(jigData.usecase.getType('not.found'), undefined);
        });
    });

    test.describe('createTypeLinkResolver', () => {
        test('domainData にある型は domain.html# プレフィックスのリンクを返す', () => {
            globalThis.domainData = {types: [{fqn: 'com.example.Order', isDeprecated: false}]};
            const resolver = jigData.createTypeLinkResolver();
            const result = resolver('com.example.Order');
            assert.ok(result.href.startsWith('domain.html#'));
            assert.equal(result.className, undefined);
        });

        test('domainData にある deprecated 型は className: deprecated を返す', () => {
            globalThis.domainData = {types: [{fqn: 'com.example.Old', isDeprecated: true}]};
            const resolver = jigData.createTypeLinkResolver();
            const result = resolver('com.example.Old');
            assert.equal(result.className, 'deprecated');
        });

        test('usecaseData にある型は usecase.html# プレフィックスのリンクを返す', () => {
            globalThis.usecaseData = {usecases: [{fqn: 'com.example.ServiceA'}]};
            const resolver = jigData.createTypeLinkResolver();
            const result = resolver('com.example.ServiceA');
            assert.ok(result.href.startsWith('usecase.html#'));
        });

        test('どちらにもない型は null を返す', () => {
            globalThis.domainData = {types: []};
            globalThis.usecaseData = {usecases: []};
            const resolver = jigData.createTypeLinkResolver();
            assert.equal(resolver('java.lang.String'), null);
        });

        test('domainData も usecaseData もない場合は null を返す（関数自体が null）', () => {
            const resolver = jigData.createTypeLinkResolver();
            assert.equal(resolver, null);
        });

        test('domain が usecase より優先される', () => {
            globalThis.domainData = {types: [{fqn: 'com.example.Shared', isDeprecated: false}]};
            globalThis.usecaseData = {usecases: [{fqn: 'com.example.Shared'}]};
            const resolver = jigData.createTypeLinkResolver();
            const result = resolver('com.example.Shared');
            assert.ok(result.href.startsWith('domain.html#'));
        });
    });

    test.describe('その他の dataset', () => {
        test('usecase/outbound/package/insight/list の get() が globalThis を読む', () => {
            globalThis.usecaseData = {u: 1};
            globalThis.outboundData = {o: 1};
            globalThis.packageData = {p: 1};
            globalThis.insightData = {i: 1};
            globalThis.listData = {l: 1};
            assert.deepEqual(jigData.usecase.get(), {u: 1});
            assert.deepEqual(jigData.outbound.get(), {o: 1});
            assert.deepEqual(jigData.package.get(), {p: 1});
            assert.deepEqual(jigData.insight.get(), {i: 1});
            assert.deepEqual(jigData.list.get(), {l: 1});
        });
    });
});

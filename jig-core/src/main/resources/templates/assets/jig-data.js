globalThis.Jig ??= {};

/**
 * globalThis.<xxx>Data への唯一の窓口。
 *
 * 各 asset スクリプト（domain.js / usecase.js / ...）は globalThis を直接参照せず、
 * この名前空間を経由してデータを取得する。派生キャッシュ（FQN→type の Map など）も
 * 元データを汚さず、このモジュール内部のクロージャに保持する。
 */
globalThis.Jig.data = (() => {

    // ---- domain ----

    /** @type {Map<string, object>|null} */
    let domainTypesMap = null;
    /** @type {Set<string>|null} */
    let domainFqnSet = null;
    /** @type {object[]|null} */
    let domainPackages = null;
    /** @type {Map<string, object[]>|null} */
    let domainChildPackagesMap = null;

    const domain = {
        get() {
            return globalThis.domainData;
        },
        has() {
            return !!globalThis.domainData;
        },
        getTypes() {
            return globalThis.domainData?.types ?? [];
        },
        getDomainPackageRoots() {
            return globalThis.domainData?.domainPackageRoots ?? [];
        },
        getTypesMap() {
            if (!domainTypesMap) {
                domainTypesMap = new Map(domain.getTypes().map(t => [t.fqn, t]));
            }
            return domainTypesMap;
        },
        getType(fqn) {
            return domain.getTypesMap().get(fqn);
        },
        getDomainFqnSet() {
            if (!domainFqnSet) {
                domainFqnSet = new Set(domain.getTypes().map(t => t.fqn));
            }
            return domainFqnSet;
        },
        getPackages() {
            return domainPackages;
        },
        setPackages(packages) {
            domainPackages = packages;
        },
        getChildPackagesMap() {
            return domainChildPackagesMap;
        },
        setChildPackagesMap(map) {
            domainChildPackagesMap = map;
        },
    };

    // ---- glossary ----

    const glossary = {
        get() {
            return globalThis.glossaryData;
        },
        getTerm(fqn) {
            return globalThis.glossaryData?.terms?.[fqn];
        },
    };

    // ---- usecase ----

    const usecase = {
        get() {
            return globalThis.usecaseData;
        },
    };

    // ---- inbound ----

    const inbound = {
        get() {
            return globalThis.inboundData ?? null;
        },
        getControllers() {
            return globalThis.inboundData?.controllers ?? [];
        },
    };

    // ---- outbound ----

    const outbound = {
        get() {
            return globalThis.outboundData;
        },
    };

    // ---- package ----

    const pkg = {
        get() {
            return globalThis.packageData;
        },
    };

    // ---- insight ----

    const insight = {
        get() {
            return globalThis.insightData ?? null;
        },
    };

    // ---- list-output ----

    const list = {
        get() {
            return globalThis.listData;
        },
    };

    // ---- navigation ----

    const navigation = {
        get() {
            return globalThis.navigationData;
        },
        getLinks() {
            return globalThis.navigationData?.links ?? [];
        },
    };

    // ---- type-relations ----

    const typeRelations = {
        get() {
            return globalThis.typeRelationsData;
        },
        getRelations() {
            return globalThis.typeRelationsData?.relations ?? [];
        },
    };

    /**
     * 全ての派生キャッシュを破棄する。
     * テスト間で globalThis.<xxx>Data を差し替える場合に呼び出す。
     */
    function resetCache() {
        domainTypesMap = null;
        domainFqnSet = null;
        domainPackages = null;
        domainChildPackagesMap = null;
    }

    return {
        domain,
        glossary,
        usecase,
        inbound,
        outbound,
        package: pkg,
        insight,
        list,
        navigation,
        typeRelations,
        resetCache,
    };
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.data;
}

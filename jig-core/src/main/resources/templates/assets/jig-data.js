globalThis.Jig ??= {};

/**
 * globalThis.<xxx>Data への唯一の窓口。
 *
 * 各 asset スクリプト（domain.js / usecase.js / ...）は globalThis を直接参照せず、
 * この名前空間を経由してデータを取得する。派生キャッシュ（FQN→type の Map など）も
 * 元データを汚さず、このモジュール内部のクロージャに保持する。
 */
globalThis.Jig.data = (() => {

    /** @type {Map<string, object>|null} */
    let domainTypesMap = null;
    /** @type {Set<string>|null} */
    let domainFqnSet = null;
    /** @type {object[]|null} */
    let domainPackages = null;
    /** @type {Map<string, object[]>|null} */
    let domainChildPackagesMap = null;
    /** @type {Map<string, object>|null} */
    let usecaseTypesMap = null;

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

    const glossary = {
        get() {
            return globalThis.glossaryData;
        },
        has() {
            return !!globalThis.glossaryData;
        },
        getTerm(fqn) {
            return globalThis.glossaryData?.terms?.[fqn];
        },
    };

    const usecase = {
        get() {
            return globalThis.usecaseData;
        },
        has() {
            return !!globalThis.usecaseData;
        },
        getTypesMap() {
            if (!usecaseTypesMap) {
                usecaseTypesMap = new Map(
                    (globalThis.usecaseData?.usecases ?? []).map(t => [t.fqn, t])
                );
            }
            return usecaseTypesMap;
        },
        getType(fqn) {
            return usecase.getTypesMap().get(fqn);
        },
    };

    const inbound = {
        get() {
            return globalThis.inboundData ?? null;
        },
        getControllers() {
            return globalThis.inboundData?.inboundAdapters ?? [];
        },
    };

    const outbound = {
        get() {
            return globalThis.outboundData;
        },
    };

    const pkg = {
        get() {
            return globalThis.packageData;
        },
    };

    const insight = {
        get() {
            return globalThis.insightData ?? null;
        },
    };

    const list = {
        get() {
            return globalThis.listData;
        },
    };

    const navigation = {
        get() {
            return globalThis.navigationData;
        },
        getLinks() {
            return globalThis.navigationData?.links ?? [];
        },
    };

    const summary = {
        get() {
            return globalThis.summaryData;
        },
        getGit() {
            return globalThis.summaryData?.git ?? null;
        },
        getStats() {
            return globalThis.summaryData?.stats ?? null;
        },
    };

    const typeRelations = {
        get() {
            return globalThis.typeRelationsData;
        },
        getRelations() {
            return globalThis.typeRelationsData?.relations ?? [];
        },
    };

    /**
     * domain の派生キャッシュ（typesMap / fqnSet / packages / childPackagesMap）を破棄する。
     * データが入れ替わる際（init の再実行など）に呼び出す。
     */
    function resetCache() {
        domainTypesMap = null;
        domainFqnSet = null;
        domainPackages = null;
        domainChildPackagesMap = null;
        usecaseTypesMap = null;
    }

    /**
     * 利用可能なデータに基づいて型リンクリゾルバーを生成する。
     * domain型があれば domain.html へ、usecase型があれば usecase.html へリンクする。
     * 現在のページと同じドキュメントへのリンクはページ内アンカー（#）になる。
     *
     * @returns {(fqn: string) => {href?: string, className?: string, text?: string} | null}
     */
    function createTypeLinkResolver() {
        if (!domain.has() && !usecase.has() && !glossary.has()) return null;

        const currentPage = (typeof location !== 'undefined')
            ? location.pathname.split('/').pop()
            : '';

        // 解決順は配列の順序に従う。各 entry は対象データの保有判定・型取得・遷移先ページ等を表す。
        const resolvers = [
            {has: domain.has, find: domain.getType, page: 'domain.html', idPrefix: 'domain', deprecatedAware: true},
            {has: usecase.has, find: usecase.getType, page: 'usecase.html', idPrefix: 'type', deprecatedAware: true},
            {has: glossary.has, find: glossary.getTerm, page: 'glossary.html', idPrefix: 'term', deprecatedAware: false},
        ];

        return function (fqn) {
            for (const r of resolvers) {
                if (!r.has()) continue;
                const found = r.find(fqn);
                if (!found) continue;
                const prefix = (currentPage === r.page) ? '#' : `${r.page}#`;
                const result = {href: prefix + globalThis.Jig.util.fqnToId(r.idPrefix, fqn)};
                if (r.deprecatedAware && found.isDeprecated) result.className = 'deprecated';
                return result;
            }
            return {
                className: 'weak',
                text: fqn.substring(fqn.lastIndexOf('.') + 1)
            };
        };
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
        summary,
        typeRelations,
        resetCache,
        createTypeLinkResolver,
    };
})();

if (typeof module !== "undefined" && module.exports) {
    module.exports = globalThis.Jig.data;
}

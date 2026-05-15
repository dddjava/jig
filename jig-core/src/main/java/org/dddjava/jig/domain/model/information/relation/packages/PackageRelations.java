package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * パッケージの関連一覧
 *
 * @param relations from/toでユニーク化されたパッケージ関連のコレクション（自己参照は含まない）
 */
public record PackageRelations(Collection<PackageRelation> relations) {

    /**
     * クラスの関連をパッケージの関連に丸める。
     * すべての型が非Deprecated扱いとなる。テスト用途。
     */
    public static PackageRelations from(TypeRelationships typeRelationships) {
        return from(typeRelationships, typeId -> false);
    }

    /**
     * クラスの関連をパッケージの関連に丸める。
     *
     * @param isDeprecated 型がDeprecatedかどうかを判定する関数
     */
    public static PackageRelations from(TypeRelationships typeRelationships, Predicate<TypeId> isDeprecated) {
        // (from, to) ごとに「すべての構成クラス関連がDeprecated絡みか」を集約する
        Map<Map.Entry<PackageId, PackageId>, Boolean> deprecatedOnlyMap = new LinkedHashMap<>();
        for (var rel : typeRelationships.relationships()) {
            var fromPkg = rel.from().packageId();
            var toPkg = rel.to().packageId();
            if (fromPkg.equals(toPkg)) continue;
            var key = Map.entry(fromPkg, toPkg);
            boolean thisIsDeprecated = isDeprecated.test(rel.from()) || isDeprecated.test(rel.to());
            deprecatedOnlyMap.merge(key, thisIsDeprecated, Boolean::logicalAnd);
        }
        var packageRelations = deprecatedOnlyMap.entrySet().stream()
                .map(e -> new PackageRelation(e.getKey().getKey(), e.getKey().getValue(), e.getValue()))
                .toList();
        return new PackageRelations(packageRelations);
    }

    /**
     * from,toの名前順のリストを生成する
     */
    public List<PackageRelation> listUnique() {
        return relations.stream()
                .sorted(Comparator.<PackageRelation, PackageId>comparing(pr -> pr.from()).thenComparing(pr -> pr.to()))
                .toList();
    }
}

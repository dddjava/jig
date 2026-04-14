package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.Comparator;
import java.util.List;

/**
 * パッケージの依存関係
 *
 * @param relations 重複・自己参照・順序に関わらず保持するコレクション
 */
public record PackageRelations(List<PackageRelation> relations) {

    /**
     * クラスの関連をパッケージの関連に丸める
     */
    public static PackageRelations from(TypeRelationships typeRelationships) {
        var packageRelations = typeRelationships.typeRelationships().stream()
                .map(classRelation -> PackageRelation.from(classRelation.from().packageId(), classRelation.to().packageId()))
                .toList();
        return new PackageRelations(packageRelations);
    }

    /**
     * 重複と自己参照を除いた上で、from,toの名前順のリストを生成する
     */
    public List<PackageRelation> listUnique() {
        return relations.stream()
                // 自己参照を除く
                .filter(pr -> !pr.from().equals(pr.to()))
                // 重複を除く
                .distinct()
                .sorted(Comparator.<PackageRelation, PackageId>comparing(pr -> pr.from()).thenComparing(pr -> pr.to()))
                .toList();
    }
}

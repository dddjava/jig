package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;
import org.dddjava.jig.domain.model.information.relation.graph.Edges;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.List;

/**
 * パッケージの依存関係
 *
 * @param relations 重複・自己参照・順序に関わらず保持するコレクション
 */
public record PackageRelations(Edges<PackageId> relations) {

    /**
     * クラスの関連をパッケージの関連に丸める
     */
    public static PackageRelations from(TypeRelationships typeRelationships) {
        var packageRelationEdges = new Edges<>(typeRelationships.typeRelationships().stream()
                .map(classRelation -> Edge.of(classRelation.from().packageId(), classRelation.to().packageId()))
                .toList());
        return new PackageRelations(packageRelationEdges);
    }

    /**
     * 重複と自己参照を除いた上で、from,toの名前順のリストを生成する
     */
    public List<PackageRelation> listUnique() {
        return relations.edges().stream()
                // 自己参照を除く
                .filter(edge -> !edge.from().equals(edge.to()))
                // 重複を除く
                .distinct()
                .sorted() // packageRelationはComparableではないので変換前に並び替える
                .map(edge -> PackageRelation.from(edge.from(), edge.to()))
                .toList();
    }
}

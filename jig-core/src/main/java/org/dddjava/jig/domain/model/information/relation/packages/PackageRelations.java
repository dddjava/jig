package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.packages.PackageIds;
import org.dddjava.jig.domain.model.information.relation.graph.Edge;
import org.dddjava.jig.domain.model.information.relation.graph.Edges;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toSet;

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

    public static Edges<PackageId> toEdges(Collection<PackageRelation> relations) {
        return new Edges<>(relations.stream().map(PackageRelation::edge).toList());
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

    /**
     * 指定された深さに切り詰める
     */
    public PackageRelations applyDepth(PackageDepth packageDepth) {
        return new PackageRelations(new Edges<>(
                relations.edges().stream()
                        .map(edge -> Edge.of(edge.from().applyDepth(packageDepth), edge.to().applyDepth(packageDepth)))
                        .toList()));
    }

    public boolean available() {
        return !relations.isEmpty();
    }

    public PackageIds packageIds() {
        return new PackageIds(relations.nodeStream().collect(toSet()));
    }
}

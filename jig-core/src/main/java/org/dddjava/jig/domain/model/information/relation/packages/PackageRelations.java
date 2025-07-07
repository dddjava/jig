package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.packages.PackageIds;
import org.dddjava.jig.domain.model.information.relation.graph.Edges;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * パッケージの依存関係
 *
 * @param relations 重複・自己参照・順序に関わらず保持するコレクション
 */
public record PackageRelations(Collection<PackageRelation> relations) {

    /**
     * クラスの関連をパッケージの関連に丸める
     */
    public static PackageRelations from(TypeRelationships typeRelationships) {
        return new PackageRelations(typeRelationships.typeRelationships().stream()
                .map(classRelation -> PackageRelation.from(
                        classRelation.from().packageId(), classRelation.to().packageId()))
                .toList());
    }

    public static Edges<PackageId> toEdges(Collection<PackageRelation> relations) {
        return new Edges<>(relations.stream().map(PackageRelation::edge).toList());
    }

    /**
     * 重複と自己参照を除いた上で、from,toの名前順のリストを生成する
     */
    public List<PackageRelation> listUnique() {
        return relations.stream()
                .distinct()
                .filter(PackageRelation::notSelfRelation)
                .sorted(Comparator.comparing((PackageRelation packageRelation) -> packageRelation.from().asText())
                        .thenComparing(packageRelation -> packageRelation.to().asText()))
                .toList();
    }

    /**
     * 関連数
     */
    public RelationNumber number() {
        return new RelationNumber(listUnique().size());
    }

    /**
     * 指定された深さに切り詰める
     */
    public PackageRelations applyDepth(PackageDepth packageDepth) {
        return new PackageRelations(this.relations.stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .toList());
    }

    public boolean available() {
        return !relations.isEmpty();
    }

    public PackageIds packageIds() {
        return new PackageIds(relations.stream()
                .flatMap(relation -> Stream.of(relation.from(), relation.to()))
                .collect(Collectors.toSet()));
    }
}

package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * パッケージの依存関係
 */
public class PackageRelations {

    /**
     * 重複・自己参照・順序に関わらず保持するコレクション
     */
    private final Collection<PackageRelation> relations;

    public PackageRelations(Collection<PackageRelation> relations) {
        this.relations = relations;
    }

    /**
     * クラスの関連をパッケージの関連に丸める
     */
    public static PackageRelations from(ClassRelations classRelations) {
        return new PackageRelations(classRelations.list().stream()
                .map(classRelation -> new PackageRelation(
                        classRelation.from().packageIdentifier(), classRelation.to().packageIdentifier()))
                .toList());
    }

    /**
     * 重複と自己参照を除いた上で、from,toの名前順のリストを生成する
     */
    public List<PackageRelation> list() {
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
        return new RelationNumber(list().size());
    }

    /**
     * 指定された深さに切り詰める
     */
    public PackageRelations applyDepth(PackageDepth packageDepth) {
        return new PackageRelations(this.relations.stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .toList());
    }

    /**
     * 指定されたパッケージ間の参照のみにフィルタリングする
     */
    public PackageRelations filterInternal(PackageIdentifiers packageIdentifiers) {
        return new PackageRelations(this.relations.stream()
                .filter(packageDependency -> packageDependency.bothMatch(packageIdentifiers))
                .toList());
    }

    public boolean available() {
        return !relations.isEmpty();
    }

    public PackageIdentifiers packageIdentifiers() {
        var packageIdentifiers = relations.stream()
                .flatMap(relation -> Stream.of(relation.from(), relation.to()))
                .distinct()
                .toList();
        return new PackageIdentifiers(packageIdentifiers);
    }
}

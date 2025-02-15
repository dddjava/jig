package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * パッケージの依存関係一覧
 */
public class PackageRelations {

    private final Collection<PackageRelation> relations;

    public PackageRelations(Collection<PackageRelation> relations) {
        this.relations = relations;
    }

    public static PackageRelations from(ClassRelations classRelations) {
        var collect = classRelations.list().stream()
                .map(classRelation -> new PackageRelation(
                        classRelation.from().packageIdentifier(), classRelation.to().packageIdentifier()))
                .filter(PackageRelation::notSelfRelation)
                .collect(Collectors.toSet());
        return new PackageRelations(collect);
    }

    public List<PackageRelation> list() {
        return new ArrayList<>(relations);
    }

    public PackageRelations applyDepth(PackageDepth packageDepth) {
        var newSet = this.relations.stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .filter(PackageRelation::notSelfRelation)
                .collect(Collectors.toSet());
        return new PackageRelations(newSet);
    }

    public RelationNumber number() {
        return new RelationNumber(relations.size());
    }

    public PackageRelations filterBothMatch(PackageIdentifiers packageIdentifiers) {
        var collection = this.relations.stream()
                .filter(packageDependency -> packageDependency.bothMatch(packageIdentifiers))
                .collect(Collectors.toSet());

        return new PackageRelations(collection);
    }

    public boolean available() {
        return !relations.isEmpty();
    }
}

package org.dddjava.jig.domain.model.jigmodel.relation.packages;

import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.relation.class_.ClassRelations;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * パッケージの依存関係一覧
 */
public class PackageRelations {

    List<PackageRelation> dependencies;

    public PackageRelations(List<PackageRelation> dependencies) {
        this.dependencies = dependencies;
    }

    public static PackageRelations fromClassRelations(ClassRelations classRelations) {
        List<PackageRelation> packageRelationList = classRelations.list().stream()
                .map(PackageRelation::fromClassRelation)
                .filter(PackageRelation::notSelfRelation)
                .distinct()
                .collect(Collectors.toList());

        return new PackageRelations(packageRelationList);
    }

    public List<PackageRelation> list() {
        return dependencies;
    }

    public PackageRelations applyDepth(PackageDepth packageDepth) {
        List<PackageRelation> list = this.dependencies.stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .distinct()
                .filter(PackageRelation::notSelfRelation)
                .collect(toList());
        return new PackageRelations(list);
    }

    public RelationNumber number() {
        return new RelationNumber(list().size());
    }

    public PackageRelations filterBothMatch(PackageIdentifiers packageIdentifiers) {
        List<PackageRelation> list = dependencies.stream()
                .filter(packageDependency -> packageDependency.bothMatch(packageIdentifiers))
                .collect(Collectors.toList());
        return new PackageRelations(list);
    }

    public boolean available() {
        return !dependencies.isEmpty();
    }
}

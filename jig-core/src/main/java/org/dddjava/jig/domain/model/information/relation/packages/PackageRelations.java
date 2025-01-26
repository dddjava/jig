package org.dddjava.jig.domain.model.information.relation.packages;

import org.dddjava.jig.domain.model.data.packages.PackageDepth;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifiers;
import org.dddjava.jig.domain.model.data.packages.RelationNumber;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelation;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static java.util.stream.Collectors.groupingBy;

/**
 * パッケージの依存関係一覧
 */
public class PackageRelations {

    Map<PackageRelation, List<PackageRelation>> map;

    public PackageRelations(List<PackageRelation> list) {
        this(list.stream().collect(groupingBy(Function.identity())));
    }

    public PackageRelations(Map<PackageRelation, List<PackageRelation>> map) {
        this.map = map;
    }

    public static PackageRelations from(ClassRelations classRelations) {
        Map<PackageRelation, List<PackageRelation>> map = classRelations.list().stream()
                .map(PackageRelations::toPackageRelation)
                .filter(PackageRelation::notSelfRelation)
                .collect(groupingBy(Function.identity()));
        return new PackageRelations(map);
    }

    private static PackageRelation toPackageRelation(ClassRelation classRelation) {
        return new PackageRelation(classRelation.from().packageIdentifier(), classRelation.to().packageIdentifier());
    }

    public List<PackageRelation> list() {
        return new ArrayList<>(map.keySet());
    }

    public PackageRelations applyDepth(PackageDepth packageDepth) {
        Map<PackageRelation, List<PackageRelation>> map = this.list().stream()
                .map(relation -> relation.applyDepth(packageDepth))
                .filter(PackageRelation::notSelfRelation)
                .collect(groupingBy(Function.identity()));
        return new PackageRelations(map);
    }

    public RelationNumber number() {
        return new RelationNumber(list().size());
    }

    public PackageRelations filterBothMatch(PackageIdentifiers packageIdentifiers) {
        Map<PackageRelation, List<PackageRelation>> map = this.map.keySet().stream()
                .filter(packageDependency -> packageDependency.bothMatch(packageIdentifiers))
                .collect(groupingBy(Function.identity()));
        return new PackageRelations(map);
    }

    public boolean available() {
        return !map.isEmpty();
    }
}

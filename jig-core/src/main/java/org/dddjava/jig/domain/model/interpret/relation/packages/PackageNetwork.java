package org.dddjava.jig.domain.model.interpret.relation.packages;

import org.dddjava.jig.domain.model.declaration.package_.PackageDepth;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifiers;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelations;

import java.util.Collections;
import java.util.StringJoiner;

/**
 * パッケージの関連
 */
public class PackageNetwork {

    PackageIdentifiers packageIdentifiers;
    PackageRelations packageRelations;
    ClassRelations classRelations;
    PackageDepth appliedDepth;
    BidirectionalRelations bidirectionalRelations;

    public PackageNetwork(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations, ClassRelations classRelations) {
        this(packageIdentifiers, packageRelations, classRelations, new PackageDepth(-1));
    }

    private PackageNetwork(PackageIdentifiers packageIdentifiers, PackageRelations packageRelations, ClassRelations classRelations, PackageDepth appliedDepth) {
        this.packageIdentifiers = packageIdentifiers;
        this.packageRelations = packageRelations.filterBothMatch(packageIdentifiers);
        this.classRelations = classRelations;
        this.appliedDepth = appliedDepth;
        this.bidirectionalRelations = BidirectionalRelations.from(this.packageRelations);
    }

    public static PackageNetwork empty() {
        return new PackageNetwork(
                new PackageIdentifiers(Collections.emptyList()),
                new PackageRelations(Collections.emptyList()),
                null,
                new PackageDepth(-1)
        );
    }

    public PackageIdentifiers allPackages() {
        return packageIdentifiers;
    }

    public PackageRelations packageDependencies() {
        return packageRelations;
    }

    public PackageNetwork applyDepth(PackageDepth depth) {
        return new PackageNetwork(
                packageIdentifiers.applyDepth(depth),
                packageRelations.applyDepth(depth),
                this.classRelations,
                depth
        );
    }

    public PackageDepth appliedDepth() {
        return appliedDepth;
    }

    public boolean available() {
        return packageDependencies().available();
    }

    public PackageDepth maxDepth() {
        return packageIdentifiers.maxDepth();
    }

    public BidirectionalRelations bidirectionalRelations() {
        return bidirectionalRelations;
    }

    public boolean hasBidirectionalRelation() {
        return !bidirectionalRelations().list.isEmpty();
    }

    public String bidirectionalRelationReasonText() {
        StringJoiner sj = new StringJoiner("\n");
        for (BidirectionalRelation bidirectionalRelation : bidirectionalRelations().list) {
            sj.add("# " + bidirectionalRelation.toString());
            String package1 = bidirectionalRelation.packageRelation.from.asText();
            String package2 = bidirectionalRelation.packageRelation.to.asText();
            for (ClassRelation classRelation : classRelations.list()) {
                String from = classRelation.from().fullQualifiedName();
                String to = classRelation.to().fullQualifiedName();

                if ((from.startsWith(package1) && to.startsWith(package2))
                        || (from.startsWith(package2) && to.startsWith(package1))) {
                    sj.add("- " + classRelation.toString());
                }
            }
        }
        return sj.toString();
    }
}

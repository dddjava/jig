package org.dddjava.jig.domain.model.parts.relation.packages;

import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

/**
 * 相互依存
 */
public class BidirectionalRelation {
    PackageRelation packageRelation;

    public BidirectionalRelation(PackageRelation packageRelation) {
        this.packageRelation = packageRelation;
    }

    public boolean matches(PackageRelation packageRelation) {
        PackageIdentifier left = this.packageRelation.from;
        PackageIdentifier right = this.packageRelation.to;
        return (left.equals(packageRelation.from()) && right.equals(packageRelation.to())) ||
                (left.equals(packageRelation.to()) && right.equals(packageRelation.from()));
    }

    @Override
    public String toString() {
        return packageRelation.from.asText() + " <-> " + packageRelation.to.asText();
    }

    public boolean matches(ClassRelation classRelation) {
        TypeIdentifier fromClass = classRelation.from();
        TypeIdentifier toClass = classRelation.to();
        return packageRelation.matches(fromClass, toClass) || packageRelation.matches(toClass, fromClass);
    }

}

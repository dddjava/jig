package org.dddjava.jig.domain.model.parts.packages;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

/**
 * 相互依存一覧
 */
public class BidirectionalRelations {

    List<PackageMutualDependency> list;

    private BidirectionalRelations(List<PackageMutualDependency> list) {
        this.list = list;
    }

    public boolean notContains(PackageRelation packageRelation) {
        for (PackageMutualDependency packageMutualDependency : list) {
            if (packageMutualDependency.matches(packageRelation)) {
                return false;
            }
        }
        return true;
    }

    public static BidirectionalRelations from(PackageRelations packageRelations) {
        List<PackageMutualDependency> list = new ArrayList<>();
        BidirectionalRelations bidirectionalRelations = new BidirectionalRelations(list);

        for (PackageRelation packageRelation : packageRelations.list()) {
            for (PackageRelation right : packageRelations.list()) {
                if (packageRelation.from().equals(right.to()) && packageRelation.to().equals(right.from())) {
                    if (bidirectionalRelations.notContains(packageRelation)) {
                        list.add(new PackageMutualDependency(packageRelation));
                    }
                }
            }
        }
        return bidirectionalRelations;
    }

    public String dotRelationText() {
        StringJoiner stringJoiner = new StringJoiner("\n")
                .add("edge [color=red,dir=both,style=bold];");
        for (PackageMutualDependency packageMutualDependency : list) {
            stringJoiner.add(packageMutualDependency.dotText());
        }
        return stringJoiner.toString();
    }

    public boolean none() {
        return list.isEmpty();
    }

    public List<PackageMutualDependency> list() {
        return list;
    }
}

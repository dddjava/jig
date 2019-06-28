package org.dddjava.jig.domain.model.fact.relation.packages;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 相互依存一覧
 */
public class BidirectionalRelations {

    private final List<BidirectionalRelation> list;

    private BidirectionalRelations(List<BidirectionalRelation> list) {
        this.list = list;
    }

    public List<BidirectionalRelation> list() {
        return list;
    }

    public PackageRelations filterBidirectionalFrom(PackageRelations packageRelations) {
        List<PackageRelation> simplexDependencies = packageRelations.list()
                .stream()
                .filter(this::notContains)
                .collect(Collectors.toList());
        return new PackageRelations(simplexDependencies);
    }

    private boolean notContains(PackageRelation packageRelation) {
        for (BidirectionalRelation bidirectionalRelation : list) {
            if (bidirectionalRelation.matches(packageRelation)) {
                return false;
            }
        }
        return true;
    }

    public static BidirectionalRelations from(PackageRelations packageRelations) {
        List<BidirectionalRelation> list = new ArrayList<>();
        BidirectionalRelations bidirectionalRelations = new BidirectionalRelations(list);

        for (PackageRelation left : packageRelations.list()) {
            for (PackageRelation right : packageRelations.list()) {
                if (left.from().equals(right.to()) && left.to().equals(right.from())) {
                    if (bidirectionalRelations.notContains(left)) {
                        list.add(new BidirectionalRelation(left.from(), left.to()));
                    }
                }
            }
        }
        return bidirectionalRelations;
    }
}

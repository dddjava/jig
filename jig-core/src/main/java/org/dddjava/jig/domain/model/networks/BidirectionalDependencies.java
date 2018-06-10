package org.dddjava.jig.domain.model.networks;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 相互依存一覧
 */
public class BidirectionalDependencies {

    private final List<BidirectionalDependency> list;

    private BidirectionalDependencies(List<BidirectionalDependency> list) {
        this.list = list;
    }

    public List<BidirectionalDependency> list() {
        return list;
    }

    public PackageDependencies filterBidirectionalFrom(PackageDependencies packageDependencies) {
        List<PackageDependency> simplexDependencies = packageDependencies.list()
                .stream()
                .filter(this::notContains)
                .collect(Collectors.toList());
        return new PackageDependencies(simplexDependencies);
    }

    private boolean notContains(PackageDependency packageDependency) {
        for (BidirectionalDependency bidirectionalDependency : list) {
            if (bidirectionalDependency.matches(packageDependency)) {
                return false;
            }
        }
        return true;
    }

    public static BidirectionalDependencies from(PackageDependencies packageDependencies) {
        List<BidirectionalDependency> list = new ArrayList<>();
        for (PackageDependency left : packageDependencies.list()) {
            for (PackageDependency right : packageDependencies.list()) {
                if (left.from().equals(right.to()) && left.to().equals(right.from())) {
                    list.add(new BidirectionalDependency(left.from(), left.to()));
                }
            }
        }
        return new BidirectionalDependencies(list);
    }
}

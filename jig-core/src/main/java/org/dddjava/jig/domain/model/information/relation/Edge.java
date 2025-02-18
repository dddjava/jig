package org.dddjava.jig.domain.model.information.relation;

import java.util.Collection;

public record Edge<NODE>(NODE from, NODE to) implements Comparable<Edge<NODE>> {

    boolean bothEndpointsIn(Collection<NODE> nodes) {
        return nodes.contains(from) && nodes.contains(to);
    }

    @Override
    public int compareTo(Edge<NODE> o) {
        int fromComparison = this.from.toString().compareTo(o.from.toString());
        return fromComparison != 0 ? fromComparison : this.to.toString().compareTo(o.to.toString());
    }
}

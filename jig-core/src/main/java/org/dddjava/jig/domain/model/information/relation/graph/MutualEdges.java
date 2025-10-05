package org.dddjava.jig.domain.model.information.relation.graph;

import java.util.Set;

public record MutualEdges<T>(Set<Edge<T>> edges) {
}

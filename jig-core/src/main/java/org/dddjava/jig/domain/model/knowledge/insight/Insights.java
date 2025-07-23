package org.dddjava.jig.domain.model.knowledge.insight;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public record Insights(Collection<Insight> values) {
    public List<Insight> list() {
        return values.stream()
                .sorted(Comparator.comparing(Insight::fqn))
                .toList();
    }
}

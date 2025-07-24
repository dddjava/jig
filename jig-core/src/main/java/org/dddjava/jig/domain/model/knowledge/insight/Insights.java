package org.dddjava.jig.domain.model.knowledge.insight;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public record Insights(Collection<MethodInsight> values) {
    public List<MethodInsight> methodList() {
        return values.stream()
                .sorted(Comparator.comparing(MethodInsight::fqn))
                .toList();
    }
}

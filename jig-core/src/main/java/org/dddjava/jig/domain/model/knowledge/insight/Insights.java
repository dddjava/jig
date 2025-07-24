package org.dddjava.jig.domain.model.knowledge.insight;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record Insights(Collection<MethodInsight> values) {
    public List<TypeInsight> typeInsightList() {
        return values.stream()
                .collect(Collectors.groupingBy(MethodInsight::typeId, Collectors.toList()))
                .entrySet()
                .stream()
                .map(entry -> new TypeInsight(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(TypeInsight::fqn))
                .toList();
    }

    public List<MethodInsight> methodInsightList() {
        return values.stream()
                .sorted(Comparator.comparing(MethodInsight::fqn))
                .toList();
    }
}

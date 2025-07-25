package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

import static java.util.function.Predicate.not;

public record TypeInsight(TypeId typeId, Collection<MethodInsight> methodInsights) {
    public String fqn() {
        return typeId.value();
    }

    public String label() {
        return typeId.asSimpleText();
    }

    public int numberOfMethods() {
        return methodInsights.size();
    }

    public int numberOfUsingTypes() {
        return Math.toIntExact(methodInsights.stream()
                .flatMap(methodInsight -> methodInsight.jigMethod().usingTypes().values().stream())
                .filter(not(TypeId::isJavaLanguageType))
                .distinct()
                .count());
    }

    public int cyclomaticComplexity() {
        return methodInsights.stream()
                .mapToInt(MethodInsight::cyclomaticComplexity)
                .sum();
    }

    public int size() {
        return methodInsights.stream()
                .mapToInt(MethodInsight::size)
                .sum();
    }

    public String packageFqn() {
        return typeId.packageId().asText();
    }
}

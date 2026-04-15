package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.packages.PackageId;

import java.util.Collection;

public record PackageInsight(PackageId packageId, Collection<TypeInsight> typeInsights) {
    public String fqn() {
        return packageId.asText();
    }

    public int numberOfTypes() {
        return typeInsights.size();
    }

    public int numberOfMethods() {
        return typeInsights.stream().mapToInt(TypeInsight::numberOfMethods).sum();
    }

    public int numberOfUsingTypes() {
        return Math.toIntExact(typeInsights.stream()
                .flatMap(typeInsight -> typeInsight.methodInsights().stream())
                .mapToLong(methodInsight -> methodInsight.jigMethod().usingTypes().size())
                .sum());
    }

    public int cyclomaticComplexity() {
        return typeInsights.stream()
                .mapToInt(TypeInsight::cyclomaticComplexity)
                .sum();
    }

    public int size() {
        return typeInsights.stream()
                .mapToInt(TypeInsight::size)
                .sum();
    }
}

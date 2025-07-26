package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;

import static java.util.function.Predicate.not;

public record PackageInsight(PackageId packageId, Term term, Collection<TypeInsight> typeInsights) {
    public String fqn() {
        return packageId.asText();
    }

    public String label() {
        return term.title();
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
                .flatMap(methodInsight -> methodInsight.jigMethod().usingTypes().values().stream())
                .filter(not(TypeId::isJavaLanguageType))
                .distinct()
                .count());
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

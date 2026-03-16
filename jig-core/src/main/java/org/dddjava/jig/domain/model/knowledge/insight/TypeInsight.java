package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;

import java.util.Collection;

import static java.util.function.Predicate.not;

public record TypeInsight(TypeId typeId, Term term, Collection<MethodInsight> methodInsights) {
    public String fqn() {
        return typeId.value();
    }

    public String label() {
        return term.title();
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

    public int numberOfUsedByTypes(TypeRelationships typeRelationships) {
        return typeRelationships.collectTypeIdWhichRelationTo(typeId).size();
    }

    public double instability(TypeRelationships typeRelationships) {
        int ce = numberOfUsingTypes();
        int ca = numberOfUsedByTypes(typeRelationships);
        int total = ce + ca;
        if (total == 0) return 0.0;
        return (double) ce / total;
    }

    public String packageFqn() {
        return typeId.packageId().asText();
    }
}

package org.dddjava.jig.domain.model.knowledge.insight;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

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

    public double lcom(JigTypes jigTypes) {
        return jigTypes.resolveJigType(typeId)
                .map(this::computeLcom)
                .orElse(0.0);
    }

    private double computeLcom(JigType jigType) {
        var instanceFields = jigType.instanceJigFields().fields();
        int F = instanceFields.size();
        if (F == 0) return 0.0;

        var instanceMethods = jigType.instanceJigMethodStream().toList();
        int M = instanceMethods.size();
        if (M <= 1) return 0.0;

        Set<JigFieldId> fieldIds = instanceFields.stream()
                .map(field -> JigFieldId.from(jigType.id(), field.nameText()))
                .collect(Collectors.toSet());

        double sumMF = fieldIds.stream()
                .mapToLong(fieldId -> instanceMethods.stream()
                        .filter(m -> m.usingFields().jigFieldIds().contains(fieldId))
                        .count())
                .sum();

        return (M - sumMF / F) / (M - 1);
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

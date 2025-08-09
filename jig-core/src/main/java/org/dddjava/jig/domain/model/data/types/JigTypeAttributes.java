package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;

/**
 * 型の属性セット
 */
public record JigTypeAttributes(JigTypeVisibility jigTypeVisibility,
                                Collection<JigTypeModifier> jigTypeModifiers,
                                Collection<JigAnnotationReference> declarationAnnotationInstances,
                                List<JigTypeParameter> typeParameters) {

    public String typeParametersSimpleName() {
        if (typeParameters.isEmpty()) return "";
        return typeParameters.stream()
                .map(jigTypeParameter -> jigTypeParameter.nameAndBounds())
                .collect(joining(", ", "<", ">"));
    }

    public List<JigAnnotationReference> declarationAnnotationList() {
        return declarationAnnotationInstances.stream()
                .sorted(Comparator.comparing(jigAnnotationData -> jigAnnotationData.id()))
                .toList();
    }

    public boolean declaredAnnotation(TypeId typeId) {
        return declarationAnnotationInstances.stream()
                .anyMatch(jigAnnotationInstance -> jigAnnotationInstance.id().equals(typeId));
    }

    public Collection<? extends TypeId> typeIdSet() {
        // アノテーションのelementの型がまだはいっていない
        // 型パラメタ（の境界型）がまだはいっていない
        return declarationAnnotationInstances.stream()
                .map(JigAnnotationReference::id)
                .collect(toSet());
    }
}

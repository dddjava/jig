package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record JigTypeAttributes(JigTypeVisibility jigTypeVisibility,
                                Collection<JigTypeModifier> jigTypeModifiers,
                                Collection<JigAnnotationReference> declarationAnnotationInstances,
                                List<JigTypeParameter> typeParameters) {

    public static JigTypeAttributes simple() {
        return new JigTypeAttributes(JigTypeVisibility.PUBLIC, List.of(), List.of(), List.of());
    }

    public String typeParametersSimpleName() {
        if (typeParameters.isEmpty()) return "";
        return typeParameters.stream()
                .map(jigTypeParameter -> jigTypeParameter.nameAndBounds())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public List<JigAnnotationReference> declarationAnnotationList() {
        return declarationAnnotationInstances.stream()
                .sorted(Comparator.comparing(jigAnnotationData -> jigAnnotationData.id()))
                .toList();
    }

    public boolean declaredAnnotation(TypeIdentifier typeIdentifier) {
        return declarationAnnotationInstances.stream()
                .anyMatch(jigAnnotationInstance -> jigAnnotationInstance.id().equals(typeIdentifier));
    }

    public Collection<? extends TypeIdentifier> typeIdSet() {
        // アノテーションのelementの型がまだはいっていない
        // 型パラメタ（の境界型）がまだはいっていない
        return declarationAnnotationInstances.stream()
                .map(JigAnnotationReference::id)
                .collect(Collectors.toSet());
    }
}

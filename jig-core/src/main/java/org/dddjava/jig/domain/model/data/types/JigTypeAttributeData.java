package org.dddjava.jig.domain.model.data.types;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record JigTypeAttributeData(JigTypeVisibility jigTypeVisibility,
                                   Collection<JigTypeModifier> jigTypeModifiers,
                                   Collection<JigAnnotationInstance> declarationAnnotationInstances,
                                   List<JigTypeParameter> typeParameters) {

    public static JigTypeAttributeData simple() {
        return new JigTypeAttributeData(JigTypeVisibility.PUBLIC, List.of(), List.of(), List.of());
    }

    public String typeParametersSimpleName() {
        if (typeParameters.isEmpty()) return "";
        return typeParameters.stream()
                .map(jigTypeParameter -> jigTypeParameter.nameAndBounds())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public List<JigAnnotationInstance> declarationAnnotationList() {
        return declarationAnnotationInstances.stream()
                .sorted(Comparator.comparing(jigAnnotationData -> jigAnnotationData.id()))
                .toList();
    }
}

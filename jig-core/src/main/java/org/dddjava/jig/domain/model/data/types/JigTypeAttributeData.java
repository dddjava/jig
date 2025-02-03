package org.dddjava.jig.domain.model.data.types;

import org.dddjava.jig.domain.model.data.classes.type.TypeVisibility;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record JigTypeAttributeData(TypeVisibility typeVisibility,
                                   Collection<JigAnnotationData> declarationAnnotations,
                                   List<JigTypeParameter> typeParameters) {

    public static JigTypeAttributeData simple() {
        return new JigTypeAttributeData(TypeVisibility.PUBLIC, List.of(), List.of());
    }

    public String typeParametersSimpleName() {
        if (typeParameters.isEmpty()) return "";
        return typeParameters.stream()
                .map(jigTypeParameter -> jigTypeParameter.nameAndBounds())
                .collect(Collectors.joining(", ", "<", ">"));
    }

    public List<JigAnnotationData> declarationAnnotationList() {
        return declarationAnnotations.stream()
                .sorted(Comparator.comparing(jigAnnotationData -> jigAnnotationData.id()))
                .toList();
    }
}

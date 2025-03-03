package org.dddjava.jig.domain.model.data.members.fields;

import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Stream;

public record JigFieldAttribute(JigMemberVisibility jigMemberVisibility,
                                Collection<JigAnnotationReference> declarationAnnotations,
                                EnumSet<JigFieldFlag> flags) {
    Stream<TypeIdentifier> allTypeIdentifierStream() {
        return declarationAnnotations.stream().map(jigAnnotationReference -> jigAnnotationReference.id());
    }

    public boolean isDeprecated() {
        return declarationAnnotations.stream()
                .anyMatch(annotation -> annotation.id().equals(TypeIdentifier.from(Deprecated.class)));
    }
}

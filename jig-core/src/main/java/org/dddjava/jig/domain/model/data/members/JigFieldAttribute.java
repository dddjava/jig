package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.EnumSet;
import java.util.stream.Stream;

public record JigFieldAttribute(JigMemberVisibility jigMemberVisibility,
                                Collection<JigAnnotationReference> declarationAnnotations,
                                EnumSet<JigFieldFlag> flags,
                                JigTypeReference typeReference) {
    Stream<TypeIdentifier> allTypeIdentifierStream() {
        return Stream.concat(
                declarationAnnotations.stream().map(jigAnnotationReference -> jigAnnotationReference.id()),
                typeReference.allTypeIentifierStream()
        );
    }
}

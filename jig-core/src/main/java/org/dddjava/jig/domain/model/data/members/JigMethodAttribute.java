package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public record JigMethodAttribute(JigMemberVisibility jigMemberVisibility,
                                 Collection<JigAnnotationReference> declarationAnnotations,
                                 JigTypeReference returnType,
                                 List<JigTypeReference> argumentList,
                                 Collection<JigTypeReference> throwTypes,
                                 EnumSet<JigMethodFlag> flags) {
    public boolean isAbstract() {
        return flags.contains(JigMethodFlag.ABSTRACT);
    }

    public Stream<TypeIdentifier> associatedTypeStream() {
        return Stream.of(
                        declarationAnnotations.stream().flatMap(JigAnnotationReference::allTypeIentifierStream),
                        returnType.allTypeIentifierStream(),
                        argumentList.stream().flatMap(JigTypeReference::allTypeIentifierStream),
                        throwTypes.stream().flatMap(JigTypeReference::allTypeIentifierStream))
                .flatMap(Function.identity());
    }
}

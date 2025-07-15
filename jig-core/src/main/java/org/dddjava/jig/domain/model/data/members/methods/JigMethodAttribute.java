package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

record JigMethodAttribute(JigMemberVisibility jigMemberVisibility,
                          Collection<JigAnnotationReference> declarationAnnotations,
                          JigTypeReference returnType,
                          List<JigTypeReference> argumentList,
                          Collection<JigTypeReference> throwTypes,
                          EnumSet<JigMethodFlag> flags) {

    public Stream<TypeId> associatedTypeStream() {
        return Stream.of(
                        declarationAnnotations.stream().flatMap(JigAnnotationReference::allTypeIentifierStream),
                        returnType.toTypeIdStream(),
                        argumentList.stream().flatMap(JigTypeReference::toTypeIdStream),
                        throwTypes.stream().flatMap(JigTypeReference::toTypeIdStream))
                .flatMap(Function.identity());
    }
}

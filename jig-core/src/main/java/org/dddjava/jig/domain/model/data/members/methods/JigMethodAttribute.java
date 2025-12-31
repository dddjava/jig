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

/**
 * メンバの属性
 *
 * 仮引数型は順番に意味があるので List で扱う。
 */
record JigMethodAttribute(JigMemberVisibility jigMemberVisibility,
                          Collection<JigAnnotationReference> declarationAnnotations,
                          JigTypeReference returnType,
                          List<JigTypeReference> parameterTypeList,
                          Collection<JigTypeReference> throwTypes,
                          EnumSet<JigMethodFlag> flags) {

    public Stream<TypeId> associatedTypeStream() {
        return Stream.of(
                        declarationAnnotations.stream().flatMap(JigAnnotationReference::allTypeIdStream),
                        returnType.toTypeIdStream(),
                        parameterTypeList.stream().flatMap(JigTypeReference::toTypeIdStream),
                        throwTypes.stream().flatMap(JigTypeReference::toTypeIdStream))
                .flatMap(Function.identity());
    }
}

package org.dddjava.jig.domain.model.data.members.methods;

import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * メソッドのヘッダ
 */
public record JigMethodHeader(JigMethodIdentifier id,
                              JigMemberOwnership ownership,
                              JigMethodAttribute jigMethodAttribute) {
    public static JigMethodHeader from(JigMethodIdentifier id,
                                       JigMemberOwnership ownership,
                                       JigMemberVisibility jigMemberVisibility,
                                       Collection<JigAnnotationReference> declarationAnnotations,
                                       JigTypeReference returnType,
                                       List<JigTypeReference> argumentList,
                                       Collection<JigTypeReference> throwTypes,
                                       EnumSet<JigMethodFlag> flags) {
        return new JigMethodHeader(id, ownership,
                new JigMethodAttribute(jigMemberVisibility, declarationAnnotations, returnType, argumentList, throwTypes, flags));
    }

    public String name() {
        return id.name();
    }

    public String nameArgumentsReturnSimpleText() {
        return nameAndArgumentSimpleText() + ':' + jigMethodAttribute.returnType().simpleNameWithGenerics();
    }

    public String nameAndArgumentSimpleText() {
        return "%s(%s)".formatted(
                id().name(),
                jigMethodAttribute.argumentList().stream()
                        .map(JigTypeReference::simpleNameWithGenerics)
                        .collect(Collectors.joining(", ")));
    }

    public boolean isObjectMethod() {
        // とりあえずIDで比較するけど、attributeのFlagで判別できるようにしてていい気はする
        return id.value().endsWith("#equals(java.lang.Object)")
                || id.value().endsWith("#hashCode()")
                || id.value().endsWith("#toString()");
    }
}

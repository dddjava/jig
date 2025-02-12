package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.JigTypeReference;

import java.util.stream.Collectors;

/**
 * メソッドのヘッダ
 */
public record JigMethodHeader(JigMethodIdentifier id,
                              JigMemberOwnership ownership,
                              JigMethodAttribute jigMethodAttribute) {
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

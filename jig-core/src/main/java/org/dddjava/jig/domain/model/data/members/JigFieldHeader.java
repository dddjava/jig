package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.stream.Stream;

/**
 * フィールドのヘッダ
 *
 * フィールドには本体がないためヘッダだけで完全な定義になるが、
 * JigTypeHeaderやJigMethodHeaderに合わせてこの名前にしておく。
 */
public record JigFieldHeader(JigFieldIdentifier id,
                             JigMemberOwnership ownership,
                             JigFieldAttribute jigFieldAttribute) {
    public String simpleText() {
        return jigFieldAttribute.typeReference().simpleName() + ' ' + id.name();
    }

    public Stream<TypeIdentifier> allTypeIdentifierStream() {
        return jigFieldAttribute.allTypeIdentifierStream();
    }
}

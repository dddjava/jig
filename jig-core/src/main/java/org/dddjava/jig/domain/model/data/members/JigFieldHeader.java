package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.JigTypeReference;
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
                             JigTypeReference jigTypeReference,
                             JigFieldAttribute jigFieldAttribute) {
    public String simpleText() {
        return jigTypeReference().simpleName() + ' ' + id.name();
    }

    public Stream<TypeIdentifier> allTypeIdentifierStream() {
        return Stream.concat(jigTypeReference.allTypeIentifierStream(), jigFieldAttribute.allTypeIdentifierStream());
    }

    public String simpleNameWithGenerics() {
        return jigTypeReference().simpleNameWithGenerics() + ' ' + id.name();
    }

    public String name() {
        return id.name();
    }

    public boolean isDeprecated() {
        return jigFieldAttribute.isDeprecated();
    }
}

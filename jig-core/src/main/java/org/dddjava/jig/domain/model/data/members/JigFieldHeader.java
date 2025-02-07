package org.dddjava.jig.domain.model.data.members;

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
}

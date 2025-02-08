package org.dddjava.jig.domain.model.data.members;

/**
 * メソッドのヘッダ
 */
public record JigMethodHeader(JigMethodIdentifier id,
                              JigMemberOwnership ownership,
                              JigMethodAttribute jigMethodAttribute) {
    public Object name() {
        return id.name();
    }
}

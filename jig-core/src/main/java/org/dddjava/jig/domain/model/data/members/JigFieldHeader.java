package org.dddjava.jig.domain.model.data.members;

public record JigFieldHeader(JigFieldIdentifier id,
                             JigMemberOwnership ownership,
                             JigFieldAttribute jigFieldAttribute) {
}

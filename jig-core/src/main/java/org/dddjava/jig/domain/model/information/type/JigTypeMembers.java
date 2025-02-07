package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

public record JigTypeMembers(
        Collection<JigFieldHeader> jigFieldHeaders,
        Collection<JigMethodHeader> jigMethodHeaders,
        // 互換のため
        JigStaticMember jigStaticMember,
        // 互換のため
        JigInstanceMember jigInstanceMember
) {

    public String instanceFieldsSimpleText() {
        return jigFieldHeaders().stream()
                .filter(jigFieldHeader -> jigFieldHeader.ownership() == JigMemberOwnership.INSTANCE)
                .map(jigFieldHeader -> jigFieldHeader.simpleText())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public Set<TypeIdentifier> allTypeIdentifierSet() {
        return jigFieldHeaders.stream()
                .flatMap(JigFieldHeader::allTypeIdentifierStream)
                .collect(Collectors.toSet());
    }
}

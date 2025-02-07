package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record JigTypeMembers(
        Collection<JigFieldHeader> jigFieldHeaders,
        Collection<JigMethodHeader> jigMethodHeaders,
        // 互換のため
        JigStaticMember jigStaticMember,
        // 互換のため
        JigInstanceMember jigInstanceMember
) {

    public String instanceFieldsSimpleText() {
        return instanceJigFieldHeaderStream()
                .map(jigFieldHeader -> jigFieldHeader.simpleText())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private Stream<JigFieldHeader> instanceJigFieldHeaderStream() {
        return jigFieldHeaders().stream()
                .filter(jigFieldHeader -> jigFieldHeader.ownership() == JigMemberOwnership.INSTANCE);
    }

    public Set<TypeIdentifier> allTypeIdentifierSet() {
        return jigFieldHeaders.stream()
                .flatMap(JigFieldHeader::allTypeIdentifierStream)
                .collect(Collectors.toSet());
    }

    public JigFields instanceFields() {
        return new JigFields(instanceJigFieldHeaderStream()
                .map(jigFieldHeader -> JigField.from(jigFieldHeader))
                .toList());
    }
}

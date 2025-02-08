package org.dddjava.jig.domain.model.information.type;

import org.dddjava.jig.domain.model.data.classes.field.JigField;
import org.dddjava.jig.domain.model.data.classes.field.JigFields;
import org.dddjava.jig.domain.model.data.members.JigFieldFlag;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.members.JigMemberOwnership;
import org.dddjava.jig.domain.model.data.members.JigMethodHeader;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.Collection;
import java.util.Optional;
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
        return jigFieldHeaderStream(JigMemberOwnership.INSTANCE)
                .map(jigFieldHeader -> jigFieldHeader.simpleText())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private Stream<JigFieldHeader> jigFieldHeaderStream(JigMemberOwnership jigMemberOwnership) {
        return jigFieldHeaders().stream()
                .filter(jigFieldHeader -> jigFieldHeader.ownership() == jigMemberOwnership);
    }

    public Set<TypeIdentifier> allTypeIdentifierSet() {
        return jigFieldHeaders.stream()
                .flatMap(JigFieldHeader::allTypeIdentifierStream)
                .collect(Collectors.toSet());
    }

    public JigFields instanceFields() {
        return new JigFields(jigFieldHeaderStream(JigMemberOwnership.INSTANCE)
                .map(jigFieldHeader -> JigField.from(jigFieldHeader))
                .toList());
    }

    public Optional<JigFieldHeader> findFieldByName(String name) {
        return jigFieldHeaders.stream()
                .filter(jigFieldHeader -> jigFieldHeader.name().equals(name))
                .findAny();
    }

    public String enumConstantNames() {
        return jigFieldHeaderStream(JigMemberOwnership.CLASS)
                .filter(jigFieldHeader -> jigFieldHeader.jigFieldAttribute().flags().contains(JigFieldFlag.ENUM))
                .map(jigFieldHeader -> jigFieldHeader.name())
                .collect(Collectors.joining(", ", "[", "]"));
    }
}

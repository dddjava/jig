package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.*;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.unit.JigMethodDeclaration;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigFields;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethods;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record JigTypeMembers(Collection<JigFieldHeader> jigFieldHeaders,
                             Collection<JigMethod> jigMethods) {

    public Collection<JigMethodDeclaration> jigMethodDeclarations() {
        return jigMethods.stream().map(jigMethod -> jigMethod.jigMethodDeclaration()).toList();
    }

    public String instanceFieldsSimpleText() {
        return jigFieldHeaderStream(JigMemberOwnership.INSTANCE)
                .map(jigFieldHeader -> jigFieldHeader.simpleText())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String instanceFieldsSimpleTextWithGenerics() {
        List<String> list = jigFieldHeaderStream(JigMemberOwnership.INSTANCE)
                .map(jigFieldHeader -> jigFieldHeader.jigTypeReference().simpleNameWithGenerics())
                .toList();
        return list.size() == 1 ? list.get(0) : list.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    private Stream<JigFieldHeader> jigFieldHeaderStream(JigMemberOwnership jigMemberOwnership) {
        return jigFieldHeaders().stream()
                .filter(jigFieldHeader -> jigFieldHeader.ownership() == jigMemberOwnership);
    }

    public Set<TypeIdentifier> allTypeIdentifierSet() {
        return Stream.concat(
                jigFieldHeaders.stream().flatMap(JigFieldHeader::allTypeIdentifierStream),
                jigMethods.stream().flatMap(jigMethod -> jigMethod.jigMethodDeclaration().associatedTypes().stream())
        ).collect(Collectors.toSet());
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

    public List<String> enumConstantNames() {
        return jigFieldHeaderStream(JigMemberOwnership.CLASS)
                .filter(jigFieldHeader -> jigFieldHeader.jigFieldAttribute().flags().contains(JigFieldFlag.ENUM))
                // TODO enumの順でソートしないと狂う可能性がある
                .map(jigFieldHeader -> jigFieldHeader.name())
                .toList();
    }

    public JigMethods instanceMethods() {
        return new JigMethods(jigMethods.stream()
                .filter(jigMethod -> {
                    JigMethodHeader header = jigMethod.jigMethodDeclaration().header();
                    return header.ownership() == JigMemberOwnership.INSTANCE
                            // コンストラクタを除く
                            && !header.jigMethodAttribute().flags().contains(JigMethodFlag.INITIALIZER);
                })
                .toList());
    }

    public JigMethods staticMethods() {
        return new JigMethods(jigMethods.stream().filter(jigMethod -> jigMethod.jigMethodDeclaration().header().ownership() == JigMemberOwnership.CLASS).toList());
    }
}

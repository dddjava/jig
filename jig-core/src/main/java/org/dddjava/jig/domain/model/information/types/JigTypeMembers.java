package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldFlag;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 型のメンバ一式　
 * おもにフィールドおよびメソッド。コンストラクタやイニシャライザも入る。
 */
public record JigTypeMembers(Collection<JigField> staticFields, Collection<JigField> instanceFields,
                             Collection<JigMethod> initializers,
                             Collection<JigMethod> staticMethods, Collection<JigMethod> instanceMethods) {

    public Collection<JigMethodDeclaration> jigMethodDeclarations() {
        return Stream.concat(staticMethods.stream(), instanceMethods.stream())
                .map(jigMethod -> jigMethod.jigMethodDeclaration()).toList();
    }

    public String instanceFieldsSimpleText() {
        return instanceFields.stream()
                .map(jigField -> jigField.jigFieldHeader().simpleText())
                .collect(Collectors.joining(", ", "[", "]"));
    }

    public String instanceFieldsSimpleTextWithGenerics() {
        List<String> list = instanceFields.stream()
                .map(jigField -> jigField.jigTypeReference().simpleNameWithGenerics())
                .toList();
        return list.size() == 1 ? list.get(0) : list.stream().collect(Collectors.joining(", ", "[", "]"));
    }

    public Set<TypeIdentifier> allTypeIdentifierSet() {
        return Stream.concat(
                allJigFieldStream().flatMap(jigFields -> jigFields.jigFieldHeader().allTypeIdentifierStream()),
                allJigMethodStream().flatMap(jigMethod -> jigMethod.jigMethodDeclaration().associatedTypes().stream())
        ).collect(Collectors.toSet());
    }

    public Optional<JigField> findFieldByName(String name) {
        return allJigFieldStream()
                .filter(jigField -> jigField.nameText().equals(name))
                .findAny();
    }

    public List<String> enumConstantNames() {
        return staticFields.stream()
                .map(JigField::jigFieldHeader)
                .filter(jigFieldHeader -> jigFieldHeader.jigFieldAttribute().flags().contains(JigFieldFlag.ENUM))
                // TODO enumの順でソートしないと狂う可能性がある
                .map(jigFieldHeader -> jigFieldHeader.name())
                .toList();
    }

    public Stream<JigMethod> allJigMethodStream() {
        return Stream.of(initializers, staticMethods, instanceMethods).flatMap(Collection::stream);
    }

    public Stream<JigField> allJigFieldStream() {
        return Stream.of(staticFields, instanceFields).flatMap(Collection::stream);
    }
}

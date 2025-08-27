package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigField;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethodDeclaration;

import java.util.Collection;
import java.util.Optional;
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

    public Stream<TypeId> toTypeIdStream() {
        return Stream.concat(
                allJigFieldStream().flatMap(jigFields -> jigFields.jigFieldHeader().toTypeIdStream()),
                allJigMethodStream().flatMap(jigMethod -> jigMethod.jigMethodDeclaration().associatedTypes().stream())
        );
    }

    public Optional<JigField> findFieldByName(String name) {
        return allJigFieldStream()
                .filter(jigField -> jigField.nameText().equals(name))
                .findAny();
    }

    public Stream<JigField> enumConstantStream() {
        // TODO enumの順でソートしないと異なる順番の可能性がある
        return staticFields.stream()
                .filter(jigField -> jigField.jigFieldHeader().isEnumConstant());
    }

    public Stream<JigMethod> allJigMethodStream() {
        return Stream.of(initializers, staticMethods, instanceMethods).flatMap(Collection::stream);
    }

    public Stream<JigField> allJigFieldStream() {
        return Stream.of(staticFields, instanceFields).flatMap(Collection::stream);
    }
}

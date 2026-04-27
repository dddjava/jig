package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.*;
import org.dddjava.jig.domain.model.information.members.JigFields;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.JigMethods;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * JIGが識別する型
 */
public record JigType(JigTypeHeader jigTypeHeader, JigTypeMembers jigTypeMembers) {

    public TypeId id() {
        return jigTypeHeader.id();
    }

    public PackageId packageId() {
        return id().packageId();
    }

    public String fqn() {
        return jigTypeHeader.fqn();
    }

    public JigTypeVisibility visibility() {
        return jigTypeHeader.jigTypeAttributes().jigTypeVisibility();
    }

    public boolean hasAnnotation(TypeId typeId) {
        return jigTypeHeader.jigTypeAttributes().declaredAnnotation(typeId);
    }

    public boolean isDeprecated() {
        return hasAnnotation(TypeId.DEPRECATED_ANNOTATION);
    }

    public Optional<String> annotationValueOf(TypeId typeId, String... elementNames) {
        return jigTypeHeader.jigTypeAttributes().declarationAnnotationInstances().stream()
                .filter(annotation -> annotation.id().equals(typeId))
                .flatMap(annotation -> annotation.elements().stream())
                .filter(element -> element.matchName(elementNames))
                .map(element -> element.valueAsString())
                .findAny();
    }

    public boolean isEnumDeclaration() {
        return jigTypeHeader.javaTypeDeclarationKind() == JavaTypeDeclarationKind.ENUM;
    }

    public boolean isPolymorphicEnumDeclaration() {
        return isEnumDeclaration()
                && jigTypeHeader.jigTypeAttributes().jigTypeModifiers().contains(JigTypeModifier.ABSTRACT);
    }

    public JigTypeValueKind toValueKind() {
        return JigTypeValueKind.from(this);
    }

    public TypeCategory typeCategory() {
        return TypeCategory.from(this);
    }

    public Stream<JigMethod> allJigMethodStream() {
        return jigTypeMembers.allJigMethodStream();
    }

    public JigFields instanceJigFields() {
        return new JigFields(jigTypeMembers.instanceFields());
    }

    public boolean hasInstanceField() {
        return !instanceJigFields().isEmpty();
    }

    public JigMethods instanceJigMethods() {
        return new JigMethods(jigTypeMembers.instanceMethods());
    }

    public Stream<JigMethod> instanceJigMethodStream() {
        return instanceJigMethods().stream();
    }

    public boolean hasInstanceMethod() {
        return !instanceJigMethods().isEmpty();
    }

    public JigMethods staticJigMethods() {
        return new JigMethods(jigTypeMembers.staticMethods());
    }

    public boolean isCompilerGenerated() {
        return jigTypeHeader.isCompilerGenerated();
    }

    public boolean isService() {
        return typeCategory() == TypeCategory.InboundPort;
    }

    public boolean isApplicationComponent() {
        return typeCategory().isApplicationComponent();
    }
}

package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * バイトコード
 */
public class ByteCode {


    final TypeIdentifier typeIdentifier;
    final TypeIdentifier parentTypeIdentifier;
    final boolean canExtend;

    public TypeIdentifiers interfaceTypeIdentifiers;
    final List<TypeAnnotation> typeAnnotations = new ArrayList<>();
    final List<StaticFieldDeclaration> staticFieldDeclarations = new ArrayList<>();

    final List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
    final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    final List<MethodByteCode> instanceMethodByteCodes = new ArrayList<>();
    final List<MethodByteCode> staticMethodByteCodes = new ArrayList<>();
    final List<MethodByteCode> constructorByteCodes = new ArrayList<>();

    final Set<TypeIdentifier> useTypes = new HashSet<>();

    public ByteCode(TypeIdentifier typeIdentifier,
                    TypeIdentifier parentTypeIdentifier,
                    TypeIdentifiers interfaceTypeIdentifiers,
                    List<TypeIdentifier> useTypes,
                    boolean canExtend) {
        this.typeIdentifier = typeIdentifier;
        this.parentTypeIdentifier = parentTypeIdentifier;
        this.interfaceTypeIdentifiers = interfaceTypeIdentifiers;
        this.canExtend = canExtend;

        this.useTypes.addAll(useTypes);
        this.useTypes.add(parentTypeIdentifier);
        this.useTypes.addAll(interfaceTypeIdentifiers.list());
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public boolean canExtend() {
        return canExtend;
    }

    public boolean isEnum() {
        return parentTypeIdentifier.equals(new TypeIdentifier(Enum.class));
    }

    public boolean hasInstanceMethod() {
        return !instanceMethodByteCodes().isEmpty();
    }

    public boolean hasField() {
        return !fieldDeclarations.isEmpty();
    }

    public boolean hasAnnotation(String annotation) {
        TypeIdentifier annotationType = new TypeIdentifier(annotation);
        return typeAnnotations.stream().anyMatch(typeAnnotation -> typeAnnotation.typeIs(annotationType));
    }

    public FieldDeclarations fieldDeclarations() {
        return new FieldDeclarations(fieldDeclarations);
    }

    public StaticFieldDeclarations staticFieldDeclarations() {
        return new StaticFieldDeclarations(staticFieldDeclarations);
    }

    public TypeIdentifiers useTypes() {
        for (MethodByteCode methodByteCode : methodByteCodes()) {
            useTypes.addAll(methodByteCode.useTypes());
        }

        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<MethodByteCode> instanceMethodByteCodes() {
        return instanceMethodByteCodes;
    }

    public List<TypeAnnotation> typeAnnotations() {
        return typeAnnotations;
    }

    public List<FieldAnnotation> annotatedFields() {
        return fieldAnnotations;
    }

    public void registerTypeAnnotation(TypeAnnotation typeAnnotation) {
        typeAnnotations.add(typeAnnotation);
        useTypes.add(typeAnnotation.type());
    }

    public void registerField(FieldDeclaration field) {
        fieldDeclarations.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void registerStaticField(StaticFieldDeclaration field) {
        staticFieldDeclarations.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void registerUseType(TypeIdentifier typeIdentifier) {
        useTypes.add(typeIdentifier);
    }

    public void registerFieldAnnotation(FieldAnnotation fieldAnnotation) {
        fieldAnnotations.add(fieldAnnotation);
    }

    public void registerInstanceMethodByteCodes(MethodByteCode methodByteCode) {
        instanceMethodByteCodes.add(methodByteCode);
    }

    public void registerStaticMethodByteCodes(MethodByteCode methodByteCode) {
        staticMethodByteCodes.add(methodByteCode);
    }

    public void registerConstructorByteCodes(MethodByteCode methodByteCode) {
        constructorByteCodes.add(methodByteCode);
    }

    public List<MethodByteCode> methodByteCodes() {
        ArrayList<MethodByteCode> list = new ArrayList<>();
        list.addAll(instanceMethodByteCodes);
        list.addAll(staticMethodByteCodes);
        list.addAll(constructorByteCodes);
        return list;
    }
}

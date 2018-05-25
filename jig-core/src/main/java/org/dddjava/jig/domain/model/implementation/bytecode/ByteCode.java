package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedField;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotatedType;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * バイトコード
 */
public class ByteCode {

    final ByteCodeAnalyzeContext byteCodeAnalyzeContext;

    final TypeIdentifier typeIdentifier;
    final TypeIdentifier parentTypeIdentifier;
    final boolean canExtend;

    public TypeIdentifiers interfaceTypeIdentifiers;
    final List<AnnotatedType> annotatedTypes = new ArrayList<>();
    final List<FieldDeclaration> staticFieldDeclarations = new ArrayList<>();

    final List<AnnotatedField> annotatedFields = new ArrayList<>();
    final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    final List<MethodImplementation> instanceMethodImplementations = new ArrayList<>();
    final List<MethodImplementation> staticMethodImplementations = new ArrayList<>();
    final List<MethodImplementation> constructorSpecifications = new ArrayList<>();

    final Set<TypeIdentifier> useTypes = new HashSet<>();

    public ByteCode(ByteCodeAnalyzeContext byteCodeAnalyzeContext,
                    TypeIdentifier typeIdentifier,
                    TypeIdentifier parentTypeIdentifier,
                    TypeIdentifiers interfaceTypeIdentifiers,
                    List<TypeIdentifier> useTypes,
                    boolean canExtend) {
        this.byteCodeAnalyzeContext = byteCodeAnalyzeContext;
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
        return !instanceMethodSpecifications().isEmpty();
    }

    public boolean hasField() {
        return !fieldDeclarations.isEmpty();
    }

    public boolean hasAnnotation(String annotation) {
        TypeIdentifier annotationType = new TypeIdentifier(annotation);
        return annotatedTypes.stream().anyMatch(annotatedType -> annotatedType.typeIs(annotationType));
    }

    public FieldDeclarations fieldDeclarations() {
        return new FieldDeclarations(fieldDeclarations);
    }

    public FieldDeclarations staticFieldDeclarations() {
        return new FieldDeclarations(staticFieldDeclarations);
    }

    public boolean isModel() {
        return byteCodeAnalyzeContext.isModel(this);
    }

    public boolean isRepository() {
        return byteCodeAnalyzeContext.isRepository(this);
    }

    public TypeIdentifiers useTypes() {
        for (MethodImplementation methodImplementation : instanceMethodImplementations) {
            useTypes.addAll(methodImplementation.useTypes());
        }
        for (MethodImplementation methodImplementation : staticMethodImplementations) {
            useTypes.addAll(methodImplementation.useTypes());
        }
        for (MethodImplementation methodImplementation : constructorSpecifications) {
            useTypes.addAll(methodImplementation.useTypes());
        }

        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<MethodImplementation> instanceMethodSpecifications() {
        return instanceMethodImplementations;
    }

    public void registerTypeAnnotation(AnnotatedType annotatedType) {
        annotatedTypes.add(annotatedType);
        useTypes.add(annotatedType.type());
    }

    public void registerField(FieldDeclaration field) {
        fieldDeclarations.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void registerStaticField(FieldDeclaration field) {
        staticFieldDeclarations.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void registerUseType(TypeIdentifier typeIdentifier) {
        useTypes.add(typeIdentifier);
    }

    public void registerFieldAnnotation(AnnotatedField annotatedField) {
        annotatedFields.add(annotatedField);
    }

    public AnnotatedType newAnnotationDeclaration(TypeIdentifier annotationTypeIdentifier) {
        return new AnnotatedType(typeIdentifier, annotationTypeIdentifier);
    }

    public FieldDeclaration newFieldDeclaration(String name, TypeIdentifier fieldTypeIdentifier) {
        return new FieldDeclaration(typeIdentifier, name, fieldTypeIdentifier);
    }

    public List<AnnotatedField> fieldAnnotationDeclarations() {
        return annotatedFields;
    }

    public void registerInstanceMethodSpecification(MethodImplementation methodImplementation) {
        instanceMethodImplementations.add(methodImplementation);
    }

    public void registerStaticMethodSpecification(MethodImplementation methodImplementation) {
        staticMethodImplementations.add(methodImplementation);
    }

    public void registerConstructorSpecification(MethodImplementation methodImplementation) {
        constructorSpecifications.add(methodImplementation);
    }
}

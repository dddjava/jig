package jig.domain.model.specification;

import jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import jig.domain.model.declaration.annotation.TypeAnnotationDeclaration;
import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Specification {

    final TypeIdentifier typeIdentifier;
    final TypeIdentifier parentTypeIdentifier;
    final boolean canExtend;

    public TypeIdentifiers interfaceTypeIdentifiers;
    final List<TypeAnnotationDeclaration> typeAnnotationDeclarations = new ArrayList<>();
    final List<FieldDeclaration> staticFieldDeclarations = new ArrayList<>();

    final List<FieldAnnotationDeclaration> fieldAnnotationDeclarations = new ArrayList<>();
    final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    final List<MethodSpecification> instanceMethodSpecifications = new ArrayList<>();
    final List<MethodSpecification> staticMethodSpecifications = new ArrayList<>();
    final List<MethodSpecification> constructorSpecifications = new ArrayList<>();

    final Set<TypeIdentifier> useTypes = new HashSet<>();

    public Specification(TypeIdentifier typeIdentifier,
                         TypeIdentifier parentTypeIdentifier,
                         TypeIdentifiers interfaceTypeIdentifiers,
                         List<TypeIdentifier> useTypes, boolean canExtend) {
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

    public boolean hasOnlyOneFieldAndFieldTypeIs(Class<?> clz) {
        if (isEnum()) return false;
        if (fieldDeclarations.size() != 1) return false;
        return fieldDeclarations.get(0).typeIdentifier().fullQualifiedName().equals(clz.getName());
    }

    public boolean hasTwoFieldsAndFieldTypeAre(Class<?> clz) {
        if (isEnum()) return false;
        if (fieldDeclarations.size() != 2) return false;
        TypeIdentifier field1 = fieldDeclarations.get(0).typeIdentifier();
        TypeIdentifier field2 = fieldDeclarations.get(1).typeIdentifier();
        return (field1.equals(field2) && field1.fullQualifiedName().equals(clz.getName()));
    }

    public boolean hasInstanceMethod() {
        return !instanceMethodSpecifications.isEmpty();
    }

    public boolean hasField() {
        return !fieldDeclarations.isEmpty();
    }

    public boolean hasAnnotation(String annotation) {
        TypeIdentifier annotationType = new TypeIdentifier(annotation);
        return typeAnnotationDeclarations.stream().anyMatch(typeAnnotationDeclaration -> typeAnnotationDeclaration.typeIs(annotationType));
    }

    public FieldDeclarations fieldIdentifiers() {
        return new FieldDeclarations(fieldDeclarations);
    }

    public FieldDeclarations staticFieldDeclarations() {
        return new FieldDeclarations(staticFieldDeclarations);
    }

    public boolean isModel() {
        // TODO 外部化
        return typeIdentifier.fullQualifiedName().contains(".domain.model.");
    }

    public boolean isRepository() {
        return typeIdentifier.fullQualifiedName().endsWith("Repository");
    }

    public TypeIdentifiers useTypes() {
        for (MethodSpecification methodSpecification : instanceMethodSpecifications) {
            useTypes.addAll(methodSpecification.useTypes());
        }
        for (MethodSpecification methodSpecification : staticMethodSpecifications) {
            useTypes.addAll(methodSpecification.useTypes());
        }
        for (MethodSpecification methodSpecification : constructorSpecifications) {
            useTypes.addAll(methodSpecification.useTypes());
        }

        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<MethodSpecification> instanceMethodSpecifications() {
        return instanceMethodSpecifications;
    }

    public void registerTypeAnnotation(TypeAnnotationDeclaration typeAnnotationDeclaration) {
        typeAnnotationDeclarations.add(typeAnnotationDeclaration);
        useTypes.add(typeAnnotationDeclaration.type());
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

    public void registerFieldAnnotation(FieldAnnotationDeclaration fieldAnnotationDeclaration) {
        fieldAnnotationDeclarations.add(fieldAnnotationDeclaration);
    }

    public TypeAnnotationDeclaration newAnnotationDeclaration(TypeIdentifier annotationTypeIdentifier) {
        return new TypeAnnotationDeclaration(typeIdentifier, annotationTypeIdentifier);
    }

    public FieldDeclaration newFieldDeclaration(String name, TypeIdentifier fieldTypeIdentifier) {
        return new FieldDeclaration(typeIdentifier, name, fieldTypeIdentifier);
    }

    public MethodDeclaration newMethodDeclaration(MethodSignature methodSignature) {
        return new MethodDeclaration(typeIdentifier, methodSignature);
    }

    public List<FieldAnnotationDeclaration> fieldAnnotationDeclarations() {
        return fieldAnnotationDeclarations;
    }

    public void registerInstanceMethodSpecification(MethodSpecification methodSpecification) {
        instanceMethodSpecifications.add(methodSpecification);
    }

    public void registerStaticMethodSpecification(MethodSpecification methodSpecification) {
        staticMethodSpecifications.add(methodSpecification);
    }

    public void registerConstructorSpecification(MethodSpecification methodSpecification) {
        constructorSpecifications.add(methodSpecification);
    }
}

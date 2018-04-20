package jig.domain.model.specification;

import jig.domain.model.declaration.annotation.AnnotationDeclaration;
import jig.domain.model.declaration.annotation.FieldAnnotationDeclaration;
import jig.domain.model.declaration.field.FieldDeclaration;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Specification {

    public TypeIdentifier typeIdentifier;

    TypeIdentifier parentTypeIdentifier;
    private final boolean canExtend;

    public TypeIdentifiers interfaceTypeIdentifiers;
    List<AnnotationDeclaration> annotations = new ArrayList<>();
    List<FieldDeclaration> constantIdentifiers = new ArrayList<>();

    private List<FieldAnnotationDeclaration> fieldAnnotationDeclarations = new ArrayList<>();
    List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    List<MethodSpecification> methodSpecifications = new ArrayList<>();

    private Set<TypeIdentifier> useTypes = new HashSet<>();

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
        return methodSpecifications.stream().anyMatch(MethodSpecification::isInstanceMethod);
    }

    public boolean hasField() {
        return !fieldDeclarations.isEmpty();
    }

    public boolean hasAnnotation(String annotation) {
        TypeIdentifier annotationType = new TypeIdentifier(annotation);
        return annotations.stream().anyMatch(annotationDeclaration -> annotationDeclaration.typeIs(annotationType));
    }

    public FieldDeclarations fieldIdentifiers() {
        return new FieldDeclarations(fieldDeclarations);
    }

    public FieldDeclarations constantIdentifiers() {
        return new FieldDeclarations(constantIdentifiers);
    }

    public boolean isModel() {
        // TODO 外部化
        return typeIdentifier.fullQualifiedName().contains(".domain.model.");
    }

    public TypeIdentifiers useTypes() {
        for (MethodSpecification methodSpecification : methodSpecifications) {
            useTypes.addAll(methodSpecification.useTypes());
        }
        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<MethodSpecification> instanceMethodSpecifications() {
        return methodSpecifications.stream().filter(MethodSpecification::isInstanceMethod).collect(Collectors.toList());
    }

    public void addAnnotation(AnnotationDeclaration annotationDeclaration) {
        annotations.add(annotationDeclaration);
        useTypes.add(annotationDeclaration.type());
    }

    public void add(MethodSpecification methodSpecification) {
        methodSpecifications.add(methodSpecification);
    }

    public void add(FieldDeclaration field) {
        fieldDeclarations.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void addConstant(FieldDeclaration field) {
        constantIdentifiers.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void addUseType(TypeIdentifier typeIdentifier) {
        useTypes.add(typeIdentifier);
    }

    public void addFieldAnnotation(FieldAnnotationDeclaration fieldAnnotationDeclaration) {
        fieldAnnotationDeclarations.add(fieldAnnotationDeclaration);
    }
}

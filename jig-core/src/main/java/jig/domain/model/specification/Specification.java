package jig.domain.model.specification;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.field.FieldIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Specification {

    public TypeIdentifier typeIdentifier;

    TypeIdentifier parentTypeIdentifier;
    int classAccess;
    public TypeIdentifiers interfaceTypeIdentifiers;
    List<TypeIdentifier> annotationDescriptors;
    List<MethodSpecification> methodSpecifications;
    List<FieldIdentifier> fieldIdentifiers;
    List<FieldIdentifier> constantIdentifiers;
    private Set<TypeIdentifier> useTypes = new HashSet<>();

    public Specification(TypeIdentifier typeIdentifier,
                         TypeIdentifier parentTypeIdentifier,
                         int classAccess,
                         TypeIdentifiers interfaceTypeIdentifiers,
                         List<TypeIdentifier> useTypes) {
        this.typeIdentifier = typeIdentifier;
        this.parentTypeIdentifier = parentTypeIdentifier;
        this.classAccess = classAccess;
        this.interfaceTypeIdentifiers = interfaceTypeIdentifiers;
        this.annotationDescriptors = new ArrayList<>();
        this.methodSpecifications = new ArrayList<>();
        this.fieldIdentifiers = new ArrayList<>();
        this.constantIdentifiers = new ArrayList<>();

        this.useTypes.addAll(useTypes);
        this.useTypes.add(parentTypeIdentifier);
        this.useTypes.addAll(interfaceTypeIdentifiers.list());
    }

    public boolean canExtend() {
        return (classAccess & Opcodes.ACC_FINAL) == 0;
    }

    public boolean isEnum() {
        return parentTypeIdentifier.equals(new TypeIdentifier(Enum.class));
    }

    public boolean hasOnlyOneFieldAndFieldTypeIs(Class<?> clz) {
        if (isEnum()) return false;
        if (fieldIdentifiers.size() != 1) return false;
        return fieldIdentifiers.get(0).typeIdentifier().fullQualifiedName().equals(clz.getName());
    }

    public boolean hasTwoFieldsAndFieldTypeAre(Class<?> clz) {
        if (isEnum()) return false;
        if (fieldIdentifiers.size() != 2) return false;
        TypeIdentifier field1 = fieldIdentifiers.get(0).typeIdentifier();
        TypeIdentifier field2 = fieldIdentifiers.get(1).typeIdentifier();
        return (field1.equals(field2) && field1.fullQualifiedName().equals(clz.getName()));
    }

    public boolean hasInstanceMethod() {
        return methodSpecifications.stream().anyMatch(MethodSpecification::isInstanceMethod);
    }

    public boolean hasField() {
        return !fieldIdentifiers.isEmpty();
    }

    public boolean hasAnnotation(String annotation) {
        return annotationDescriptors.stream().anyMatch(annotationDescriptor -> annotationDescriptor.fullQualifiedName().equals(annotation));
    }

    public FieldIdentifiers fieldIdentifiers() {
        return new FieldIdentifiers(fieldIdentifiers);
    }

    public FieldIdentifiers constantIdentifiers() {
        return new FieldIdentifiers(constantIdentifiers);
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

    public void addAnnotation(TypeIdentifier annotationType) {
        annotationDescriptors.add(annotationType);
        useTypes.add(annotationType);
    }

    public void add(MethodSpecification methodSpecification) {
        methodSpecifications.add(methodSpecification);
    }

    public void add(FieldIdentifier field) {
        fieldIdentifiers.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void addConstant(FieldIdentifier field) {
        constantIdentifiers.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void addUseType(TypeIdentifier typeIdentifier) {
        useTypes.add(typeIdentifier);
    }
}

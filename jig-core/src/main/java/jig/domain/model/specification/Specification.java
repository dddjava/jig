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

public class Specification {

    public TypeIdentifier typeIdentifier;
    TypeIdentifier parentTypeIdentifier;
    int classAccess;
    public TypeIdentifiers interfaceTypeIdentifiers;
    List<ClassDescriptor> annotationDescriptors;
    public List<MethodSpecification> methodSpecifications;
    List<FieldIdentifier> fieldIdentifiers;
    List<FieldIdentifier> constantIdentifiers;
    private Set<TypeIdentifier> useTypes = new HashSet<>();

    public Specification(TypeIdentifier typeIdentifier,
                         TypeIdentifier parentTypeIdentifier,
                         int classAccess,
                         TypeIdentifiers interfaceTypeIdentifiers,
                         List<ClassDescriptor> annotationDescriptors,
                         List<MethodSpecification> methodSpecifications,
                         List<FieldIdentifier> fieldIdentifiers,
                         List<FieldIdentifier> constantIdentifiers) {
        this.typeIdentifier = typeIdentifier;
        this.parentTypeIdentifier = parentTypeIdentifier;
        this.classAccess = classAccess;
        this.interfaceTypeIdentifiers = interfaceTypeIdentifiers;
        this.annotationDescriptors = annotationDescriptors;
        this.methodSpecifications = methodSpecifications;
        this.fieldIdentifiers = fieldIdentifiers;
        this.constantIdentifiers = constantIdentifiers;
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
        return !methodSpecifications.isEmpty();
    }

    public boolean hasField() {
        return !fieldIdentifiers.isEmpty();
    }

    public boolean hasAnnotation(String annotation) {
        return annotationDescriptors.stream().anyMatch(annotationDescriptor -> annotationDescriptor.value.equals(annotation));
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

    public void addUseType(TypeIdentifier identifier) {
        useTypes.add(identifier);
    }
}

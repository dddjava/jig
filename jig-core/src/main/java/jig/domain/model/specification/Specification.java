package jig.domain.model.specification;

import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.TypeIdentifiers;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class Specification {

    public TypeIdentifier typeIdentifier;
    TypeIdentifier parentTypeIdentifier;
    int classAccess;
    public TypeIdentifiers interfaceTypeIdentifiers;
    List<ClassDescriptor> annotationDescriptors;
    public List<MethodSpecification> methodSpecifications;
    List<ClassDescriptor> fieldDescriptors;

    public Specification(TypeIdentifier typeIdentifier,
                         TypeIdentifier parentTypeIdentifier,
                         int classAccess,
                         TypeIdentifiers interfaceTypeIdentifiers,
                         List<ClassDescriptor> annotationDescriptors,
                         List<MethodSpecification> methodSpecifications,
                         List<ClassDescriptor> fieldDescriptors) {
        this.typeIdentifier = typeIdentifier;
        this.parentTypeIdentifier = parentTypeIdentifier;
        this.classAccess = classAccess;
        this.interfaceTypeIdentifiers = interfaceTypeIdentifiers;
        this.annotationDescriptors = annotationDescriptors;
        this.methodSpecifications = methodSpecifications;
        this.fieldDescriptors = fieldDescriptors;
    }

    public boolean canExtend() {
        return (classAccess & Opcodes.ACC_FINAL) == 0;
    }

    public boolean isEnum() {
        return parentTypeIdentifier.equals(new TypeIdentifier(Enum.class));
    }

    public boolean hasOnlyOneFieldAndFieldTypeIs(String classDescriptor) {
        if (isEnum()) return false;
        if (fieldDescriptors.size() != 1) return false;
        return fieldDescriptors.get(0).value.equals(classDescriptor);
    }

    public boolean hasTwoFieldsAndFieldTypeAre(String classDescriptor) {
        if (isEnum()) return false;
        if (fieldDescriptors.size() != 2) return false;
        TypeIdentifier field1 = fieldDescriptors.get(0).toTypeIdentifier();
        TypeIdentifier field2 = fieldDescriptors.get(1).toTypeIdentifier();
        return (field1.equals(field2) && field1.equals(new ClassDescriptor(classDescriptor).toTypeIdentifier()));
    }

    public boolean hasMethod() {
        return !methodSpecifications.isEmpty();
    }

    public boolean hasField() {
        return !fieldDescriptors.isEmpty();
    }

    public boolean hasAnnotation(String annotation) {
        return annotationDescriptors.stream().anyMatch(annotationDescriptor -> annotationDescriptor.value.equals(annotation));
    }

    public TypeIdentifiers fieldTypeIdentifiers() {
        return fieldDescriptors.stream().map(ClassDescriptor::toTypeIdentifier).collect(TypeIdentifiers.collector());
    }

    public boolean isModel() {
        // TODO 外部化
        return typeIdentifier.fullQualifiedName().contains(".domain.model.");
    }
}

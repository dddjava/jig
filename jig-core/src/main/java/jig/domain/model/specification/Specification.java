package jig.domain.model.specification;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class Specification {

    public Identifier identifier;
    public Identifier parentIdentifier;
    public int classAccess;
    public Identifiers interfaceIdentifiers;
    public List<ClassDescriptor> annotationDescriptors;
    public List<MethodSpecification> methodSpecifications;
    public List<ClassDescriptor> fieldDescriptors;

    public Specification(Identifier identifier,
                         Identifier parentIdentifier,
                         int classAccess,
                         Identifiers interfaceIdentifiers,
                         List<ClassDescriptor> annotationDescriptors,
                         List<MethodSpecification> methodSpecifications,
                         List<ClassDescriptor> fieldDescriptors) {
        this.identifier = identifier;
        this.parentIdentifier = parentIdentifier;
        this.classAccess = classAccess;
        this.interfaceIdentifiers = interfaceIdentifiers;
        this.annotationDescriptors = annotationDescriptors;
        this.methodSpecifications = methodSpecifications;
        this.fieldDescriptors = fieldDescriptors;
    }

    public boolean canExtend() {
        return (classAccess & Opcodes.ACC_FINAL) == 0;
    }

    public boolean isEnum() {
        return parentIdentifier.equals(new Identifier(Enum.class));
    }

    public boolean hasOnlyOneFieldAndFieldTypeIs(String classDescriptor) {
        if (isEnum()) return false;
        if (fieldDescriptors.size() != 1) return false;
        return fieldDescriptors.get(0).value.equals(classDescriptor);
    }

    public boolean hasTwoFieldsAndFieldTypeAre(String classDescriptor) {
        if (isEnum()) return false;
        if (fieldDescriptors.size() != 2) return false;
        Identifier field1 = fieldDescriptors.get(0).toTypeIdentifier();
        Identifier field2 = fieldDescriptors.get(1).toTypeIdentifier();
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

    public Identifiers fieldTypeIdentifiers() {
        return fieldDescriptors.stream().map(ClassDescriptor::toTypeIdentifier).collect(Identifiers.collector());
    }

    public boolean isModel() {
        // TODO 外部化
        return identifier.value().contains(".domain.model.");
    }
}

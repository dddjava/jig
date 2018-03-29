package jig.domain.model.specification;

import jig.domain.model.identifier.field.FieldIdentifier;
import jig.domain.model.identifier.field.FieldIdentifiers;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import org.objectweb.asm.Opcodes;

import java.util.List;

public class Specification {

    public TypeIdentifier typeIdentifier;
    TypeIdentifier parentTypeIdentifier;
    int classAccess;
    public TypeIdentifiers interfaceTypeIdentifiers;
    List<ClassDescriptor> annotationDescriptors;
    public List<MethodSpecification> methodSpecifications;
    List<FieldIdentifier> fieldIdentifiers;

    public Specification(TypeIdentifier typeIdentifier,
                         TypeIdentifier parentTypeIdentifier,
                         int classAccess,
                         TypeIdentifiers interfaceTypeIdentifiers,
                         List<ClassDescriptor> annotationDescriptors,
                         List<MethodSpecification> methodSpecifications,
                         List<FieldIdentifier> fieldIdentifiers) {
        this.typeIdentifier = typeIdentifier;
        this.parentTypeIdentifier = parentTypeIdentifier;
        this.classAccess = classAccess;
        this.interfaceTypeIdentifiers = interfaceTypeIdentifiers;
        this.annotationDescriptors = annotationDescriptors;
        this.methodSpecifications = methodSpecifications;
        this.fieldIdentifiers = fieldIdentifiers;
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

    public boolean hasMethod() {
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

    public boolean isModel() {
        // TODO 外部化
        return typeIdentifier.fullQualifiedName().contains(".domain.model.");
    }
}

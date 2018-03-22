package jig.domain.model.specification;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;

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
        String field1 = fieldDescriptors.get(0).toString();
        String field2 = fieldDescriptors.get(1).toString();
        return (field1.equals(field2) && field1.equals(classDescriptor));
    }

    public boolean hasMethod() {
        return !methodSpecifications.isEmpty();
    }

    public boolean hasField() {
        return !fieldDescriptors.isEmpty();
    }
}

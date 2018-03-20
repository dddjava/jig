package jig.domain.model.specification;

import jig.domain.model.thing.Identifier;
import jig.domain.model.thing.Identifiers;

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
}

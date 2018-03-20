package jig.domain.model.specification;

import jig.domain.model.thing.Identifier;
import jig.domain.model.thing.Names;

import java.util.List;

public class Specification {

    public Identifier identifier;
    public Identifier parentIdentifier;
    public int classAccess;
    public Names interfaceNames;
    public List<ClassDescriptor> annotationDescriptors;
    public List<MethodSpecification> methodSpecifications;
    public List<ClassDescriptor> fieldDescriptors;

    public Specification(Identifier identifier,
                         Identifier parentIdentifier,
                         int classAccess,
                         Names interfaceNames,
                         List<ClassDescriptor> annotationDescriptors,
                         List<MethodSpecification> methodSpecifications,
                         List<ClassDescriptor> fieldDescriptors) {
        this.identifier = identifier;
        this.parentIdentifier = parentIdentifier;
        this.classAccess = classAccess;
        this.interfaceNames = interfaceNames;
        this.annotationDescriptors = annotationDescriptors;
        this.methodSpecifications = methodSpecifications;
        this.fieldDescriptors = fieldDescriptors;
    }
}

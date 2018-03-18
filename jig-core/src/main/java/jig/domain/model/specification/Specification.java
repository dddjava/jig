package jig.domain.model.specification;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

import java.util.List;

public class Specification {

    public Name name;
    public Name parentName;
    public int classAccess;
    public Names interfaceNames;
    public List<ClassDescriptor> annotationDescriptors;
    public List<MethodDescriptor> methodDescriptors;
    public List<ClassDescriptor> fieldDescriptors;

    public Specification(Name name,
                         Name parentName,
                         int classAccess,
                         Names interfaceNames,
                         List<ClassDescriptor> annotationDescriptors,
                         List<MethodDescriptor> methodDescriptors,
                         List<ClassDescriptor> fieldDescriptors) {
        this.name = name;
        this.parentName = parentName;
        this.classAccess = classAccess;
        this.interfaceNames = interfaceNames;
        this.annotationDescriptors = annotationDescriptors;
        this.methodDescriptors = methodDescriptors;
        this.fieldDescriptors = fieldDescriptors;
    }
}

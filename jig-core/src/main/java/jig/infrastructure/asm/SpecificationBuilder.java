package jig.infrastructure.asm;

import jig.domain.model.specification.ClassDescriptor;
import jig.domain.model.specification.MethodDescriptor;
import jig.domain.model.specification.Specification;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SpecificationBuilder {

    private String name;
    private String parent;
    private List<String> annotationDescriptors = new ArrayList<>();
    private List<MethodDescriptor> methodDescriptors = new ArrayList<>();
    private List<String> fieldDescriptors = new ArrayList<>();
    private List<String> interfaceNames = new ArrayList<>();
    private int accessor;

    public SpecificationBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public SpecificationBuilder withParent(String parent) {
        this.parent = parent;
        return this;
    }

    public SpecificationBuilder withAccessor(int accessor) {
        this.accessor = accessor;
        return this;
    }

    public SpecificationBuilder withInterfaces(String[] interfaces) {
        this.interfaceNames.addAll(Arrays.asList(interfaces));
        return this;
    }

    public SpecificationBuilder withAnnotation(String descriptor) {
        this.annotationDescriptors.add(descriptor);
        return this;
    }

    public SpecificationBuilder withInstanceField(String descriptor) {
        this.fieldDescriptors.add(descriptor);
        return this;
    }

    public MethodDescriptor newInstanceMethod(String methodName, String descriptor) {
        MethodDescriptor methodDescriptor = new MethodDescriptor(name, methodName, descriptor);

        this.methodDescriptors.add(methodDescriptor);
        return methodDescriptor;
    }

    public Specification build() {
        return new Specification(
                new Name(name),
                new Name(parent),
                accessor,
                interfaceNames.stream().map(Name::new).collect(Names.collector()),
                annotationDescriptors.stream().map(ClassDescriptor::new).collect(Collectors.toList()),
                methodDescriptors,
                fieldDescriptors.stream().map(ClassDescriptor::new).collect(Collectors.toList()));
    }
}

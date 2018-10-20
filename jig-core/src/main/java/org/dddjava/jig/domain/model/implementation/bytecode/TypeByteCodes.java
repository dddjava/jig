package org.dddjava.jig.domain.model.implementation.bytecode;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.datasources.DatasourceMethod;
import org.dddjava.jig.domain.model.datasources.DatasourceMethods;
import org.dddjava.jig.domain.model.declaration.annotation.*;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.*;
import org.dddjava.jig.domain.model.services.ServiceMethods;
import org.dddjava.jig.domain.model.unit.method.Method;
import org.dddjava.jig.domain.model.unit.method.Methods;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * モデルの実装一式
 */
public class TypeByteCodes {
    private final List<TypeByteCode> list;

    public TypeByteCodes(List<TypeByteCode> list) {
        this.list = list;
    }

    public List<TypeByteCode> list() {
        return list;
    }

    public List<MethodByteCode> instanceMethodByteCodes() {
        return list.stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .collect(toList());
    }

    public Methods instanceMethods() {
        List<Method> list = instanceMethodByteCodes().stream()
                .map(MethodByteCode::method)
                .collect(toList());
        return new Methods(list);
    }

    public TypeAnnotations typeAnnotations() {
        List<TypeAnnotation> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            list.addAll(typeByteCode.typeAnnotations());
        }
        return new TypeAnnotations(list);
    }

    public FieldAnnotations annotatedFields() {
        List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            fieldAnnotations.addAll(typeByteCode.annotatedFields());
        }
        return new FieldAnnotations(fieldAnnotations);
    }

    public MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = new ArrayList<>();
        for (MethodByteCode methodByteCode : instanceMethodByteCodes()) {
            methodAnnotations.addAll(methodByteCode.annotatedMethods());
        }
        return new MethodAnnotations(methodAnnotations);
    }

    public FieldDeclarations instanceFields() {
        List<FieldDeclaration> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            FieldDeclarations fieldDeclarations = typeByteCode.fieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new FieldDeclarations(list);
    }

    public StaticFieldDeclarations staticFields() {
        List<StaticFieldDeclaration> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            StaticFieldDeclarations fieldDeclarations = typeByteCode.staticFieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new StaticFieldDeclarations(list);
    }

    public Types types() {
        List<Type> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : list()) {
            list.add(typeByteCode.type());
        }
        return new Types(list);
    }

    public ServiceMethods serviceMethods(Architecture architecture) {
        List<TypeByteCode> serviceByteCodes = list.stream()
                .filter(typeByteCode -> architecture.isService(typeByteCode.typeAnnotations()))
                .collect(Collectors.toList());

        List<Method> methods = serviceByteCodes.stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .map(MethodByteCode::method)
                .collect(Collectors.toList());

        return new ServiceMethods(methods);
    }

    public DatasourceMethods datasourceMethods(Architecture architecture) {
        List<DatasourceMethod> datasourceMethods = new ArrayList<>();
        for (TypeByteCode concreteByteCode : list) {
            if (!architecture.isDataSource(concreteByteCode.typeAnnotations())) {
                continue;
            }

            ParameterizedTypes parameterizedTypes = concreteByteCode.parameterizedInterfaceTypes();
            for (ParameterizedType parameterizedType : parameterizedTypes.list()) {
                TypeIdentifier interfaceTypeIdentifier = parameterizedType.typeIdentifier();
                if (!architecture.isBusinessRule(interfaceTypeIdentifier)) {
                    continue;
                }

                for (TypeByteCode interfaceByteCode : list) {
                    if (!interfaceTypeIdentifier.equals(interfaceByteCode.typeIdentifier())) {
                        continue;
                    }

                    for (MethodByteCode interfaceMethodByteCode : interfaceByteCode.methodByteCodes()) {
                        concreteByteCode.methodByteCodes().stream()
                                .filter(datasourceMethodByteCode -> interfaceMethodByteCode.sameSignature(datasourceMethodByteCode))
                                // 0 or 1
                                .forEach(concreteMethodByteCode ->
                                        datasourceMethods.add(new DatasourceMethod(
                                                interfaceMethodByteCode.method(),
                                                concreteMethodByteCode.method(),
                                                concreteMethodByteCode.usingMethods()))
                                );
                    }
                }
            }
        }

        return new DatasourceMethods(datasourceMethods);
    }
}

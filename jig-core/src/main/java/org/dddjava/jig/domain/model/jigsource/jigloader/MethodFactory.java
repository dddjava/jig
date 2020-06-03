package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Arguments;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodReturn;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.ParameterizedType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.ParameterizedTypes;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Methods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.RequestHandlerMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.jigmodel.smells.StringComparingCallerMethods;
import org.dddjava.jig.domain.model.jigsource.file.binary.MethodByteCode;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCodes;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.BuildingBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MethodFactory {

    public static Method createMethod(MethodByteCode methodByteCode) {
        return new Method(
                methodByteCode.methodDeclaration(),
                methodByteCode.judgeNull(),
                methodByteCode.decisionNumber(),
                methodByteCode.annotatedMethods(),
                methodByteCode.accessor(),
                methodByteCode.methodDepend());
    }

    public static RequestHandlerMethod createRequestHandlerMethod(MethodByteCode methodByteCode, TypeByteCode typeByteCode) {
        Method method = createMethod(methodByteCode);
        TypeAnnotations typeAnnotations = new TypeAnnotations(typeByteCode.typeAnnotations());
        return new RequestHandlerMethod(method, typeAnnotations);
    }

    public static Methods createMethods(TypeByteCodes typeByteCodes) {
        return new Methods(typeByteCodes.instanceMethodByteCodes().stream()
                .map(methodByteCode -> createMethod(methodByteCode))
                .collect(toList()));
    }

    public static ControllerMethods createControllerMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<RequestHandlerMethod> list = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (ApplicationLayer.PRESENTATION.satisfy(typeByteCode, architecture)) {
                for (MethodByteCode methodByteCode : typeByteCode.instanceMethodByteCodes()) {
                    RequestHandlerMethod requestHandlerMethod = createRequestHandlerMethod(methodByteCode, typeByteCode);
                    if (requestHandlerMethod.valid()) {
                        list.add(requestHandlerMethod);
                    }
                }
            }
        }
        return new ControllerMethods(list);
    }

    public static DatasourceMethods createDatasourceMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<DatasourceMethod> list = new ArrayList<>();
        for (TypeByteCode concreteByteCode : typeByteCodes.list()) {
            if (!BuildingBlock.DATASOURCE.satisfy(concreteByteCode, architecture)) {
                continue;
            }

            ParameterizedTypes parameterizedTypes = concreteByteCode.parameterizedInterfaceTypes();
            for (ParameterizedType parameterizedType : parameterizedTypes.list()) {
                TypeIdentifier interfaceTypeIdentifier = parameterizedType.typeIdentifier();

                for (TypeByteCode interfaceByteCode : typeByteCodes.list()) {
                    if (!interfaceTypeIdentifier.equals(interfaceByteCode.typeIdentifier())) {
                        continue;
                    }

                    for (MethodByteCode interfaceMethodByteCode : interfaceByteCode.methodByteCodes()) {
                        concreteByteCode.methodByteCodes().stream()
                                .filter(datasourceMethodByteCode -> interfaceMethodByteCode.sameSignature(datasourceMethodByteCode))
                                // 0 or 1
                                .forEach(concreteMethodByteCode -> list.add(new DatasourceMethod(
                                        createMethod(interfaceMethodByteCode),
                                        createMethod(concreteMethodByteCode),
                                        concreteMethodByteCode.methodDepend().usingMethods().methodDeclarations()))
                                );
                    }
                }
            }
        }
        return new DatasourceMethods(list);
    }

    public static ServiceMethods createServiceMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        TypeByteCodes applications = ApplicationLayer.APPLICATION.filter(typeByteCodes, architecture);

        return new ServiceMethods(applications.list().stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .map(methodByteCode -> createMethod(methodByteCode))
                .collect(toList()));
    }

    public static MethodSmellAngles createMethodSmellAngles(AnalyzedImplementation analyzedImplementation, BusinessRules businessRules) {
        return new MethodSmellAngles(createMethods(analyzedImplementation.typeByteCodes()),
                analyzedImplementation.typeByteCodes().instanceFields(),
                RelationsFactory.createMethodRelations(analyzedImplementation.typeByteCodes()),
                businessRules);
    }

    public static StringComparingCallerMethods from(AnalyzedImplementation analyzedImplementation, Architecture architecture) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ControllerMethods controllerMethods = createControllerMethods(typeByteCodes, architecture);
        ServiceMethods serviceMethods = createServiceMethods(typeByteCodes, architecture);

        // String#equals(Object)
        MethodDeclaration stringEqualsMethod = new MethodDeclaration(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        new Arguments(Collections.singletonList(new TypeIdentifier(Object.class)))),
                new MethodReturn(new TypeIdentifier(boolean.class))
        );

        List<Method> methods = Stream.concat(
                controllerMethods.list().stream()
                        .filter(controllerMethod -> controllerMethod.isCall(stringEqualsMethod))
                        .map(controllerMethod -> controllerMethod.method()),
                serviceMethods.list().stream()
                        .filter(serviceMethod -> serviceMethod.isCall(stringEqualsMethod))
                        .map(controllerMethod -> controllerMethod.method())
        ).collect(toList());

        return new StringComparingCallerMethods(methods);
    }
}

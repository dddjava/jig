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
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.MethodFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.BuildingBlock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MethodFactory {

    public static Method createMethod(MethodFact methodFact) {
        return new Method(
                methodFact.methodDeclaration(),
                methodFact.judgeNull(),
                methodFact.decisionNumber(),
                methodFact.annotatedMethods(),
                methodFact.visibility(),
                methodFact.methodDepend());
    }

    public static RequestHandlerMethod createRequestHandlerMethod(MethodFact methodFact, TypeFact typeFact) {
        Method method = createMethod(methodFact);
        TypeAnnotations typeAnnotations = new TypeAnnotations(typeFact.typeAnnotations());
        return new RequestHandlerMethod(method, typeAnnotations);
    }

    public static Methods createMethods(TypeFacts typeFacts) {
        return new Methods(typeFacts.instanceMethodFacts().stream()
                .map(methodByteCode -> createMethod(methodByteCode))
                .collect(toList()));
    }

    public static ControllerMethods createControllerMethods(TypeFacts typeFacts, Architecture architecture) {
        List<RequestHandlerMethod> list = new ArrayList<>();
        for (TypeFact typeFact : typeFacts.list()) {
            if (ApplicationLayer.PRESENTATION.satisfy(typeFact, architecture)) {
                for (MethodFact methodFact : typeFact.instanceMethodFacts()) {
                    RequestHandlerMethod requestHandlerMethod = createRequestHandlerMethod(methodFact, typeFact);
                    if (requestHandlerMethod.valid()) {
                        list.add(requestHandlerMethod);
                    }
                }
            }
        }
        return new ControllerMethods(list);
    }

    public static DatasourceMethods createDatasourceMethods(TypeFacts typeFacts, Architecture architecture) {
        List<DatasourceMethod> list = new ArrayList<>();
        for (TypeFact concreteByteCode : typeFacts.list()) {
            if (!BuildingBlock.DATASOURCE.satisfy(concreteByteCode, architecture)) {
                continue;
            }

            ParameterizedTypes parameterizedTypes = concreteByteCode.parameterizedInterfaceTypes();
            for (ParameterizedType parameterizedType : parameterizedTypes.list()) {
                TypeIdentifier interfaceTypeIdentifier = parameterizedType.typeIdentifier();

                for (TypeFact interfaceByteCode : typeFacts.list()) {
                    if (!interfaceTypeIdentifier.equals(interfaceByteCode.typeIdentifier())) {
                        continue;
                    }

                    for (MethodFact interfaceMethodFact : interfaceByteCode.allMethodFacts()) {
                        concreteByteCode.allMethodFacts().stream()
                                .filter(datasourceMethodByteCode -> interfaceMethodFact.sameSignature(datasourceMethodByteCode))
                                // 0 or 1
                                .forEach(concreteMethodByteCode -> list.add(new DatasourceMethod(
                                        createMethod(interfaceMethodFact),
                                        createMethod(concreteMethodByteCode),
                                        concreteMethodByteCode.methodDepend().usingMethods().methodDeclarations()))
                                );
                    }
                }
            }
        }
        return new DatasourceMethods(list);
    }

    public static ServiceMethods createServiceMethods(TypeFacts typeFacts, Architecture architecture) {
        return new ServiceMethods(typeFacts.list().stream()
                .filter(typeFact -> ApplicationLayer.APPLICATION.satisfy(typeFact, architecture))
                .map(TypeFact::instanceMethodFacts)
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
        TypeFacts typeFacts = analyzedImplementation.typeByteCodes();
        ControllerMethods controllerMethods = createControllerMethods(typeFacts, architecture);
        ServiceMethods serviceMethods = createServiceMethods(typeFacts, architecture);

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

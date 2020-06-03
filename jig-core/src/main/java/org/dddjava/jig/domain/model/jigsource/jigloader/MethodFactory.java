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

    public static ControllerMethods createControllerMethods(TypeFacts typeFacts, Architecture architecture) {
        List<RequestHandlerMethod> list = new ArrayList<>();
        for (TypeFact typeFact : typeFacts.list()) {
            if (ApplicationLayer.PRESENTATION.satisfy(typeFact, architecture)) {
                for (MethodFact methodFact : typeFact.instanceMethodFacts()) {
                    Method method = methodFact.createMethod();
                    TypeAnnotations typeAnnotations = new TypeAnnotations(typeFact.typeAnnotations());
                    RequestHandlerMethod requestHandlerMethod = new RequestHandlerMethod(method, typeAnnotations);
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
        for (TypeFact typeFact : typeFacts.list()) {
            if (!BuildingBlock.DATASOURCE.satisfy(typeFact, architecture)) {
                continue;
            }

            ParameterizedTypes parameterizedTypes = typeFact.parameterizedInterfaceTypes();
            for (ParameterizedType parameterizedType : parameterizedTypes.list()) {
                TypeIdentifier interfaceTypeIdentifier = parameterizedType.typeIdentifier();

                for (TypeFact interfaceTypeFact : typeFacts.list()) {
                    if (!interfaceTypeIdentifier.equals(interfaceTypeFact.typeIdentifier())) {
                        continue;
                    }

                    // TODO allじゃなくinstanceでよさそう
                    for (MethodFact interfaceMethodFact : interfaceTypeFact.allMethodFacts()) {
                        typeFact.allMethodFacts().stream()
                                .filter(datasourceMethodByteCode -> interfaceMethodFact.sameSignature(datasourceMethodByteCode))
                                // 0 or 1
                                .forEach(concreteMethodByteCode -> list.add(new DatasourceMethod(
                                        interfaceMethodFact.createMethod(),
                                        concreteMethodByteCode.createMethod(),
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
                .map(methodFact -> methodFact.createMethod())
                .collect(toList()));
    }

    public static MethodSmellAngles createMethodSmellAngles(AnalyzedImplementation analyzedImplementation, BusinessRules businessRules) {
        Methods methods = new Methods(analyzedImplementation.typeByteCodes().instanceMethodFacts().stream()
                .map(methodByteCode -> methodByteCode.createMethod())
                .collect(toList()));
        return new MethodSmellAngles(methods,
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

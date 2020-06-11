package org.dddjava.jig.domain.model.jigsource.jigloader;

import org.dddjava.jig.domain.model.jigmodel.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.ParameterizedType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.RequestHandlerMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.MethodFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.BuildingBlock;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MethodFactory {

    public static ControllerMethods createControllerMethods(TypeFacts typeFacts, Architecture architecture) {
        List<RequestHandlerMethod> list = new ArrayList<>();
        for (TypeFact typeFact : typeFacts.list()) {
            if (ApplicationLayer.PRESENTATION.satisfy(typeFact, architecture)) {
                for (MethodFact methodFact : typeFact.instanceMethodFacts()) {
                    Method method = methodFact.createMethod();
                    RequestHandlerMethod requestHandlerMethod = new RequestHandlerMethod(method, new Annotations(typeFact.listAnnotations()));
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
        List<TypeFact> datasourceFacts = typeFacts.list().stream()
                .filter(typeFact1 -> BuildingBlock.DATASOURCE.satisfy(typeFact1, architecture))
                .collect(toList());
        for (TypeFact typeFact : datasourceFacts) {
            for (ParameterizedType interfaceType : typeFact.interfaceTypes()) {
                TypeIdentifier interfaceTypeIdentifier = interfaceType.typeIdentifier();
                typeFacts.selectByTypeIdentifier(interfaceTypeIdentifier).ifPresent(interfaceTypeFact -> {
                    for (MethodFact interfaceMethodFact : interfaceTypeFact.instanceMethodFacts()) {
                        typeFact.instanceMethodFacts().stream()
                                .filter(datasourceMethodByteCode -> interfaceMethodFact.sameSignature(datasourceMethodByteCode))
                                // 0 or 1
                                .forEach(concreteMethodByteCode -> list.add(new DatasourceMethod(
                                        interfaceMethodFact.createMethod(),
                                        concreteMethodByteCode.createMethod(),
                                        concreteMethodByteCode.methodDepend().usingMethods().methodDeclarations()))
                                );
                    }
                });
            }
        }
        return new DatasourceMethods(list);
    }
}

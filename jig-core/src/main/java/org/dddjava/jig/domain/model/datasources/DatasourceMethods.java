package org.dddjava.jig.domain.model.datasources;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.declaration.type.ParameterizedType;
import org.dddjava.jig.domain.model.declaration.type.ParameterizedTypes;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.unit.method.Method;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatasourceMethods {
    List<DatasourceMethod> list;

    public DatasourceMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        this.list = new ArrayList<>();
        for (TypeByteCode concreteByteCode : typeByteCodes.list()) {
            if (!architecture.isDataSource(concreteByteCode.typeAnnotations())) {
                continue;
            }

            ParameterizedTypes parameterizedTypes = concreteByteCode.parameterizedInterfaceTypes();
            for (ParameterizedType parameterizedType : parameterizedTypes.list()) {
                TypeIdentifier interfaceTypeIdentifier = parameterizedType.typeIdentifier();
                if (!architecture.isBusinessRule(interfaceTypeIdentifier)) {
                    continue;
                }

                for (TypeByteCode interfaceByteCode : typeByteCodes.list()) {
                    if (!interfaceTypeIdentifier.equals(interfaceByteCode.typeIdentifier())) {
                        continue;
                    }

                    for (MethodByteCode interfaceMethodByteCode : interfaceByteCode.methodByteCodes()) {
                        concreteByteCode.methodByteCodes().stream()
                                .filter(datasourceMethodByteCode -> interfaceMethodByteCode.sameSignature(datasourceMethodByteCode))
                                // 0 or 1
                                .forEach(concreteMethodByteCode -> list.add(new DatasourceMethod(
                                        new Method(interfaceMethodByteCode),
                                        new Method(concreteMethodByteCode),
                                        concreteMethodByteCode.usingMethods()))
                                );
                    }
                }
            }
        }
    }

    public List<DatasourceMethod> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public RepositoryMethods repositoryMethods() {
        return list.stream().map(DatasourceMethod::repositoryMethod)
                .collect(Collectors.collectingAndThen(Collectors.toList(), RepositoryMethods::new));
    }
}

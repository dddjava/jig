package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.fact.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.fact.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.richmethod.RequestHandlerMethod;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * コントローラーメソッド一覧
 */
public class ControllerMethods {
    List<RequestHandlerMethod> list;

    public ControllerMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<TypeByteCode> controllerTypeByteCode = typeByteCodes.list().stream()
                .filter(typeByteCode -> architecture.isController(typeByteCode.typeAnnotations()))
                .collect(toList());

        List<RequestHandlerMethod> result = new ArrayList<>();
        for (TypeByteCode typeByteCode : controllerTypeByteCode) {
            List<MethodByteCode> methodByteCodes = typeByteCode.instanceMethodByteCodes();
            for (MethodByteCode methodByteCode : methodByteCodes) {
                RequestHandlerMethod requestHandlerMethod = new RequestHandlerMethod(methodByteCode, typeByteCode);
                if (requestHandlerMethod.valid()) {
                    result.add(requestHandlerMethod);
                }
            }
        }
        this.list = result;
    }

    private ControllerMethods(List<RequestHandlerMethod> list) {
        this.list = list;
    }

    public List<RequestHandlerMethod> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public ControllerMethods filter(CallerMethods callerMethods) {
        return list.stream()
                .filter(requestHandlerMethod -> requestHandlerMethod.anyMatch(callerMethods))
                .collect(collectingAndThen(toList(), ControllerMethods::new));
    }
}

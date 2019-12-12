package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.jigsource.bytecode.MethodByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.jigloaded.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.jigloaded.architecture.Architecture;
import org.dddjava.jig.domain.model.jigloaded.relation.method.CallerMethods;
import org.dddjava.jig.domain.model.jigloaded.richmethod.RequestHandlerMethod;

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
        List<RequestHandlerMethod> result = new ArrayList<>();
        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            if (ApplicationLayer.PRESENTATION.satisfy(typeByteCode, architecture)) {
                for (MethodByteCode methodByteCode : typeByteCode.instanceMethodByteCodes()) {
                    RequestHandlerMethod requestHandlerMethod = new RequestHandlerMethod(methodByteCode, typeByteCode);
                    if (requestHandlerMethod.valid()) {
                        result.add(requestHandlerMethod);
                    }
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

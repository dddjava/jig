package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.unit.method.Method;

import java.util.List;
import java.util.stream.Collectors;

public class ControllerMethods {
    List<Method> list;

    public ControllerMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<TypeByteCode> controllerTypeByteCode = typeByteCodes.list().stream()
                .filter(typeByteCode -> architecture.isController(typeByteCode.typeAnnotations()))
                .collect(Collectors.toList());

        this.list = controllerTypeByteCode.stream()
                .map(TypeByteCode::instanceMethodByteCodes)
                .flatMap(List::stream)
                .map(methodByteCode -> new Method(methodByteCode))
                .filter(method ->
                        method.methodAnnotations().list().stream()
                                .anyMatch(annotatedMethod -> {
                                            String annotationName = annotatedMethod.annotationType().fullQualifiedName();
                                            // RequestMappingをメタアノテーションとして使うものにしたいが、spring-webに依存させたくないので列挙にする
                                            // そのため独自アノテーションに対応できない
                                            return annotationName.equals("org.springframework.web.bind.annotation.RequestMapping")
                                                    || annotationName.equals("org.springframework.web.bind.annotation.GetMapping")
                                                    || annotationName.equals("org.springframework.web.bind.annotation.PostMapping")
                                                    || annotationName.equals("org.springframework.web.bind.annotation.PutMapping")
                                                    || annotationName.equals("org.springframework.web.bind.annotation.DeleteMapping")
                                                    || annotationName.equals("org.springframework.web.bind.annotation.PatchMapping");
                                        }
                                ))
                .collect(Collectors.toList());
    }

    public List<Method> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }
}

package org.dddjava.jig.domain.model.threelayer.controllers;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.unit.method.Method;

import java.util.List;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * コントローラーメソッド一覧
 */
public class ControllerMethods {
    List<Method> list;

    public ControllerMethods(TypeByteCodes typeByteCodes, Architecture architecture) {
        List<TypeByteCode> controllerTypeByteCode = typeByteCodes.list().stream()
                .filter(typeByteCode -> architecture.isController(typeByteCode.typeAnnotations()))
                .collect(toList());

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
                .collect(toList());
    }

    private ControllerMethods(List<Method> list) {
        this.list = list;
    }

    public List<Method> list() {
        return list;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public ControllerMethods filter(MethodDeclarations methodDeclarations) {
        return list.stream()
                .filter(method -> methodDeclarations.contains(method.declaration()))
                .collect(collectingAndThen(toList(), ControllerMethods::new));
    }
}

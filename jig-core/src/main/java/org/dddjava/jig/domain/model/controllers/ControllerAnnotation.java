package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.List;
import java.util.StringJoiner;

/**
 * コントローラーアノテーション
 */
public class ControllerAnnotation {

    private final Annotations typeAnnotations;
    private final Annotations methodAnnotations;

    public ControllerAnnotation(TypeAnnotations typeAnnotations, MethodAnnotations methodAnnotations) {
        TypeIdentifier[] mappingAnnotations = {
                new TypeIdentifier("org.springframework.web.bind.annotation.RequestMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.GetMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.PostMapping")
        };
        this.typeAnnotations = typeAnnotations.annotations().filterAny(mappingAnnotations);
        this.methodAnnotations = methodAnnotations.annotations().filterAny(mappingAnnotations);
    }

    public String pathText() {
        List<String> typePaths = typeAnnotations.descriptionTextsOf("value");
        typePaths.addAll(typeAnnotations.descriptionTextsOf("path"));
        if (typePaths.isEmpty()) typePaths.add("");

        List<String> methodPaths = methodAnnotations.descriptionTextsOf("value");
        methodPaths.addAll(methodAnnotations.descriptionTextsOf("path"));

        StringJoiner stringJoiner = new StringJoiner(", ", "[", "]");
        for (String typePath : typePaths) {
            for (String methodPath : methodPaths) {
                if (typePath.isEmpty()) {
                    stringJoiner.add(methodPath);
                } else if (methodPath.startsWith("/")) {
                    String pathText = typePath + methodPath;
                    stringJoiner.add(pathText);
                } else {
                    String pathText = typePath + "/" + methodPath;
                    stringJoiner.add(pathText);
                }
            }
        }
        return stringJoiner.toString();
    }
}

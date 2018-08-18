package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.List;
import java.util.StringJoiner;

/**
 * コントローラーアノテーションの一覧
 */
public class ControllerAnnotations {

    private final Annotations typeAnnotations;
    private final Annotations methodAnnotations;

    public ControllerAnnotations(TypeAnnotations typeAnnotations, MethodAnnotations methodAnnotations) {
        this.typeAnnotations = typeAnnotations.annotations().filterAny(
                new TypeIdentifier("org.springframework.web.bind.annotation.RequestMapping"));
        this.methodAnnotations = methodAnnotations.annotations().filterAny(
                new TypeIdentifier("org.springframework.web.bind.annotation.RequestMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.GetMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.PostMapping"));
    }

    public String pathTexts() {
        List<String> typePaths = typeAnnotations.descriptionTextsOf("value");
        typePaths.addAll(typeAnnotations.descriptionTextsOf("path"));
        if (typePaths.isEmpty()) typePaths.add("");

        List<String> methodPaths = methodAnnotations.descriptionTextsOf("value");
        methodPaths.addAll(methodAnnotations.descriptionTextsOf("path"));

        StringJoiner pathTexts = new StringJoiner(", ", "[", "]");
        for (String typePath : typePaths) {
            for (String methodPath : methodPaths) {
                String pathText = combinePath(typePath, methodPath);
                pathTexts.add(pathText);
            }
        }
        return pathTexts.toString();
    }

    private String combinePath(String typePath, String methodPath) {
        String pathText;
        if (typePath.isEmpty()) {
            pathText = methodPath;
        } else if (methodPath.startsWith("/")) {
            pathText = typePath + methodPath;
        } else {
            pathText = typePath + "/" + methodPath;
        }
        return pathText;
    }
}

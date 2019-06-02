package org.dddjava.jig.domain.model.architecture;

import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;

import java.util.List;

/**
 * アーキテクチャ
 */
public class Architecture {

    public boolean isService(List<TypeAnnotation> typeAnnotations) {
        TypeIdentifier serviceAnnotation = new TypeIdentifier("org.springframework.stereotype.Service");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(serviceAnnotation));
    }

    public boolean isDataSource(List<TypeAnnotation> typeAnnotations) {
        // DataSourceは Repositoryインタフェースが実装され @Repository のついた infrastructure/datasource のクラス
        TypeIdentifier repositoryAnnotation = new TypeIdentifier("org.springframework.stereotype.Repository");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(repositoryAnnotation));
    }

    public boolean isController(List<TypeAnnotation> typeAnnotations) {
        TypeIdentifier controller = new TypeIdentifier("org.springframework.stereotype.Controller");
        TypeIdentifier restController = new TypeIdentifier("org.springframework.web.bind.annotation.RestController");
        TypeIdentifier controllerAdvice = new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(controller)
                        || typeAnnotation.typeIs(restController)
                        || typeAnnotation.typeIs(controllerAdvice));
    }

    public ArchitectureBlock layer(TypeByteCode typeByteCode) {
        // TODO enumにもってきたい
        if (isService(typeByteCode.typeAnnotations())) return ArchitectureBlock.APPLICATION;
        if (isDataSource(typeByteCode.typeAnnotations())) return ArchitectureBlock.DATASOURCE;
        if (isController(typeByteCode.typeAnnotations())) return ArchitectureBlock.PRESENTATION;

        return ArchitectureBlock.OTHER;
    }
}

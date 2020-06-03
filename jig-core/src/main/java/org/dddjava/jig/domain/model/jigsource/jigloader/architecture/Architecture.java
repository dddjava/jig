package org.dddjava.jig.domain.model.jigsource.jigloader.architecture;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;

import java.util.List;

/**
 * アーキテクチャ
 */
public class Architecture {

    IsBusinessRule isBusinessRule;

    public Architecture(IsBusinessRule isBusinessRule) {
        this.isBusinessRule = isBusinessRule;
    }

    boolean isService(List<TypeAnnotation> typeAnnotations) {
        TypeIdentifier serviceAnnotation = new TypeIdentifier("org.springframework.stereotype.Service");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(serviceAnnotation));
    }

    boolean isDataSource(TypeFact typeFact) {
        // TODO インタフェース実装を見てない
        // DataSourceは Repositoryインタフェースが実装され @Repository のついた infrastructure/datasource のクラス
        List<TypeAnnotation> typeAnnotations = typeFact.typeAnnotations();
        TypeIdentifier repositoryAnnotation = new TypeIdentifier("org.springframework.stereotype.Repository");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(repositoryAnnotation));
    }

    boolean isController(List<TypeAnnotation> typeAnnotations) {
        TypeIdentifier controller = new TypeIdentifier("org.springframework.stereotype.Controller");
        TypeIdentifier restController = new TypeIdentifier("org.springframework.web.bind.annotation.RestController");
        TypeIdentifier controllerAdvice = new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice");
        return typeAnnotations.stream()
                .anyMatch(typeAnnotation -> typeAnnotation.typeIs(controller)
                        || typeAnnotation.typeIs(restController)
                        || typeAnnotation.typeIs(controllerAdvice));
    }

    public boolean isBusinessRule(TypeFact typeFact) {
        return isBusinessRule.isBusinessRule(typeFact);
    }
}

package org.dddjava.jig.domain.model.jigsource.jigloader.architecture;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotation;
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

    boolean isService(TypeFact typeFact) {
        TypeIdentifier serviceAnnotation = new TypeIdentifier("org.springframework.stereotype.Service");
        return typeFact.listAnnotations().stream()
                .anyMatch(annotation -> annotation.is(serviceAnnotation));
    }

    boolean isDataSource(TypeFact typeFact) {
        // TODO インタフェース実装を見てない
        // DataSourceは Repositoryインタフェースが実装され @Repository のついた infrastructure/datasource のクラス
        List<Annotation> typeAnnotations = typeFact.listAnnotations();
        TypeIdentifier repositoryAnnotation = new TypeIdentifier("org.springframework.stereotype.Repository");
        return typeAnnotations.stream()
                .anyMatch(annotation -> annotation.is(repositoryAnnotation));
    }

    boolean isController(TypeFact typeFact) {
        TypeIdentifier controller = new TypeIdentifier("org.springframework.stereotype.Controller");
        TypeIdentifier restController = new TypeIdentifier("org.springframework.web.bind.annotation.RestController");
        TypeIdentifier controllerAdvice = new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice");
        return typeFact.listAnnotations().stream()
                .anyMatch(annotation -> annotation.is(controller)
                        || annotation.is(restController)
                        || annotation.is(controllerAdvice));
    }

    public boolean isBusinessRule(TypeFact typeFact) {
        return isBusinessRule.isBusinessRule(typeFact);
    }
}

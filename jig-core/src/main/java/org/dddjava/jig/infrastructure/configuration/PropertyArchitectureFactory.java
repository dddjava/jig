package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;

import java.util.List;
import java.util.regex.Pattern;

public class PropertyArchitectureFactory {

    JigProperties jigProperties;

    public PropertyArchitectureFactory(JigProperties jigProperties) {
        this.jigProperties = jigProperties;
    }

    public Architecture architecture() {
        Pattern compilerGeneratedClassPattern = Pattern.compile(".+\\$\\d+");
        Pattern businessRulePattern = Pattern.compile(jigProperties.getBusinessRulePattern());

        return new Architecture() {

            @Override
            public boolean isService(TypeFact typeFact) {
                TypeIdentifier serviceAnnotation = new TypeIdentifier("org.springframework.stereotype.Service");
                return typeFact.listAnnotations().stream()
                        .anyMatch(annotation -> annotation.is(serviceAnnotation));
            }

            @Override
            public boolean isDataSource(TypeFact typeFact) {
                // TODO インタフェース実装を見てない
                // DataSourceは Repositoryインタフェースが実装され @Repository のついた infrastructure/datasource のクラス
                List<Annotation> typeAnnotations = typeFact.listAnnotations();
                TypeIdentifier repositoryAnnotation = new TypeIdentifier("org.springframework.stereotype.Repository");
                return typeAnnotations.stream()
                        .anyMatch(annotation -> annotation.is(repositoryAnnotation));
            }

            @Override
            public boolean isController(TypeFact typeFact) {
                TypeIdentifier controller = new TypeIdentifier("org.springframework.stereotype.Controller");
                TypeIdentifier restController = new TypeIdentifier("org.springframework.web.bind.annotation.RestController");
                TypeIdentifier controllerAdvice = new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice");
                return typeFact.listAnnotations().stream()
                        .anyMatch(annotation -> annotation.is(controller)
                                || annotation.is(restController)
                                || annotation.is(controllerAdvice));
            }

            @Override
            public boolean isBusinessRule(TypeFact typeFact) {
                String fqn = typeFact.typeIdentifier().fullQualifiedName();
                return businessRulePattern.matcher(fqn).matches()
                        && !compilerGeneratedClassPattern.matcher(fqn).matches();
            }
        };
    }
}

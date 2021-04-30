package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureComponent;
import org.dddjava.jig.domain.model.jigsource.jigfactory.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigfactory.TypeFact;
import org.dddjava.jig.domain.model.parts.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

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
        Pattern infrastructurePattern = Pattern.compile(jigProperties.infrastructurePattern);
        Pattern presentationPattern = Pattern.compile(jigProperties.presentationPattern);
        Pattern applicationPattern = Pattern.compile(jigProperties.applicationPattern);

        return new Architecture() {

            @Override
            public boolean isService(TypeFact typeFact) {
                TypeIdentifier serviceAnnotation = new TypeIdentifier("org.springframework.stereotype.Service");
                return typeFact.listAnnotations().stream()
                        .anyMatch(annotation -> annotation.is(serviceAnnotation));
            }

            @Override
            public boolean isRepositoryImplementation(TypeFact typeFact) {
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

            @Override
            public ArchitectureComponent architectureComponent(TypeFact typeFact) {
                String fqn = typeFact.typeIdentifier().fullQualifiedName();
                if (businessRulePattern.matcher(fqn).matches()) {
                    return ArchitectureComponent.BUSINESS_RULE;
                }
                // isRepositoryImplementationは見てもあまり意味がないので使用しない。
                if (infrastructurePattern.matcher(fqn).matches()) {
                    return ArchitectureComponent.INFRASTRUCTURE;
                }
                if (presentationPattern.matcher(fqn).matches()) {
                    return ArchitectureComponent.PRESENTATION;
                }
                if (applicationPattern.matcher(fqn).matches()) {
                    return ArchitectureComponent.APPLICATION;
                }

                return ArchitectureComponent.OTHERS;
            }
        };
    }
}

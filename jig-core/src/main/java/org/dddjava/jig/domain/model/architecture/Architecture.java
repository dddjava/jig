package org.dddjava.jig.domain.model.architecture;

import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.List;

public class Architecture {

    BusinessRuleCondition businessRuleCondition;

    public Architecture(BusinessRuleCondition businessRuleCondition) {
        this.businessRuleCondition = businessRuleCondition;
    }

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

    public boolean isBusinessRule(TypeIdentifier typeIdentifier) {
        return businessRuleCondition.judge(typeIdentifier);
    }
}

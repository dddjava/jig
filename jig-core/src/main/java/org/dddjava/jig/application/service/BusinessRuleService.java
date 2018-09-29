package org.dddjava.jig.application.service;

import org.dddjava.jig.annotation.Progress;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.declaration.type.Types;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.smells.MethodSmellAngles;
import org.springframework.stereotype.Service;

/**
 * ビジネスルールサービス
 */
@Progress("これから成長させる")
@Service
public class BusinessRuleService {

    BusinessRuleCondition businessRuleCondition;

    public BusinessRuleService(BusinessRuleCondition businessRuleCondition) {
        this.businessRuleCondition = businessRuleCondition;
    }

    /**
     * ビジネスルール一覧を取得する
     */
    public BusinessRules businessRules(Types types) {
        return new BusinessRules(types, businessRuleCondition);
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellAngles methodSmellAngles(ProjectData projectData) {
        return new MethodSmellAngles(
                projectData.methods(),
                projectData.methodUsingFields(),
                projectData.fieldDeclarations(),
                projectData.methodRelations(),
                businessRules(projectData.types()));
    }
}

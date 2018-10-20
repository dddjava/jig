package org.dddjava.jig.application.service;

import org.dddjava.jig.annotation.Progress;
import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.categories.CategoryTypes;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.collections.CollectionTypes;
import org.dddjava.jig.domain.model.declaration.type.Types;
import org.dddjava.jig.domain.model.implementation.bytecode.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;
import org.dddjava.jig.domain.model.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.unit.method.Methods;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.domain.model.values.ValueTypes;
import org.springframework.stereotype.Service;

/**
 * ビジネスルールの分析サービス
 */
@Progress("安定")
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
    public MethodSmellAngles methodSmells(ProjectData projectData) {

        return new MethodSmellAngles(
                new Methods(projectData),
                new MethodUsingFields(projectData),
                projectData.instanceFields(),
                new MethodRelations(projectData),
                businessRules(projectData.types()));
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryAngles categories(ProjectData projectData) {
        CategoryTypes categoryTypes = new CategoryTypes(projectData);

        return new CategoryAngles(categoryTypes,
                new TypeDependencies(projectData),
                projectData.instanceFields(),
                projectData.staticFields());
    }

    /**
     * 値一覧を取得する
     */
    public ValueAngles values(ValueKind valueKind, ProjectData projectData) {
        ValueTypes valueTypes = new ValueTypes(projectData, valueKind);

        return new ValueAngles(valueKind, valueTypes, new TypeDependencies(projectData));
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collections(ProjectData projectData) {
        CollectionTypes collectionTypes = new CollectionTypes(projectData);

        return new CollectionAngles(collectionTypes, new TypeDependencies(projectData));
    }
}

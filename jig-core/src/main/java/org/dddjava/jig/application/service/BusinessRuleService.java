package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleCondition;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.categories.CategoryTypes;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.collections.CollectionTypes;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.type.TypeRelations;
import org.dddjava.jig.domain.model.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.dddjava.jig.domain.model.values.ValueTypes;
import org.springframework.stereotype.Service;

/**
 * ビジネスルールの分析サービス
 */
@Service
public class BusinessRuleService {

    BusinessRuleCondition businessRuleCondition;

    public BusinessRuleService(BusinessRuleCondition businessRuleCondition) {
        this.businessRuleCondition = businessRuleCondition;
    }

    /**
     * ビジネスルール一覧を取得する
     */
    public BusinessRules businessRules(AnalyzedImplementation analyzedImplementation) {
        return businessRuleCondition.sorting(analyzedImplementation.typeByteCodes());
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellAngles methodSmells(AnalyzedImplementation analyzedImplementation) {
        return new MethodSmellAngles(analyzedImplementation, businessRules(analyzedImplementation));
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryAngles categories(AnalyzedImplementation analyzedImplementation) {
        CategoryTypes categoryTypes = new CategoryTypes(businessRules(analyzedImplementation));
        return new CategoryAngles(categoryTypes, analyzedImplementation);
    }

    /**
     * 値一覧を取得する
     */
    public ValueAngles values(ValueKind valueKind, AnalyzedImplementation analyzedImplementation) {
        ValueTypes valueTypes = new ValueTypes(businessRules(analyzedImplementation), valueKind);

        return new ValueAngles(valueKind, valueTypes, new TypeRelations(analyzedImplementation.typeByteCodes()));
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collections(AnalyzedImplementation analyzedImplementation) {
        BusinessRules businessRules = businessRules(analyzedImplementation);
        CollectionTypes collectionTypes = new CollectionTypes(businessRules);

        return new CollectionAngles(collectionTypes, new TypeRelations(analyzedImplementation.typeByteCodes()));
    }
}

package org.dddjava.jig.application.service;

import org.dddjava.jig.annotation.Progress;
import org.dddjava.jig.domain.model.architecture.BusinessRuleCondition;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.categories.CategoryTypes;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.collections.CollectionTypes;
import org.dddjava.jig.domain.model.declaration.type.Types;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
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
    public MethodSmellAngles methodSmells(TypeByteCodes typeByteCodes) {

        return new MethodSmellAngles(
                new Methods(typeByteCodes),
                new MethodUsingFields(typeByteCodes),
                typeByteCodes.instanceFields(),
                new MethodRelations(typeByteCodes),
                businessRules(typeByteCodes.types()));
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryAngles categories(TypeByteCodes typeByteCodes) {
        CategoryTypes categoryTypes = new CategoryTypes(typeByteCodes);

        return new CategoryAngles(categoryTypes,
                new TypeDependencies(typeByteCodes),
                typeByteCodes.instanceFields(),
                typeByteCodes.staticFields());
    }

    /**
     * 値一覧を取得する
     */
    public ValueAngles values(ValueKind valueKind, TypeByteCodes typeByteCodes) {
        ValueTypes valueTypes = new ValueTypes(typeByteCodes, valueKind);

        return new ValueAngles(valueKind, valueTypes, new TypeDependencies(typeByteCodes));
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collections(TypeByteCodes typeByteCodes) {
        CollectionTypes collectionTypes = new CollectionTypes(typeByteCodes);

        return new CollectionAngles(collectionTypes, new TypeDependencies(typeByteCodes));
    }
}

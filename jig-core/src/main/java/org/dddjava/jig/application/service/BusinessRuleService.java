package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.categories.CategoryTypes;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.collections.CollectionTypes;
import org.dddjava.jig.domain.model.implementation.analyzed.architecture.Architecture;
import org.dddjava.jig.domain.model.implementation.analyzed.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.Types;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.method.MethodRelations;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.type.TypeRelations;
import org.dddjava.jig.domain.model.implementation.analyzed.unit.method.Methods;
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

    Architecture architecture;

    public BusinessRuleService(Architecture architecture) {
        this.architecture = architecture;
    }

    /**
     * ビジネスルール一覧を取得する
     */
    public BusinessRules businessRules(Types types) {
        return new BusinessRules(types, architecture);
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellAngles methodSmells(TypeByteCodes typeByteCodes) {

        return new MethodSmellAngles(
                new Methods(typeByteCodes),
                typeByteCodes.instanceFields(),
                new MethodRelations(typeByteCodes),
                businessRules(typeByteCodes.types()));
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryAngles categories(TypeByteCodes typeByteCodes) {
        CategoryTypes categoryTypes = new CategoryTypes(typeByteCodes, architecture);

        return new CategoryAngles(categoryTypes,
                new TypeRelations(typeByteCodes),
                typeByteCodes.instanceFields(),
                typeByteCodes.staticFields());
    }

    /**
     * 値一覧を取得する
     */
    public ValueAngles values(ValueKind valueKind, TypeByteCodes typeByteCodes) {
        ValueTypes valueTypes = new ValueTypes(typeByteCodes, valueKind, architecture);

        return new ValueAngles(valueKind, valueTypes, new TypeRelations(typeByteCodes));
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collections(TypeByteCodes typeByteCodes) {
        BusinessRules businessRules = businessRules(typeByteCodes.types());
        CollectionTypes collectionTypes = new CollectionTypes(businessRules, typeByteCodes);

        return new CollectionAngles(collectionTypes, new TypeRelations(typeByteCodes));
    }
}

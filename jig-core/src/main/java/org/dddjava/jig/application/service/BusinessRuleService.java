package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocumenter.diagram.CategoryDiagram;
import org.dddjava.jig.domain.model.jigdocumenter.diagram.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.businessrules.ValueKind;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionAngles;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionTypes;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.jigmodel.values.ValueAngles;
import org.dddjava.jig.domain.model.jigmodel.values.ValueTypes;
import org.dddjava.jig.domain.model.jigsource.jigloader.MethodFactory;
import org.dddjava.jig.domain.model.jigsource.jigloader.TypeFactory;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.Architecture;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

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
    public BusinessRules businessRules(AnalyzedImplementation analyzedImplementation) {
        TypeFacts typeFacts = analyzedImplementation.typeFacts();
        return TypeFactory.from(typeFacts, architecture);
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellAngles methodSmells(AnalyzedImplementation analyzedImplementation) {
        return MethodFactory.createMethodSmellAngles(analyzedImplementation, businessRules(analyzedImplementation));
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryDiagram categories(AnalyzedImplementation analyzedImplementation) {
        CategoryTypes categoryTypes = TypeFactory.createCategoryTypes(businessRules(analyzedImplementation));
        TypeFacts typeFacts = analyzedImplementation.typeFacts();
        ClassRelations classRelations = typeFacts.toClassRelations();
        FieldDeclarations fieldDeclarations = typeFacts.instanceFields();
        StaticFieldDeclarations staticFieldDeclarations = typeFacts.staticFields();

        return CategoryDiagram.categoryDiagram(categoryTypes, classRelations, fieldDeclarations, staticFieldDeclarations);
    }

    /**
     * 値一覧を取得する
     */
    public ValueAngles values(ValueKind valueKind, AnalyzedImplementation analyzedImplementation) {
        ValueTypes valueTypes = new ValueTypes(businessRules(analyzedImplementation), valueKind);

        return new ValueAngles(valueKind, valueTypes, analyzedImplementation.typeFacts().toClassRelations());
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collections(AnalyzedImplementation analyzedImplementation) {
        BusinessRules businessRules = businessRules(analyzedImplementation);
        CollectionTypes collectionTypes = new CollectionTypes(businessRules);

        return new CollectionAngles(collectionTypes, analyzedImplementation.typeFacts().toClassRelations());
    }

    /**
     * 区分使用図
     */
    public CategoryUsageDiagram categoryUsages(AnalyzedImplementation analyzedImplementation) {
        CategoryTypes categoryTypes = TypeFactory.createCategoryTypes(businessRules(analyzedImplementation));
        ServiceMethods serviceMethods = MethodFactory.createServiceMethods(analyzedImplementation.typeFacts(), architecture);
        ClassRelations businessRuleRelations = new TypeFacts(analyzedImplementation.typeFacts().list()
                .stream()
                .filter(typeByteCode -> architecture.isBusinessRule(typeByteCode))
                .collect(Collectors.toList())).toClassRelations(
        );

        return new CategoryUsageDiagram(serviceMethods, categoryTypes, businessRuleRelations);
    }
}

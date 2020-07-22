package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.jigdocument.implementation.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.MethodSmellList;
import org.dddjava.jig.domain.model.jigdocument.specification.Categories;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.businessrules.ValueKind;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionAngles;
import org.dddjava.jig.domain.model.jigmodel.collections.CollectionTypes;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.ValidationAnnotatedMembers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Methods;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.values.ValueAngles;
import org.dddjava.jig.domain.model.jigmodel.values.ValueTypes;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;

/**
 * ビジネスルールの分析サービス
 */
@Service
public class BusinessRuleService {

    final Architecture architecture;
    final JigSourceRepository jigSourceRepository;

    public BusinessRuleService(Architecture architecture, JigSourceRepository jigSourceRepository) {
        this.architecture = architecture;
        this.jigSourceRepository = jigSourceRepository;
    }

    /**
     * ビジネスルール一覧を取得する
     */
    public BusinessRules businessRules() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return typeFacts.toBusinessRule(architecture);
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellList methodSmells() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        Methods methods = new Methods(typeFacts.instanceMethodFacts().stream()
                .map(methodByteCode -> methodByteCode.createMethod())
                .collect(toList()));
        return new MethodSmellList(methods,
                typeFacts.instanceFields(),
                typeFacts.toMethodRelations(),
                businessRules());
    }

    /**
     * 区分一覧を取得する
     */
    public Categories categories() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        CategoryTypes categoryTypes = businessRules().createCategoryTypes();
        ClassRelations classRelations = typeFacts.toClassRelations();
        FieldDeclarations fieldDeclarations = typeFacts.instanceFields();
        StaticFieldDeclarations staticFieldDeclarations = typeFacts.staticFields();

        return Categories.create(categoryTypes, classRelations, fieldDeclarations, staticFieldDeclarations);
    }

    /**
     * 値一覧を取得する
     */
    public ValueAngles values(ValueKind valueKind) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        ValueTypes valueTypes = new ValueTypes(businessRules(), valueKind);

        return new ValueAngles(valueKind, valueTypes, typeFacts.toClassRelations());
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collections() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        BusinessRules businessRules = businessRules();
        CollectionTypes collectionTypes = new CollectionTypes(businessRules);

        return new CollectionAngles(collectionTypes, typeFacts.toClassRelations());
    }

    /**
     * 区分使用図
     */
    public CategoryUsageDiagram categoryUsages() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        CategoryTypes categoryTypes = businessRules().createCategoryTypes();
        ServiceMethods serviceMethods = new ServiceMethods(typeFacts.applicationMethodsOf(architecture));
        ClassRelations businessRuleRelations = new TypeFacts(typeFacts.list()
                .stream()
                .filter(typeByteCode -> architecture.isBusinessRule(typeByteCode))
                .collect(Collectors.toList())).toClassRelations(
        );

        return new CategoryUsageDiagram(serviceMethods, categoryTypes, businessRuleRelations);
    }

    public ValidationAnnotatedMembers validationAnnotatedMembers() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return typeFacts.validationAnnotatedMembers();
    }
}

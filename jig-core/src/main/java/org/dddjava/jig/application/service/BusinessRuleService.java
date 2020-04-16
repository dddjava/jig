package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigloader.MethodFactory;
import org.dddjava.jig.domain.model.jigloader.RelationsFactory;
import org.dddjava.jig.domain.model.jigloader.TypeFactory;
import org.dddjava.jig.domain.model.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.businessrules.CategoryTypes;
import org.dddjava.jig.domain.model.jigmodel.businessrules.ValueKind;
import org.dddjava.jig.domain.model.jigmodel.smells.MethodSmellAngles;
import org.dddjava.jig.domain.model.jigpresentation.collections.CollectionAngles;
import org.dddjava.jig.domain.model.jigpresentation.collections.CollectionTypes;
import org.dddjava.jig.domain.model.jigpresentation.diagram.CategoryDiagram;
import org.dddjava.jig.domain.model.jigpresentation.diagram.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.jigpresentation.values.ValueAngles;
import org.dddjava.jig.domain.model.jigpresentation.values.ValueTypes;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;
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
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        return TypeFactory.from(typeByteCodes, architecture);
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
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ClassRelations classRelations = RelationsFactory.createClassRelations(typeByteCodes);
        FieldDeclarations fieldDeclarations = typeByteCodes.instanceFields();
        StaticFieldDeclarations staticFieldDeclarations = typeByteCodes.staticFields();

        return CategoryDiagram.categoryDiagram(categoryTypes, classRelations, fieldDeclarations, staticFieldDeclarations);
    }

    /**
     * 値一覧を取得する
     */
    public ValueAngles values(ValueKind valueKind, AnalyzedImplementation analyzedImplementation) {
        ValueTypes valueTypes = new ValueTypes(businessRules(analyzedImplementation), valueKind);

        return new ValueAngles(valueKind, valueTypes, RelationsFactory.createClassRelations(analyzedImplementation.typeByteCodes()));
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collections(AnalyzedImplementation analyzedImplementation) {
        BusinessRules businessRules = businessRules(analyzedImplementation);
        CollectionTypes collectionTypes = new CollectionTypes(businessRules);

        return new CollectionAngles(collectionTypes, RelationsFactory.createClassRelations(analyzedImplementation.typeByteCodes()));
    }

    public CategoryUsageDiagram categoryUsages(AnalyzedImplementation analyzedImplementation) {
        CategoryTypes categoryTypes = TypeFactory.createCategoryTypes(businessRules(analyzedImplementation));
        ServiceMethods serviceMethods = MethodFactory.createServiceMethods(analyzedImplementation.typeByteCodes(), architecture);
        ClassRelations classRelations = RelationsFactory.createClassRelations(
                new TypeByteCodes(analyzedImplementation.typeByteCodes().list()
                        .stream()
                        .filter(typeByteCode -> architecture.isBusinessRule(typeByteCode))
                        .collect(Collectors.toList()))
        );

        return new CategoryUsageDiagram(serviceMethods, categoryTypes, classRelations);
    }
}

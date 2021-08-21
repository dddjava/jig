package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.models.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.models.domains.collections.JigCollectionTypes;
import org.dddjava.jig.domain.model.models.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.springframework.stereotype.Service;

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
        return BusinessRules.from(architecture, typeFacts.toClassRelations(), typeFacts.jigTypes());
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellList methodSmells() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        MethodRelations methodRelations = typeFacts.toMethodRelations();
        return new MethodSmellList(businessRules(), methodRelations);
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryDiagram categories() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();

        return CategoryDiagram.create(CategoryTypes.from(businessRules().jigTypes()), typeFacts.toClassRelations());
    }

    /**
     * コレクションを分析する
     */
    public JigCollectionTypes collections() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();

        return new JigCollectionTypes(businessRules().jigTypes(), typeFacts.toClassRelations());
    }

    /**
     * 区分使用図
     */
    public CategoryUsageDiagram categoryUsages() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        JigTypes jigTypes = typeFacts.jigTypes();

        BusinessRules businessRules = BusinessRules.from(architecture, typeFacts.toClassRelations(), jigTypes);
        ServiceMethods serviceMethods = ServiceMethods.from(jigTypes, typeFacts.toMethodRelations());

        return new CategoryUsageDiagram(serviceMethods, businessRules);
    }

    public JigTypes jigTypes() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return typeFacts.jigTypes();
    }
}

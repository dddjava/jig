package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.jigdocument.implementation.BusinessRuleRelationDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.PackageRelationDiagram;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigLogger;
import org.dddjava.jig.domain.model.jigdocument.stationery.Warning;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFacts;
import org.springframework.stereotype.Service;

/**
 * 依存関係サービス
 */
@Service
public class DependencyService {

    final JigLogger jigLogger;
    final BusinessRuleService businessRuleService;
    final JigSourceRepository jigSourceRepository;

    public DependencyService(BusinessRuleService businessRuleService, JigLogger jigLogger, JigSourceRepository jigSourceRepository) {
        this.businessRuleService = businessRuleService;
        this.jigLogger = jigLogger;
        this.jigSourceRepository = jigSourceRepository;
    }

    /**
     * パッケージの関連を取得する
     */
    public PackageRelationDiagram packageDependencies() {
        BusinessRules businessRules = businessRuleService.businessRules();

        if (businessRules.empty()) {
            jigLogger.warn(Warning.ビジネスルールが見つからないので出力されない通知);
            return PackageRelationDiagram.empty();
        }

        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        ClassRelations classRelations = typeFacts.toClassRelations();
        PackageRelations packageRelations = PackageRelations.fromClassRelations(classRelations);

        return new PackageRelationDiagram(businessRules.identifiers().packageIdentifiers(), packageRelations, classRelations);
    }

    /**
     * ビジネスルールの関連を取得する
     */
    public BusinessRuleRelationDiagram businessRuleNetwork() {
        return new BusinessRuleRelationDiagram(businessRuleService.businessRules());
    }
}

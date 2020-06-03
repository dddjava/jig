package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocumenter.JigLogger;
import org.dddjava.jig.domain.model.jigdocumenter.Warning;
import org.dddjava.jig.domain.model.jigdocumenter.diagram.BusinessRuleRelationDiagram;
import org.dddjava.jig.domain.model.jigdocumenter.diagram.PackageRelationDiagram;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.jigsource.jigloader.RelationsFactory;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.springframework.stereotype.Service;

/**
 * 依存関係サービス
 */
@Service
public class DependencyService {

    JigLogger jigLogger;
    BusinessRuleService businessRuleService;

    public DependencyService(BusinessRuleService businessRuleService, JigLogger jigLogger) {
        this.businessRuleService = businessRuleService;
        this.jigLogger = jigLogger;
    }

    /**
     * パッケージの関連を取得する
     */
    public PackageRelationDiagram packageDependencies(AnalyzedImplementation analyzedImplementation) {
        BusinessRules businessRules = businessRuleService.businessRules(analyzedImplementation);

        if (businessRules.empty()) {
            jigLogger.warn(Warning.ビジネスルールが見つからないので出力されない通知);
            return PackageRelationDiagram.empty();
        }

        ClassRelations classRelations = RelationsFactory.createClassRelations(analyzedImplementation.typeByteCodes());
        PackageRelations packageRelations = PackageRelations.fromClassRelations(classRelations);

        return new PackageRelationDiagram(businessRules.identifiers().packageIdentifiers(), packageRelations, classRelations);
    }

    /**
     * ビジネスルールの関連を取得する
     */
    public BusinessRuleRelationDiagram businessRuleNetwork(AnalyzedImplementation analyzedImplementation) {
        BusinessRuleRelationDiagram businessRuleRelationDiagram = new BusinessRuleRelationDiagram(
                businessRuleService.businessRules(analyzedImplementation),
                RelationsFactory.createClassRelations(analyzedImplementation.typeByteCodes()));
        return businessRuleRelationDiagram;
    }
}

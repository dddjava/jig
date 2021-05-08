package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.documents.stationery.JigLogger;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.springframework.stereotype.Service;

/**
 * 依存関係サービス
 */
@Service
public class DependencyService {

    final JigLogger jigLogger;
    final BusinessRuleService businessRuleService;

    public DependencyService(BusinessRuleService businessRuleService, JigLogger jigLogger) {
        this.businessRuleService = businessRuleService;
        this.jigLogger = jigLogger;
    }

    /**
     * パッケージの関連を取得する
     */
    public PackageRelationDiagram packageDependencies() {
        BusinessRules businessRules = businessRules();

        if (businessRules.empty()) {
            jigLogger.warn(Warning.ビジネスルールが見つからないので出力されない通知);
            return PackageRelationDiagram.empty();
        }

        return new PackageRelationDiagram(businessRules.identifiers().packageIdentifiers(), businessRules.classRelations());
    }

    public BusinessRules businessRules() {
        return businessRuleService.businessRules();
    }
}

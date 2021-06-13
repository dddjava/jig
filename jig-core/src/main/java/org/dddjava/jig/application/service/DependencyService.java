package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 依存関係サービス
 */
@Service
public class DependencyService {

    static Logger logger = LoggerFactory.getLogger(DependencyService.class);
    final BusinessRuleService businessRuleService;

    public DependencyService(BusinessRuleService businessRuleService) {
        this.businessRuleService = businessRuleService;
    }

    /**
     * パッケージの関連を取得する
     */
    public PackageRelationDiagram packageDependencies() {
        BusinessRules businessRules = businessRules();

        if (businessRules.empty()) {
            logger.warn(Warning.ビジネスルールが見つからないので出力されない通知.localizedMessage());
            return PackageRelationDiagram.empty();
        }

        return new PackageRelationDiagram(businessRules.identifiers().packageIdentifiers(), businessRules.classRelations());
    }

    public BusinessRules businessRules() {
        return businessRuleService.businessRules();
    }
}

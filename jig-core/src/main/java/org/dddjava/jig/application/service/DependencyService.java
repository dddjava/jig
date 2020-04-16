package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocument.JigLogger;
import org.dddjava.jig.domain.model.jigdocument.Warning;
import org.dddjava.jig.domain.model.jigloaded.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageRelations;
import org.dddjava.jig.domain.model.jigmodel.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigpresentation.businessrule.BusinessRuleRelations;
import org.dddjava.jig.domain.model.jigpresentation.package_.PackageNetwork;
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
    public PackageNetwork packageDependencies(AnalyzedImplementation analyzedImplementation) {
        BusinessRules businessRules = businessRuleService.businessRules(analyzedImplementation);

        if (businessRules.empty()) {
            jigLogger.warn(Warning.ビジネスルールが見つからないので出力されない通知);
            return PackageNetwork.empty();
        }

        ClassRelations classRelations = new ClassRelations(analyzedImplementation.typeByteCodes());
        PackageRelations packageRelations = PackageRelations.fromClassRelations(classRelations);

        return new PackageNetwork(businessRules.identifiers().packageIdentifiers(), packageRelations, classRelations);
    }

    /**
     * ビジネスルールの関連を取得する
     */
    public BusinessRuleRelations businessRuleNetwork(AnalyzedImplementation analyzedImplementation) {
        BusinessRuleRelations businessRuleRelations = new BusinessRuleRelations(
                businessRuleService.businessRules(analyzedImplementation),
                new ClassRelations(analyzedImplementation.typeByteCodes()));
        return businessRuleRelations;
    }
}

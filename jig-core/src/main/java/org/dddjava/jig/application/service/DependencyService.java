package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.interpret.structure.PackageStructure;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifiers;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.interpret.notice.Warning;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageRelations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 依存関係サービス
 */
@Service
public class DependencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

    BusinessRuleService businessRuleService;

    public DependencyService(BusinessRuleService businessRuleService) {
        this.businessRuleService = businessRuleService;
    }

    /**
     * パッケージ構造を取得する
     */
    public PackageStructure packageStructure(AnalyzedImplementation analyzedImplementation) {
        PackageIdentifiers packageIdentifiers = analyzedImplementation.typeByteCodes().types().packages();
        return PackageStructure.from(packageIdentifiers);
    }

    /**
     * パッケージの関連を取得する
     */
    public PackageNetwork packageDependencies(AnalyzedImplementation analyzedImplementation) {
        BusinessRules businessRules = businessRuleService.businessRules(analyzedImplementation);

        if (businessRules.empty()) {
            LOGGER.warn(Warning.ビジネスルールが見つからないので出力されない通知.text());
            return PackageNetwork.empty();
        }

        ClassRelations classRelations = new ClassRelations(analyzedImplementation.typeByteCodes());
        PackageRelations packageRelations = PackageRelations.fromClassRelations(classRelations);

        return new PackageNetwork(businessRules.identifiers().packageIdentifiers(), packageRelations, classRelations);
    }

    /**
     * ビジネスルールの関連を取得する
     */
    public BusinessRuleNetwork businessRuleNetwork(AnalyzedImplementation analyzedImplementation) {
        BusinessRuleNetwork businessRuleNetwork = new BusinessRuleNetwork(
                businessRuleService.businessRules(analyzedImplementation),
                new ClassRelations(analyzedImplementation.typeByteCodes()));
        return businessRuleNetwork;
    }
}

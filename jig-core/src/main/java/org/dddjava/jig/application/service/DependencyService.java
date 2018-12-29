package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.configuration.ConfigurationContext;
import org.dddjava.jig.domain.model.declaration.namespace.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.networks.packages.PackageNetwork;
import org.dddjava.jig.domain.model.networks.packages.PackageRelations;
import org.dddjava.jig.domain.model.networks.type.TypeRelations;
import org.dddjava.jig.domain.type.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 依存関係サービス
 */
@Service
public class DependencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

    ConfigurationContext configurationContext;
    BusinessRuleService businessRuleService;

    public DependencyService(ConfigurationContext configurationContext, BusinessRuleService businessRuleService) {
        this.configurationContext = configurationContext;
        this.businessRuleService = businessRuleService;
    }

    public AllPackageIdentifiers allPackageIdentifiers(TypeByteCodes typeByteCodes) {
        PackageIdentifiers packageIdentifiers = typeByteCodes.types().packages();
        return packageIdentifiers.allPackageIdentifiers();
    }

    /**
     * パッケージ依存を取得する
     */
    public PackageNetwork packageDependencies(TypeByteCodes typeByteCodes) {
        LOGGER.info("パッケージ依存情報を取得します");

        BusinessRules businessRules = businessRuleService.businessRules(typeByteCodes.types());

        if (businessRules.empty()) {
            LOGGER.warn(Warning.モデル検出異常.with(configurationContext));
            return new PackageNetwork(new PackageIdentifiers(Collections.emptyList()), new PackageRelations(Collections.emptyList()));
        }

        PackageRelations packageRelations = new TypeRelations(typeByteCodes).packageDependencies();

        PackageNetwork packageNetwork = new PackageNetwork(businessRules.identifiers().packageIdentifiers(), packageRelations);
        showDepth(packageNetwork);

        return packageNetwork;
    }

    /**
     * 深度ごとの関連数をログ出力する
     */
    private void showDepth(PackageNetwork packageNetwork) {
        PackageDepth maxDepth = packageNetwork.allPackages().maxDepth();
        PackageRelations packageRelations = packageNetwork.packageDependencies();

        LOGGER.info("最大深度: {}", maxDepth.value());
        for (PackageDepth depth : maxDepth.surfaceList()) {
            PackageRelations dependencies = packageRelations.applyDepth(depth);
            LOGGER.info("深度 {} の関連数: {} ", depth.value(), dependencies.number().asText());
        }
    }

    public BusinessRuleNetwork businessRuleNetwork(TypeByteCodes typeByteCodes) {
        BusinessRuleNetwork businessRuleNetwork = new BusinessRuleNetwork(
                businessRuleService.businessRules(typeByteCodes.types()),
                new TypeRelations(typeByteCodes));
        return businessRuleNetwork;
    }
}

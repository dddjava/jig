package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.basic.Warning;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifiers;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.networks.PackageDependencies;
import org.dddjava.jig.domain.model.networks.PackageNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 依存関係サービス
 */
@Service
public class DependencyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyService.class);

    @Autowired
    Environment environment;

    /**
     * パッケージ依存を取得する
     */
    public PackageNetwork packageDependencies(ProjectData projectData) {
        LOGGER.info("パッケージ依存情報を取得します");
        TypeIdentifiers availableTypes = projectData.characterizedTypes().stream()
                .filter(Characteristic.MODEL)
                .typeIdentifiers();

        if (availableTypes.empty()) {
            LOGGER.warn(Warning.モデル検出異常.textWithSpringEnvironment(environment));
            return new PackageNetwork(new PackageIdentifiers(Collections.emptyList()), new PackageDependencies(Collections.emptyList()));
        }

        PackageDependencies packageDependencies = projectData.typeDependencies()
                .toPackageDependenciesWith(availableTypes);

        PackageNetwork packageNetwork = new PackageNetwork(availableTypes.packageIdentifiers(), packageDependencies);
        showDepth(packageNetwork);

        return packageNetwork;
    }

    /**
     * 深度ごとの関連数をログ出力する
     */
    private void showDepth(PackageNetwork packageNetwork) {
        PackageDepth maxDepth = packageNetwork.allPackages().maxDepth();
        PackageDependencies packageDependencies = packageNetwork.packageDependencies();

        LOGGER.info("最大深度: {}", maxDepth.value());
        for (PackageDepth depth : maxDepth.surfaceList()) {
            PackageDependencies dependencies = packageDependencies.applyDepth(depth);
            LOGGER.info("深度 {} の関連数: {} ", depth.value(), dependencies.number().asText());
        }
    }
}

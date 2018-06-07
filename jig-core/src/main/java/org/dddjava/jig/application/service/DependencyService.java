package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.basic.Warning;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifiers;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.networks.PackageDependencies;
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
    public PackageDependencies packageDependencies(PackageDepth packageDepth, ProjectData projectData) {
        LOGGER.info("パッケージ依存情報を取得します(深度: {})", packageDepth.value());
        TypeIdentifiers modelTypes = projectData.characterizedTypes().stream()
                .filter(Characteristic.MODEL)
                .typeIdentifiers();

        if (modelTypes.empty()) {
            LOGGER.warn(Warning.モデル検出異常.textWithSpringEnvironment(environment));
            return new PackageDependencies(Collections.emptyList(), new PackageIdentifiers(Collections.emptyList()));
        }

        PackageDependencies packageDependencies = projectData.typeDependencies()
                .toPackageDependenciesWith(modelTypes);

        showDepth(packageDependencies);

        return packageDependencies.applyDepth(packageDepth);
    }

    /**
     * 深度ごとの関連数をログ出力する
     */
    private void showDepth(PackageDependencies packageDependencies) {
        PackageDepth maxDepth = packageDependencies.allPackages().maxDepth();

        LOGGER.info("最大深度: {}", maxDepth.value());
        for (PackageDepth depth : maxDepth.surfaceList()) {
            PackageDependencies dependencies = packageDependencies.applyDepth(depth);
            LOGGER.info("深度 {} の関連数: {} ", depth.value(), dependencies.number().asText());
        }
    }
}

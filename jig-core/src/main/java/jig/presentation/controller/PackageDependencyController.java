package jig.presentation.controller;

import jig.application.service.DependencyService;
import jig.domain.model.identifier.namespace.PackageDepth;
import jig.domain.model.relation.dependency.PackageDependencies;
import jig.presentation.view.JigViewResolver;
import jig.presentation.view.LocalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class PackageDependencyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDependencyController.class);

    DependencyService dependencyService;
    JigViewResolver jigViewResolver;

    public PackageDependencyController(DependencyService dependencyService, JigViewResolver jigViewResolver) {
        this.dependencyService = dependencyService;
        this.jigViewResolver = jigViewResolver;
    }

    public LocalView packageDependency(PackageDepth depth) {
        LOGGER.info("パッケージ依存ダイアグラムを出力します");
        PackageDependencies packageDependencies = dependencyService.packageDependencies(depth);
        return jigViewResolver.dependencyWriter(packageDependencies);
    }
}

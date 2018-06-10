package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.networks.PackageNetwork;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class PackageDependencyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDependencyController.class);

    DependencyService dependencyService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public PackageDependencyController(DependencyService dependencyService, GlossaryService glossaryService, ViewResolver viewResolver) {
        this.dependencyService = dependencyService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    public JigModelAndView<PackageNetwork> packageDependency(PackageDepth depth, ProjectData projectData) {
        LOGGER.info("パッケージ依存ダイアグラムを出力します");
        PackageNetwork packageNetwork = dependencyService.packageDependencies(projectData);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(packageNetwork.applyDepth(depth), viewResolver.dependencyWriter(japaneseNameFinder));
    }
}

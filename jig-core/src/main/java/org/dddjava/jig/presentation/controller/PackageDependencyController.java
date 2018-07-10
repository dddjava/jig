package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.networks.packages.PackageNetwork;
import org.dddjava.jig.domain.model.networks.packages.PackageNetworks;
import org.dddjava.jig.domain.model.report.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

@Controller
public class PackageDependencyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDependencyController.class);

    private final PackageDepth depth;

    DependencyService dependencyService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public PackageDependencyController(DependencyService dependencyService,
                                       GlossaryService glossaryService,
                                       ViewResolver viewResolver,
                                       @Value("${depth:-1}") int packageDepth) {
        this.dependencyService = dependencyService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
        this.depth = new PackageDepth(packageDepth);
    }

    @DocumentMapping(JigDocument.PackageDependency)
    public JigModelAndView<PackageNetworks> packageDependency(ProjectData projectData) {
        LOGGER.info("パッケージ依存ダイアグラムを出力します");
        PackageNetwork packageNetwork = dependencyService.packageDependencies(projectData);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(new PackageNetworks(packageNetwork, depth), viewResolver.dependencyWriter(japaneseNameFinder));
    }
}

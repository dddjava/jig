package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.declaration.namespace.PackageDepth;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.networks.packages.PackageNetwork;
import org.dddjava.jig.domain.model.networks.packages.PackageNetworks;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class PackageDependencyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageDependencyController.class);

    private final PackageDepth packageDepth;

    DependencyService dependencyService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public PackageDependencyController(DependencyService dependencyService,
                                       GlossaryService glossaryService,
                                       ViewResolver viewResolver,
                                       PackageDepth packageDepth) {
        this.dependencyService = dependencyService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
        this.packageDepth = packageDepth;
    }

    @DocumentMapping(JigDocument.PackageRelationDiagram)
    public JigModelAndView<PackageNetworks> packageDependency(TypeByteCodes typeByteCodes) {
        LOGGER.info("パッケージ依存ダイアグラムを出力します");
        PackageNetwork packageNetwork = dependencyService.packageDependencies(typeByteCodes);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(new PackageNetworks(packageNetwork, packageDepth), viewResolver.dependencyWriter(japaneseNameFinder));
    }

    @DocumentMapping(JigDocument.BusinessRuleRelationDiagram)
    public JigModelAndView<BusinessRuleNetwork> businessRuleRelation(TypeByteCodes typeByteCodes) {
        BusinessRuleNetwork network = dependencyService.businessRuleNetwork(typeByteCodes);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(network, viewResolver.businessRuleNetworkWriter(japaneseNameFinder));
    }
}

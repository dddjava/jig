package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.JapaneseNameFinder;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.PackageNetwork;
import org.dddjava.jig.domain.model.implementation.analyzed.networks.packages.PackageNetworks;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PackageDependencyController {

    DependencyService dependencyService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public PackageDependencyController(DependencyService dependencyService,
                                       GlossaryService glossaryService,
                                       ViewResolver viewResolver) {
        this.dependencyService = dependencyService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.PackageRelationDiagram)
    public JigModelAndView<PackageNetworks> packageDependency(AnalyzedImplementation implementations) {
        PackageNetwork packageNetwork = dependencyService.packageDependencies(implementations.typeByteCodes());
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(new PackageNetworks(packageNetwork), viewResolver.dependencyWriter(japaneseNameFinder));
    }

    @DocumentMapping(JigDocument.BusinessRuleRelationDiagram)
    public JigModelAndView<BusinessRuleNetwork> businessRuleRelation(AnalyzedImplementation implementations) {
        BusinessRuleNetwork network = dependencyService.businessRuleNetwork(implementations.typeByteCodes());
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(network, viewResolver.businessRuleNetworkWriter(japaneseNameFinder));
    }

    @DocumentMapping(JigDocument.PackageTreeDiagram)
    public JigModelAndView<AllPackageIdentifiers> packageTreeDiagram(AnalyzedImplementation implementations) {
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        AllPackageIdentifiers packages = dependencyService.allPackageIdentifiers(implementations.typeByteCodes());
        return new JigModelAndView<>(packages, viewResolver.packageTreeWriter(japaneseNameFinder));
    }
}

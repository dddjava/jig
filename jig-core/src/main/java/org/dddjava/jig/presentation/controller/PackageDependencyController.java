package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.declaration.package_.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageNetwork;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class PackageDependencyController {

    DependencyService dependencyService;
    AliasService aliasService;
    ViewResolver viewResolver;

    public PackageDependencyController(DependencyService dependencyService,
                                       AliasService aliasService,
                                       ViewResolver viewResolver) {
        this.dependencyService = dependencyService;
        this.aliasService = aliasService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.PackageRelationDiagram)
    public JigModelAndView<PackageNetwork> packageDependency(AnalyzedImplementation implementations) {
        PackageNetwork packageNetwork = dependencyService.packageDependencies(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(packageNetwork, viewResolver.dependencyWriter(aliasFinder));
    }

    @DocumentMapping(JigDocument.BusinessRuleRelationDiagram)
    public JigModelAndView<BusinessRuleNetwork> businessRuleRelation(AnalyzedImplementation implementations) {
        BusinessRuleNetwork network = dependencyService.businessRuleNetwork(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(network, viewResolver.businessRuleNetworkWriter(aliasFinder));
    }

    @DocumentMapping(JigDocument.PackageTreeDiagram)
    public JigModelAndView<AllPackageIdentifiers> packageTreeDiagram(AnalyzedImplementation implementations) {
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        AllPackageIdentifiers packages = dependencyService.allPackageIdentifiers(implementations);
        return new JigModelAndView<>(packages, viewResolver.packageTreeWriter(aliasFinder));
    }
}

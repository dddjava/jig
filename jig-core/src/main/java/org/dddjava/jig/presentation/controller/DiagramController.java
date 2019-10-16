package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.declaration.package_.AllPackageIdentifiers;
import org.dddjava.jig.domain.model.diagram.JigDocument;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DiagramController {

    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    ApplicationService applicationService;

    AliasService aliasService;
    ViewResolver viewResolver;

    public DiagramController(DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService, AliasService aliasService, ViewResolver viewResolver) {
        this.dependencyService = dependencyService;
        this.businessRuleService = businessRuleService;
        this.applicationService = applicationService;
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
        return new JigModelAndView<>(network, viewResolver.businessRuleRelationWriter(aliasFinder));
    }

    @DocumentMapping(JigDocument.PackageTreeDiagram)
    public JigModelAndView<AllPackageIdentifiers> packageTreeDiagram(AnalyzedImplementation implementations) {
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        AllPackageIdentifiers packages = dependencyService.allPackageIdentifiers(implementations);
        return new JigModelAndView<>(packages, viewResolver.packageTreeWriter(aliasFinder));
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public JigModelAndView<CategoryAngles> enumUsage(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(categoryAngles, viewResolver.enumUsage(aliasFinder));
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public JigModelAndView<CategoryAngles> categories(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(categoryAngles, viewResolver.categories(aliasFinder));
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy(aliasFinder));
    }

    @DocumentMapping(JigDocument.BooleanServiceDiagram)
    public JigModelAndView<?> booleanServiceTrace(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace(aliasFinder));
    }
}

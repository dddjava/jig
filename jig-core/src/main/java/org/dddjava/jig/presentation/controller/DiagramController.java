package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.diagram.JigDocument;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.interpret.structure.PackageStructure;
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

    ViewResolver viewResolver;

    public DiagramController(DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService, ViewResolver viewResolver) {
        this.dependencyService = dependencyService;
        this.businessRuleService = businessRuleService;
        this.applicationService = applicationService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.PackageRelationDiagram)
    public JigModelAndView<PackageNetwork> packageDependency(AnalyzedImplementation implementations) {
        PackageNetwork packageNetwork = dependencyService.packageDependencies(implementations);
        return new JigModelAndView<>(packageNetwork, viewResolver.dependencyWriter());
    }

    @DocumentMapping(JigDocument.BusinessRuleRelationDiagram)
    public JigModelAndView<BusinessRuleNetwork> businessRuleRelation(AnalyzedImplementation implementations) {
        BusinessRuleNetwork network = dependencyService.businessRuleNetwork(implementations);
        return new JigModelAndView<>(network, viewResolver.businessRuleRelationWriter());
    }

    @DocumentMapping(JigDocument.PackageTreeDiagram)
    public JigModelAndView<PackageStructure> packageTreeDiagram(AnalyzedImplementation implementations) {
        PackageStructure packages = dependencyService.allPackageIdentifiers(implementations);
        return new JigModelAndView<>(packages, viewResolver.packageTreeWriter());
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public JigModelAndView<CategoryAngles> enumUsage(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations);
        return new JigModelAndView<>(categoryAngles, viewResolver.enumUsage());
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public JigModelAndView<CategoryAngles> categories(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations);
        return new JigModelAndView<>(categoryAngles, viewResolver.categories());
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy());
    }

    @DocumentMapping(JigDocument.BooleanServiceDiagram)
    public JigModelAndView<?> booleanServiceTrace(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace());
    }
}

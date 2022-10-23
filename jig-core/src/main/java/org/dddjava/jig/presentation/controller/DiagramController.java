package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.documents.diagrams.*;
import org.springframework.stereotype.Controller;

@Controller
public class DiagramController {

    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    ApplicationService applicationService;

    public DiagramController(DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService) {
        this.dependencyService = dependencyService;
        this.businessRuleService = businessRuleService;
        this.applicationService = applicationService;
    }

    public PackageRelationDiagram packageDependency() {
        return dependencyService.packageDependencies();
    }

    public ClassRelationDiagram businessRuleRelation() {
        return new ClassRelationDiagram(dependencyService.businessRules());
    }

    public ClassRelationCoreDiagram coreBusinessRuleRelation() {
        return new ClassRelationCoreDiagram(new ClassRelationDiagram(dependencyService.businessRules()));
    }

    public ClassRelationConcentrateDiagram overconcentrationBusinessRuleRelation() {
        return new ClassRelationConcentrateDiagram(dependencyService.businessRules());
    }

    public CategoryUsageDiagram categoryUsage() {
        return businessRuleService.categoryUsages();
    }

    public CategoryDiagram categories() {
        return businessRuleService.categories();
    }

    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy() {
        return applicationService.serviceMethodCallHierarchy();
    }

    public ArchitectureDiagram architecture() {
        return applicationService.architectureDiagram();
    }

    public ComponentRelationDiagram componentRelation() {
        return applicationService.componentRelationDiagram();
    }

    public CompositeUsecaseDiagram useCaseDiagram() {
        return new CompositeUsecaseDiagram(applicationService.serviceAngles());
    }
}

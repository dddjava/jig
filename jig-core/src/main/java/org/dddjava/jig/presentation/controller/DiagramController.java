package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.documents.diagrams.*;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
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

    @DocumentMapping(JigDocument.PackageRelationDiagram)
    public PackageRelationDiagram packageDependency() {
        return dependencyService.packageDependencies();
    }

    @DocumentMapping(JigDocument.BusinessRuleRelationDiagram)
    public ClassRelationDiagram businessRuleRelation() {
        return new ClassRelationDiagram(dependencyService.businessRules());
    }

    @DocumentMapping(JigDocument.CoreBusinessRuleRelationDiagram)
    public ClassRelationCoreDiagram coreBusinessRuleRelation() {
        return new ClassRelationCoreDiagram(new ClassRelationDiagram(dependencyService.businessRules()));
    }

    @DocumentMapping(JigDocument.OverconcentrationBusinessRuleDiagram)
    public ClassRelationConcentrateDiagram overconcentrationBusinessRuleRelation() {
        return new ClassRelationConcentrateDiagram(dependencyService.businessRules());
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public CategoryUsageDiagram categoryUsage() {
        return businessRuleService.categoryUsages();
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public CategoryDiagram categories() {
        return businessRuleService.categories();
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy() {
        return applicationService.serviceMethodCallHierarchy();
    }

    @DocumentMapping(JigDocument.ArchitectureDiagram)
    public ArchitectureDiagram architecture() {
        return applicationService.architectureDiagram();
    }

    @DocumentMapping(JigDocument.ComponentRelationDiagram)
    public ComponentRelationDiagram componentRelation() {
        return applicationService.componentRelationDiagram();
    }

    @DocumentMapping(JigDocument.CompositeUsecaseDiagram)
    public CompositeUsecaseDiagram useCaseDiagram() {
        return new CompositeUsecaseDiagram(applicationService.serviceAngles());
    }
}

package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.implementation.BusinessRuleRelationDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.ArchitectureDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.Categories;
import org.dddjava.jig.domain.model.jigdocument.specification.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.PackageRelationDiagram;
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
    public BusinessRuleRelationDiagram businessRuleRelation() {
        return dependencyService.businessRuleNetwork();
    }

    @DocumentMapping(JigDocument.CoreBusinessRuleRelationDiagram)
    public BusinessRuleRelationDiagram coreBusinessRuleRelation() {
        return dependencyService.businessRuleNetwork();
    }

    @DocumentMapping(JigDocument.OverconcentrationBusinessRuleDiagram)
    public BusinessRuleRelationDiagram overconcentrationBusinessRuleRelation() {
        return dependencyService.businessRuleNetwork();
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public CategoryUsageDiagram categoryUsage() {
        return businessRuleService.categoryUsages();
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public Categories categories() {
        return businessRuleService.categories();
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy() {
        return applicationService.serviceMethodCallHierarchy();
    }

    @DocumentMapping(JigDocument.ArchitectureDiagram)
    public ArchitectureDiagram architecture() {
        return new ArchitectureDiagram(applicationService.buildingBlockRelations());
    }

    @DocumentMapping(JigDocument.CompositeUsecaseDiagram)
    public CompositeUsecaseDiagram useCaseDiagram() {
        return new CompositeUsecaseDiagram(applicationService.serviceAngles());
    }
}

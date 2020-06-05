package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.implementation.BusinessRuleRelationDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.ArchitectureDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.CategoryDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.CompositeUsecaseDiagram;
import org.dddjava.jig.domain.model.jigdocument.specification.PackageRelationDiagram;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.ArchitectureFactory;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class DiagramController {

    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    ApplicationService applicationService;
    ArchitectureFactory architectureFactory;

    public DiagramController(DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService, ArchitectureFactory architectureFactory) {
        this.dependencyService = dependencyService;
        this.businessRuleService = businessRuleService;
        this.applicationService = applicationService;
        this.architectureFactory = architectureFactory;
    }

    @DocumentMapping(JigDocument.PackageRelationDiagram)
    public PackageRelationDiagram packageDependency(AnalyzedImplementation implementations) {
        return dependencyService.packageDependencies(implementations);
    }

    @DocumentMapping(JigDocument.BusinessRuleRelationDiagram)
    public BusinessRuleRelationDiagram businessRuleRelation(AnalyzedImplementation implementations) {
        return dependencyService.businessRuleNetwork(implementations);
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public CategoryUsageDiagram categoryUsage(AnalyzedImplementation implementations) {
        return businessRuleService.categoryUsages(implementations);
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public CategoryDiagram categories(AnalyzedImplementation implementations) {
        return businessRuleService.categories(implementations);
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        return applicationService.serviceMethodCallHierarchy(implementations);
    }

    @DocumentMapping(JigDocument.ArchitectureDiagram)
    public ArchitectureDiagram architecture(AnalyzedImplementation implementations) {
        return new ArchitectureDiagram(applicationService.buildingBlockRelations(implementations));
    }

    @DocumentMapping(JigDocument.CompositeUsecaseDiagram)
    public CompositeUsecaseDiagram useCaseDiagram(AnalyzedImplementation analyzedImplementation) {
        return new CompositeUsecaseDiagram(applicationService.serviceAngles(analyzedImplementation));
    }
}

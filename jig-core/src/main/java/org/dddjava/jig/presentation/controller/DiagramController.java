package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.architectures.ArchitectureAngle;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.diagram.JigDocument;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.interpret.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.interpret.structure.PackageStructure;
import org.dddjava.jig.domain.model.services.ServiceAngles;
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
    public PackageNetwork packageDependency(AnalyzedImplementation implementations) {
        return dependencyService.packageDependencies(implementations);
    }

    @DocumentMapping(JigDocument.BusinessRuleRelationDiagram)
    public BusinessRuleNetwork businessRuleRelation(AnalyzedImplementation implementations) {
        return dependencyService.businessRuleNetwork(implementations);
    }

    @DocumentMapping(JigDocument.PackageTreeDiagram)
    public PackageStructure packageTreeDiagram(AnalyzedImplementation implementations) {
        return dependencyService.packageStructure(implementations);
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public CategoryAngles enumUsage(AnalyzedImplementation implementations) {
        return businessRuleService.categories(implementations);
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public CategoryAngles categories(AnalyzedImplementation implementations) {
        return businessRuleService.categories(implementations);
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public ServiceAngles serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        return applicationService.serviceAngles(implementations);
    }

    @DocumentMapping(JigDocument.BooleanServiceDiagram)
    public ServiceAngles booleanServiceTrace(AnalyzedImplementation implementations) {
        return applicationService.serviceAngles(implementations);
    }

    @DocumentMapping(JigDocument.ArchitectureDiagram)
    public ArchitectureAngle architecture(AnalyzedImplementation implementations) {
        return new ArchitectureAngle(implementations);
    }
}

package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.repository.ArchitectureFactory;
import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigloaded.relation.packages.PackageNetwork;
import org.dddjava.jig.domain.model.jigmodel.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRuleNetwork;
import org.dddjava.jig.domain.model.jigpresentation.architectures.ArchitectureAngle;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.domain.model.jigpresentation.structure.PackageStructure;
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

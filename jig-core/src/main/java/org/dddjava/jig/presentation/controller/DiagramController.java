package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigmodel.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureFactory;
import org.dddjava.jig.domain.model.jigpresentation.architectures.ArchitectureAngle;
import org.dddjava.jig.domain.model.jigpresentation.businessrule.BusinessRuleRelations;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryAngles;
import org.dddjava.jig.domain.model.jigpresentation.categories.CategoryUsages;
import org.dddjava.jig.domain.model.jigpresentation.package_.PackageNetwork;
import org.dddjava.jig.domain.model.jigpresentation.servicecall.ServiceMethodCallHierarchy;
import org.dddjava.jig.domain.model.jigpresentation.smell.ReturnBooleanTrace;
import org.dddjava.jig.domain.model.jigpresentation.usecase.UseCaseAndFellowsAngle;
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
    public BusinessRuleRelations businessRuleRelation(AnalyzedImplementation implementations) {
        return dependencyService.businessRuleNetwork(implementations);
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public CategoryUsages enumUsage(AnalyzedImplementation implementations) {
        return businessRuleService.categoryUsages(implementations);
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public CategoryAngles categories(AnalyzedImplementation implementations) {
        return businessRuleService.categories(implementations);
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public ServiceMethodCallHierarchy serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        return applicationService.serviceMethodCallHierarchy(implementations);
    }

    @DocumentMapping(JigDocument.BooleanServiceDiagram)
    public ReturnBooleanTrace booleanServiceTrace(AnalyzedImplementation implementations) {
        return new ReturnBooleanTrace(applicationService.serviceAngles(implementations).list());
    }

    @DocumentMapping(JigDocument.ArchitectureDiagram)
    public ArchitectureAngle architecture(AnalyzedImplementation implementations) {
        return new ArchitectureAngle(implementations, architectureFactory);
    }

    @DocumentMapping(JigDocument.UseCaseAndFellowsDiagram)
    public UseCaseAndFellowsAngle useCaseDiagram(AnalyzedImplementation analyzedImplementation) {
        return new UseCaseAndFellowsAngle(applicationService.serviceAngles(analyzedImplementation));
    }
}

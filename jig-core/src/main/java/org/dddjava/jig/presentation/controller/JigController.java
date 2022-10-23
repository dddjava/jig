package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.documents.diagrams.*;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.models.applications.backends.DatasourceAngles;
import org.dddjava.jig.domain.model.models.applications.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.models.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.services.StringComparingMethodList;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRulePackages;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.domains.collections.JigCollectionTypes;
import org.dddjava.jig.domain.model.models.domains.validations.Validations;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.business_rule.*;

public class JigController {

    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    ApplicationService applicationService;

    public JigController(DependencyService dependencyService, BusinessRuleService businessRuleService, ApplicationService applicationService) {
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

    public ModelReports termList() {
        Terms terms = businessRuleService.terms();
        return new ModelReports(new ModelReport<>(terms.list(), TermReport::new, TermReport.class));
    }

    public SummaryModel domainListHtml() {
        return SummaryModel.from(businessRuleService.businessRules());
    }

    public SummaryModel enumListHtml() {
        return SummaryModel.from(businessRuleService.categoryTypes(), businessRuleService.enumModels());
    }

    public SummaryModel schemaHtml() {
        return SummaryModel.from(businessRuleService.businessRules());
    }

    public ModelReports domainList() {
        return new ModelReports(
                packageReport(),
                businessRulesReport(),
                categoriesReport(),
                collectionsReport(),
                validateAnnotationReport(),
                smellReport()
        );
    }

    ModelReport<?> packageReport() {
        BusinessRulePackages businessRulePackages = businessRuleService.businessRules().businessRulePackages();
        return new ModelReport<>(businessRulePackages.list(), PackageReport::new, PackageReport.class);
    }

    ModelReport<?> businessRulesReport() {
        BusinessRules businessRules = businessRuleService.businessRules();
        return new ModelReport<>(businessRules.list(), businessRule -> new BusinessRuleReport(businessRule, businessRules), BusinessRuleReport.class);
    }

    ModelReport<?> collectionsReport() {
        JigCollectionTypes jigCollectionTypes = businessRuleService.collections();
        return new ModelReport<>(jigCollectionTypes.listJigType(),
                jigType -> new CollectionReport(jigType, jigCollectionTypes.classRelations()),
                CollectionReport.class);
    }

    ModelReport<?> categoriesReport() {
        CategoryDiagram categoryDiagram = businessRuleService.categories();
        return new ModelReport<>(categoryDiagram.list(), CategoryReport::new, CategoryReport.class);
    }

    ModelReport<?> validateAnnotationReport() {
        JigTypes jigTypes = businessRuleService.jigTypes();

        return new ModelReport<>(Validations.from(jigTypes).list(), ValidationReport::new, ValidationReport.class);
    }

    ModelReport<?> smellReport() {
        MethodSmellList angles = businessRuleService.methodSmells();
        return new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class);
    }


    public ModelReports applicationList() {
        return new ModelReports(
                controllerReport(),
                serviceReport(),
                datasourceReport(),
                stringComparingReport()
        );
    }

    public SummaryModel applicationSummary() {
        return SummaryModel.from(applicationService.serviceMethods());
    }

    ModelReport<?> controllerReport() {
        HandlerMethods handlerMethods = applicationService.controllerAngles();

        return new ModelReport<>(handlerMethods.list(),
                requestHandlerMethod -> new ControllerReport(requestHandlerMethod),
                ControllerReport.class);
    }

    ModelReport<?> serviceReport() {
        ServiceAngles serviceAngles = applicationService.serviceAngles();

        return new ModelReport<>(serviceAngles.list(),
                serviceAngle -> new ServiceReport(serviceAngle),
                ServiceReport.class);
    }

    ModelReport<?> datasourceReport() {
        DatasourceAngles datasourceAngles = applicationService.datasourceAngles();
        return new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class);
    }

    ModelReport<?> stringComparingReport() {
        StringComparingMethodList stringComparingMethodList = applicationService.stringComparing();
        return new ModelReport<>(stringComparingMethodList.list(), StringComparingReport::new, StringComparingReport.class);
    }
}

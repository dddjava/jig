package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.domain.model.documents.diagrams.*;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
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
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.presentation.view.handler.ModelAndView;
import org.dddjava.jig.presentation.view.html.HtmlView;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.business_rule.*;

public class JigController {

    private final DependencyService dependencyService;
    private final BusinessRuleService businessRuleService;
    private final ApplicationService applicationService;

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

    public ModelReports domainList() {
        MethodSmellList angles = businessRuleService.methodSmells();
        JigTypes jigTypes = businessRuleService.jigTypes();

        JigCollectionTypes jigCollectionTypes = businessRuleService.collections();
        CategoryDiagram categoryDiagram = businessRuleService.categories();
        BusinessRules businessRules = businessRuleService.businessRules();
        BusinessRulePackages businessRulePackages = businessRuleService.businessRules().businessRulePackages();
        return new ModelReports(
                new ModelReport<>(businessRulePackages.list(), PackageReport::new, PackageReport.class),
                new ModelReport<>(businessRules.list(),
                        businessRule -> new BusinessRuleReport(businessRule, businessRules),
                        BusinessRuleReport.class),
                new ModelReport<>(categoryDiagram.list(), CategoryReport::new, CategoryReport.class),
                new ModelReport<>(jigCollectionTypes.listJigType(),
                        jigType -> new CollectionReport(jigType, jigCollectionTypes.classRelations()),
                        CollectionReport.class),
                new ModelReport<>(Validations.from(jigTypes).list(), ValidationReport::new, ValidationReport.class),
                new ModelReport<>(angles.list(), MethodSmellReport::new, MethodSmellReport.class)
        );
    }

    public ModelReports applicationList() {
        ServiceAngles serviceAngles = applicationService.serviceAngles();

        DatasourceAngles datasourceAngles = applicationService.datasourceAngles();
        StringComparingMethodList stringComparingMethodList = applicationService.stringComparing();
        HandlerMethods handlerMethods = applicationService.controllerAngles();

        return new ModelReports(
                new ModelReport<>(handlerMethods.list(),
                        requestHandlerMethod -> new ControllerReport(requestHandlerMethod),
                        ControllerReport.class),
                new ModelReport<>(serviceAngles.list(),
                        serviceAngle -> new ServiceReport(serviceAngle),
                        ServiceReport.class),
                new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class),
                new ModelReport<>(stringComparingMethodList.list(), StringComparingReport::new, StringComparingReport.class)
        );
    }

    public ModelAndView mermaid() {
        ServiceAngles serviceAngles = applicationService.serviceAngles();

        record Entry(JigMethod jigMethod, String mermaidText) {
        }

        var model = serviceAngles.list()
                .stream()
                // どのServiceMethodにも使用されていないものだけを起点にする
                .filter(serviceAngle -> serviceAngle.userServiceMethods().empty())
                // どのServiceMethodも使用していないものは除外する
                .filter(serviceAngle -> !serviceAngle.usingServiceMethods().empty())
                .map(rootServiceMethod -> {
                    var key = rootServiceMethod.method().asSimpleText();

                    return new Entry(
                            rootServiceMethod.serviceMethod().method(),
                            serviceAngles.mermaidText(key)
                    );
                })
                .toList();

        return new ModelAndView(model, HtmlView.class);
    }

    public SummaryModel applicationSummary() {
        return SummaryModel.from(applicationService.serviceMethods());
    }

    public Object handle(JigDocument jigDocument) {
        return switch (jigDocument) {
            case BusinessRuleList -> domainList();
            case PackageRelationDiagram -> packageDependency();
            case BusinessRuleRelationDiagram -> businessRuleRelation();
            case OverconcentrationBusinessRuleDiagram -> overconcentrationBusinessRuleRelation();
            case CoreBusinessRuleRelationDiagram -> coreBusinessRuleRelation();
            case CategoryDiagram -> categories();
            case CategoryUsageDiagram -> categoryUsage();
            case ApplicationList -> applicationList();
            case ServiceMethodCallHierarchyDiagram -> serviceMethodCallHierarchy();
            case CompositeUsecaseDiagram -> useCaseDiagram();
            case ArchitectureDiagram -> architecture();
            case DomainSummary -> domainListHtml();
            case ApplicationSummary, UsecaseSummary -> applicationSummary();
            case EnumSummary -> enumListHtml();
            case TermTable -> businessRuleService.terms();
            case TermList -> termList();
            case Mermaid -> mermaid();
        };

    }
}

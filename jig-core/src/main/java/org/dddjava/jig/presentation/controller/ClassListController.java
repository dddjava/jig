package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.domain.model.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.decisions.DecisionAngle;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifierFormatter;
import org.dddjava.jig.domain.model.fact.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.repositories.DatasourceAngles;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.branch.DecisionReport;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class ClassListController {

    ConvertContext convertContext;
    ApplicationService applicationService;
    BusinessRuleService businessRuleService;

    public ClassListController(TypeIdentifierFormatter typeIdentifierFormatter,
                               AliasService aliasService,
                               ApplicationService applicationService,
                               BusinessRuleService businessRuleService) {
        this.convertContext = new ConvertContext(aliasService, typeIdentifierFormatter);
        this.applicationService = applicationService;
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.ApplicationList)
    public JigModelAndView<ModelReports> applicationList(AnalyzedImplementation implementations) {
        ModelReports modelReports = new ModelReports(
                controllerReport(implementations),
                serviceReport(implementations),
                datasourceReport(implementations)
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    @DocumentMapping(JigDocument.BranchList)
    public JigModelAndView<ModelReports> branchList(AnalyzedImplementation implementations) {
        DecisionAngles decisionAngles = applicationService.decision(implementations);
        ModelReports modelReports = new ModelReports(
                decisionReport(decisionAngles.listPresentations(), ApplicationLayer.PRESENTATION),
                decisionReport(decisionAngles.listApplications(), ApplicationLayer.APPLICATION),
                decisionReport(decisionAngles.listInfrastructures(), ApplicationLayer.INFRASTRUCTURE)
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    ModelReport<?> controllerReport(AnalyzedImplementation implementations) {
        ControllerMethods controllerMethods = applicationService.controllerAngles(implementations);
        ProgressAngles progressAngles = applicationService.progressAngles(implementations);

        return new ModelReport<>(controllerMethods.list(),
                requestHandlerMethod -> new ControllerReport(requestHandlerMethod, progressAngles.progressOf(requestHandlerMethod.method().declaration())),
                ControllerReport.class);
    }

    ModelReport<?> serviceReport(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);
        ProgressAngles progressAngles = applicationService.progressAngles(implementations);

        return new ModelReport<>(serviceAngles.list(),
                serviceAngle -> new ServiceReport(serviceAngle, progressAngles.progressOf(serviceAngle.method())),
                ServiceReport.class);
    }

    ModelReport<?> datasourceReport(AnalyzedImplementation implementations) {
        DatasourceAngles datasourceAngles = applicationService.datasourceAngles(implementations);
        return new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class);
    }

    ModelReport<?> decisionReport(List<DecisionAngle> decisionAngles, ApplicationLayer applicationLayer) {
        return new ModelReport<>(applicationLayer.name(), decisionAngles, DecisionReport::new, DecisionReport.class);
    }

}

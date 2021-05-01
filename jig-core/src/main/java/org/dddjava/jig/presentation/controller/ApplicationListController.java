package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.implementation.StringComparingMethodList;
import org.dddjava.jig.domain.model.jigdocument.summary.SummaryModel;
import org.dddjava.jig.domain.model.jigmodel.presentations.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.infrastructures.DatasourceAngles;
import org.dddjava.jig.domain.model.jigmodel.applications.ServiceAngles;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.business_rule.StringComparingReport;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationListController {

    ApplicationService applicationService;
    BusinessRuleService businessRuleService;

    public ApplicationListController(ApplicationService applicationService,
                                     BusinessRuleService businessRuleService) {
        this.applicationService = applicationService;
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.ApplicationList)
    public ModelReports applicationList() {
        return new ModelReports(
                controllerReport(),
                serviceReport(),
                datasourceReport(),
                stringComparingReport()
        );
    }

    @DocumentMapping(JigDocument.ApplicationSummary)
    public SummaryModel applicationSummary() {
        return SummaryModel.from(applicationService.serviceTypes());
    }

    ModelReport<?> controllerReport() {
        ControllerMethods controllerMethods = applicationService.controllerAngles();

        return new ModelReport<>(controllerMethods.list(),
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

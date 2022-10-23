package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.domain.model.documents.summaries.SummaryModel;
import org.dddjava.jig.domain.model.models.applications.backends.DatasourceAngles;
import org.dddjava.jig.domain.model.models.applications.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.models.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.services.StringComparingMethodList;
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

    public ApplicationListController(ApplicationService applicationService) {
        this.applicationService = applicationService;
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

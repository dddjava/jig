package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.domain.model.jigdocument.JigDocument;
import org.dddjava.jig.domain.model.jigmodel.applications.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.repositories.DatasourceAngles;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.smells.StringComparingCallerMethods;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.dddjava.jig.presentation.view.poi.ModelReportsPoiView;
import org.dddjava.jig.presentation.view.poi.report.ConvertContext;
import org.dddjava.jig.presentation.view.poi.report.ModelReport;
import org.dddjava.jig.presentation.view.poi.report.ModelReports;
import org.dddjava.jig.presentation.view.report.application.ControllerReport;
import org.dddjava.jig.presentation.view.report.application.RepositoryReport;
import org.dddjava.jig.presentation.view.report.application.ServiceReport;
import org.dddjava.jig.presentation.view.report.business_rule.StringComparingReport;
import org.springframework.stereotype.Controller;

@Controller
public class ApplicationListController {

    ConvertContext convertContext;
    ApplicationService applicationService;
    BusinessRuleService businessRuleService;

    public ApplicationListController(AliasService aliasService,
                                     ApplicationService applicationService,
                                     BusinessRuleService businessRuleService) {
        this.convertContext = new ConvertContext(aliasService);
        this.applicationService = applicationService;
        this.businessRuleService = businessRuleService;
    }

    @DocumentMapping(JigDocument.ApplicationList)
    public JigModelAndView<ModelReports> applicationList(AnalyzedImplementation implementations) {
        ModelReports modelReports = new ModelReports(
                controllerReport(implementations),
                serviceReport(implementations),
                datasourceReport(implementations),
                stringComparingReport(implementations)
        );

        return new JigModelAndView<>(modelReports, new ModelReportsPoiView(convertContext));
    }

    ModelReport<?> controllerReport(AnalyzedImplementation implementations) {
        ControllerMethods controllerMethods = applicationService.controllerAngles(implementations);

        return new ModelReport<>(controllerMethods.list(),
                requestHandlerMethod -> new ControllerReport(requestHandlerMethod),
                ControllerReport.class);
    }

    ModelReport<?> serviceReport(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);

        return new ModelReport<>(serviceAngles.list(),
                serviceAngle -> new ServiceReport(serviceAngle),
                ServiceReport.class);
    }

    ModelReport<?> datasourceReport(AnalyzedImplementation implementations) {
        DatasourceAngles datasourceAngles = applicationService.datasourceAngles(implementations);
        return new ModelReport<>(datasourceAngles.list(), RepositoryReport::new, RepositoryReport.class);
    }

    ModelReport<?> stringComparingReport(AnalyzedImplementation implementations) {
        StringComparingCallerMethods stringComparingCallerMethods = applicationService.stringComparing(implementations);
        return new ModelReport<>(stringComparingCallerMethods.list(), StringComparingReport::new, StringComparingReport.class);
    }
}

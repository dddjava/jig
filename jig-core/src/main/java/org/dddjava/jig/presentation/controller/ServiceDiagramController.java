package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.domain.model.fact.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.fact.alias.AliasFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceDiagramController {

    ApplicationService applicationService;
    AliasService aliasService;
    ViewResolver viewResolver;

    public ServiceDiagramController(ApplicationService applicationService, AliasService aliasService, ViewResolver viewResolver) {
        this.applicationService = applicationService;
        this.aliasService = aliasService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy(aliasFinder));
    }

    @DocumentMapping(JigDocument.BooleanServiceDiagram)
    public JigModelAndView<?> booleanServiceTrace(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace(aliasFinder));
    }
}

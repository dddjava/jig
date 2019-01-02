package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.implementation.Implementations;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.threelayer.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceDiagramController {

    ApplicationService applicationService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public ServiceDiagramController(ApplicationService applicationService, GlossaryService glossaryService, ViewResolver viewResolver) {
        this.applicationService = applicationService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchyDiagram)
    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy(Implementations implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations.typeByteCodes());
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy(japaneseNameFinder));
    }

    @DocumentMapping(JigDocument.BooleanServiceDiagram)
    public JigModelAndView<?> booleanServiceTrace(Implementations implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations.typeByteCodes());
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace(japaneseNameFinder));
    }
}

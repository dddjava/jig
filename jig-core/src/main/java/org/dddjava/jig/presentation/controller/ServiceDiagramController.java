package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.implementation.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.implementation.analyzed.alias.JapaneseNameFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
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
    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations.typeByteCodes());
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy(japaneseNameFinder));
    }

    @DocumentMapping(JigDocument.BooleanServiceDiagram)
    public JigModelAndView<?> booleanServiceTrace(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = applicationService.serviceAngles(implementations.typeByteCodes());
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace(japaneseNameFinder));
    }
}

package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.ApplicationService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceDiagramController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiagramController.class);

    ApplicationService applicationService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public ServiceDiagramController(ApplicationService applicationService, GlossaryService glossaryService, ViewResolver viewResolver) {
        this.applicationService = applicationService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchy)
    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy(TypeByteCodes typeByteCodes) {
        LOGGER.info("サービスメソッド呼び出しダイアグラムを出力します");
        ServiceAngles serviceAngles = applicationService.serviceAngles(typeByteCodes);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy(japaneseNameFinder));
    }

    @DocumentMapping(JigDocument.BooleanService)
    public JigModelAndView<?> booleanServiceTrace(TypeByteCodes typeByteCodes) {
        LOGGER.info("真偽値を返すサービスメソッド追跡ダイアグラムを出力します");
        ServiceAngles serviceAngles = applicationService.serviceAngles(typeByteCodes);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace(japaneseNameFinder));
    }
}

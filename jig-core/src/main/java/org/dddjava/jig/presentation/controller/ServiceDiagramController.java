package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.domain.model.report.JigDocument;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceDiagramController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiagramController.class);

    AngleService angleService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public ServiceDiagramController(AngleService angleService, GlossaryService glossaryService, ViewResolver viewResolver) {
        this.angleService = angleService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.ServiceMethodCallHierarchy)
    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy(ProjectData projectData) {
        LOGGER.info("サービスメソッド呼び出しダイアグラムを出力します");
        ServiceAngles serviceAngles = angleService.serviceAngles(projectData);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy(japaneseNameFinder));
    }

    @DocumentMapping(JigDocument.BooleanService)
    public JigModelAndView<?> booleanServiceTrace(ProjectData projectData) {
        LOGGER.info("真偽値を返すサービスメソッド追跡ダイアグラムを出力します");
        ServiceAngles serviceAngles = angleService.serviceAngles(projectData);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace(japaneseNameFinder));
    }
}

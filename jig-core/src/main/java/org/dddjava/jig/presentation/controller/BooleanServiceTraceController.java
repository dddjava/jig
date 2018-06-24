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
import org.springframework.stereotype.Controller;

@Controller
public class BooleanServiceTraceController {

    private AngleService angleService;
    private GlossaryService glossaryService;
    private ViewResolver viewResolver;

    public BooleanServiceTraceController(AngleService angleService, GlossaryService glossaryService, ViewResolver viewResolver) {
        this.angleService = angleService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.BooleanService)
    public JigModelAndView<?> diagram(ProjectData projectData) {
        ServiceAngles serviceAngles = angleService.serviceAngles(projectData);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(serviceAngles, viewResolver.booleanServiceTrace(japaneseNameFinder));
    }
}

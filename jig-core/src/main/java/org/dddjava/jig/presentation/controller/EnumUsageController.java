package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.springframework.stereotype.Controller;

@Controller
public class EnumUsageController {

    AngleService angleService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public EnumUsageController(AngleService angleService, GlossaryService glossaryService, ViewResolver viewResolver) {
        this.angleService = angleService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    public JigModelAndView<EnumAngles> enumUsage(ProjectData projectData) {
        EnumAngles enumAngles = angleService.enumAngles(projectData);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(enumAngles, viewResolver.enumUsage(japaneseNameFinder));
    }
}

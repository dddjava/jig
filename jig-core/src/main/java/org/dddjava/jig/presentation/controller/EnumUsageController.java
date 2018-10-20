package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.implementation.bytecode.ProjectData;
import org.dddjava.jig.domain.model.japanese.JapaneseNameFinder;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class EnumUsageController {

    BusinessRuleService businessRuleService;
    GlossaryService glossaryService;
    ViewResolver viewResolver;

    public EnumUsageController(BusinessRuleService businessRuleService, GlossaryService glossaryService, ViewResolver viewResolver) {
        this.businessRuleService = businessRuleService;
        this.glossaryService = glossaryService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.EnumUsage)
    public JigModelAndView<CategoryAngles> enumUsage(ProjectData projectData) {
        CategoryAngles categoryAngles = businessRuleService.categories(projectData);
        JapaneseNameFinder japaneseNameFinder = new JapaneseNameFinder.GlossaryServiceAdapter(glossaryService);
        return new JigModelAndView<>(categoryAngles, viewResolver.enumUsage(japaneseNameFinder));
    }
}

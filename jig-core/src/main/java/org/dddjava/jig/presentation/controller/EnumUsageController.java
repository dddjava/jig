package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AliasService;
import org.dddjava.jig.application.service.BusinessRuleService;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.interpret.alias.AliasFinder;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.presentation.view.JigDocument;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.DocumentMapping;
import org.springframework.stereotype.Controller;

@Controller
public class EnumUsageController {

    BusinessRuleService businessRuleService;
    AliasService aliasService;
    ViewResolver viewResolver;

    public EnumUsageController(BusinessRuleService businessRuleService, AliasService aliasService, ViewResolver viewResolver) {
        this.businessRuleService = businessRuleService;
        this.aliasService = aliasService;
        this.viewResolver = viewResolver;
    }

    @DocumentMapping(JigDocument.CategoryUsageDiagram)
    public JigModelAndView<CategoryAngles> enumUsage(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(categoryAngles, viewResolver.enumUsage(aliasFinder));
    }

    @DocumentMapping(JigDocument.CategoryDiagram)
    public JigModelAndView<CategoryAngles> categories(AnalyzedImplementation implementations) {
        CategoryAngles categoryAngles = businessRuleService.categories(implementations);
        AliasFinder aliasFinder = new AliasFinder.GlossaryServiceAdapter(aliasService);
        return new JigModelAndView<>(categoryAngles, viewResolver.categories(aliasFinder));
    }
}

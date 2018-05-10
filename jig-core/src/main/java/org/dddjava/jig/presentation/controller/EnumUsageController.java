package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.domain.model.angle.EnumAngles;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.springframework.stereotype.Controller;

@Controller
public class EnumUsageController {

    AngleService angleService;
    ViewResolver viewResolver;

    public EnumUsageController(AngleService angleService, ViewResolver viewResolver) {
        this.angleService = angleService;
        this.viewResolver = viewResolver;
    }

    public JigModelAndView<EnumAngles> enumUsage() {
        EnumAngles enumAngles = angleService.enumAngles();
        return new JigModelAndView<>(enumAngles, viewResolver.enumUsage());
    }
}

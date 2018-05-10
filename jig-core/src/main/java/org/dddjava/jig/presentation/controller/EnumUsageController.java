package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.domain.model.angle.EnumAngles;
import org.dddjava.jig.presentation.view.local.JigViewResolver;
import org.dddjava.jig.presentation.view.local.LocalView;
import org.springframework.stereotype.Controller;

@Controller
public class EnumUsageController {

    AngleService angleService;
    JigViewResolver jigViewResolver;

    public EnumUsageController(AngleService angleService, JigViewResolver jigViewResolver) {
        this.angleService = angleService;
        this.jigViewResolver = jigViewResolver;
    }

    public LocalView enumUsage() {
        EnumAngles enumAngles = angleService.enumAngles();
        return jigViewResolver.enumUsage(enumAngles);
    }
}

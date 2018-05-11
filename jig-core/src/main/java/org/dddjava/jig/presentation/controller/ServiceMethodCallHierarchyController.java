package org.dddjava.jig.presentation.controller;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.presentation.view.JigModelAndView;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceMethodCallHierarchyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMethodCallHierarchyController.class);

    AngleService angleService;
    ViewResolver viewResolver;

    public ServiceMethodCallHierarchyController(AngleService angleService, ViewResolver viewResolver) {
        this.angleService = angleService;
        this.viewResolver = viewResolver;
    }

    public JigModelAndView<ServiceAngles> serviceMethodCallHierarchy() {
        LOGGER.info("サービスメソッド呼び出しダイアグラムを出力します");
        ServiceAngles serviceAngles = angleService.serviceAngles();
        return new JigModelAndView<>(serviceAngles, viewResolver.serviceMethodCallHierarchy());
    }
}

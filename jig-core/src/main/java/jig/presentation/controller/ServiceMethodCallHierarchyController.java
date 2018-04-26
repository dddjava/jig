package jig.presentation.controller;

import jig.application.service.AngleService;
import jig.domain.model.angle.ServiceAngles;
import jig.presentation.view.JigViewResolver;
import jig.presentation.view.LocalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ServiceMethodCallHierarchyController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceMethodCallHierarchyController.class);

    AngleService angleService;
    JigViewResolver jigViewResolver;

    public ServiceMethodCallHierarchyController(AngleService angleService, JigViewResolver jigViewResolver) {
        this.angleService = angleService;
        this.jigViewResolver = jigViewResolver;
    }

    public LocalView serviceMethodCallHierarchy() {
        LOGGER.info("サービスメソッド呼び出しダイアグラムを出力します");
        ServiceAngles serviceAngles = angleService.serviceAngles();
        return jigViewResolver.serviceMethodCallHierarchy(serviceAngles);
    }
}

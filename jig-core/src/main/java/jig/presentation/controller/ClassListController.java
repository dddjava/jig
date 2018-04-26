package jig.presentation.controller;

import jig.application.usecase.ReportService;
import jig.domain.model.report.Reports;
import jig.presentation.view.JigViewResolver;
import jig.presentation.view.LocalView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;

@Controller
public class ClassListController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListController.class);

    ReportService reportService;
    JigViewResolver jigViewResolver;

    public ClassListController(ReportService reportService, JigViewResolver jigViewResolver) {
        this.reportService = reportService;
        this.jigViewResolver = jigViewResolver;
    }

    public LocalView classList() {
        LOGGER.info("クラス一覧を出力します");
        Reports reports = reportService.reports();
        return jigViewResolver.classList(reports);
    }
}

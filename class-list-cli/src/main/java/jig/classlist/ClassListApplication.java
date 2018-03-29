package jig.classlist;

import jig.application.service.AnalyzeService;
import jig.application.service.ReportService;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.report.Reports;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class ClassListApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassListApplication.class);

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(ClassListApplication.class, args);

        context.getBean(ClassListApplication.class).output();
    }

    @Value("${output.list.name}")
    String outputPath;

    @Value("${project.path}")
    String projectPath;

    @Autowired
    AnalyzeService analyzeService;
    @Autowired
    ReportService reportService;

    public void output() {
        long startTime = System.currentTimeMillis();

        LOGGER.info("解析をはじめます");

        Path path = Paths.get(projectPath);
        analyzeService.importProject(new ProjectLocation(path));

        LOGGER.info("レポートデータの準備をはじめます");

        Reports reports = reportService.reports();

        LOGGER.info("ファイルに書き出します");

        ReportFormat.from(outputPath)
                .writer()
                .writeTo(reports, Paths.get(outputPath));

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}


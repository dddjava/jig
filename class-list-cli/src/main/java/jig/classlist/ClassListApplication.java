package jig.classlist;

import jig.application.service.AnalyzeService;
import jig.application.service.ReportService;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.report.Report;
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
    @Value("${output.list.type}")
    String listType;

    @Value("${project.path}")
    String projectPath;

    @Autowired
    AnalyzeService analyzeService;
    @Autowired
    ReportService reportService;

    public void output() {
        long startTime = System.currentTimeMillis();
        Path path = Paths.get(projectPath);
        analyzeService.analyze(path);

        Characteristic characteristic = Characteristic.valueOf(listType.toUpperCase());
        Report report = reportService.getReport(characteristic);

        ReportFormat.from(outputPath)
                .writer()
                .writeTo(report, Paths.get(outputPath));

        LOGGER.info("所用時間: {} ms", System.currentTimeMillis() - startTime);
    }
}


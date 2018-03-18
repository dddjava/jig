package jig.classlist;

import jig.application.service.AnalyzeService;
import jig.application.service.ReportService;
import jig.domain.model.report.Report;
import jig.domain.model.tag.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication(scanBasePackages = "jig")
public class ClassListApplication {

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
        Path path = Paths.get(projectPath);
        analyzeService.analyze(path);

        Tag tag = Tag.valueOf(listType.toUpperCase());
        Report report = reportService.getReport(tag);

        ReportFormat.from(outputPath)
                .writer()
                .writeTo(report, Paths.get(outputPath));
    }
}


package jig.classlist;

import jig.application.usecase.ImportService;
import jig.application.usecase.ReportService;
import jig.domain.model.report.Reports;
import jig.infrastructure.LocalProject;
import jig.infrastructure.poi.ReportFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

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

    @Autowired
    ImportService importService;
    @Autowired
    ReportService reportService;

    @Autowired
    LocalProject localProject;

    public void output() {
        long startTime = System.currentTimeMillis();

        LOGGER.info("プロジェクト情報の取り込みをはじめます");
        importService.importSources(localProject.getSpecificationSources(), localProject.getSqlSources(), localProject.getTypeNameSources(), localProject.getPackageNameSources());

        LOGGER.info("レポートデータの準備をはじめます");
        Reports reports = reportService.reports();

        LOGGER.info("ファイルに書き出します");
        ReportFormat.from(outputPath)
                .writer()
                .writeTo(reports, Paths.get(outputPath));

        LOGGER.info("合計時間: {} ms", System.currentTimeMillis() - startTime);
    }
}


package jig.gradle;

import jig.application.service.AnalyzeService;
import jig.application.service.ReportService;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.report.template.Reports;
import org.dddjava.jig.infrastracture.ReportFormat;
import org.gradle.api.DefaultTask;
import org.gradle.api.plugins.Convention;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JigListTask extends DefaultTask {

    ServiceFactory serviceFactory = new ServiceFactory();

    @TaskAction
    public void apply() {
        ExtensionContainer extensions = getProject().getExtensions();
        JigListExtension extension = extensions.findByType(JigListExtension.class);
        ProjectLocation projectLocation = new ProjectLocation(getProject().getProjectDir().toPath());
        Convention convention = getProject().getConvention();
        AnalyzeService analyzeService = serviceFactory.analyzeService(convention);
        analyzeService.importProject(projectLocation);

        String outputPath = extension.getOutputPath();
        ensureExists(outputPath);

        ReportService reportService = serviceFactory.reportService(outputPath);
        Reports reports = reportService.reports();

        ReportFormat.from(outputPath)
                .writer()
                .writeTo(reports, Paths.get(outputPath));
    }

    private void ensureExists(String outputPath) {
        Path outputDirPath = Paths.get(outputPath);
        try {
            Files.createDirectories(outputDirPath.getParent());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}

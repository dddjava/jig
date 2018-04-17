package jig.gradle;

import jig.domain.model.project.ProjectLocation;
import jig.domain.model.report.template.Reports;
import org.dddjava.jig.infrastracture.ReportFormat;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JigListTask extends DefaultTask {

    ServiceFactory serviceFactory = new ServiceFactory();

    @TaskAction
    public void apply() {
        JigListExtension extension = getProject().getExtensions().findByType(JigListExtension.class);

        Path path = getProject().getProjectDir().toPath();
        serviceFactory.analyzeService().importProject(new ProjectLocation(path));

        Reports reports = serviceFactory.reportService(extension.getOutputPath()).reports();

        Path outputDirPath = Paths.get(extension.getOutputPath()).getParent();
        try {
            Files.createDirectories(outputDirPath);
        } catch (IOException e) {
            throw new AssertionError(e);
        }
        ReportFormat.from(extension.getOutputPath())
                .writer()
                .writeTo(reports, Paths.get(extension.getOutputPath()));
    }
}

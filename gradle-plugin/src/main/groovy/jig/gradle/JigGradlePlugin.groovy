package jig.gradle

import jig.application.service.AnalyzeService
import jig.application.service.ReportService
import jig.domain.model.project.ProjectLocation
import jig.domain.model.report.template.Reports
import jig.gradle.infrastructure.ReportFormat
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext

import java.nio.file.Path
import java.nio.file.Paths

class JigGradlePlugin implements Plugin<Project> {



    @Override
    void apply(Project project) {
        ApplicationContext context = new AnnotationConfigApplicationContext("jig")
        AnalyzeService analyzeService = context.getBean(AnalyzeService.class)
        ReportService reportService = context.getBean(ReportService.class)

        project.plugins.apply("java")



        def extension = project.extensions.create('jigList', JigListExtension)
        project.task("jigList") {
            doLast {
                Path path = project.projectDir.toPath()
                analyzeService.importProject(new ProjectLocation(path))

                Reports reports = reportService.reports()

                ReportFormat.from(extension.outputPath)
                        .writer()
                        .writeTo(reports, Paths.get(extension.outputPath))
            }
        }
    }
}

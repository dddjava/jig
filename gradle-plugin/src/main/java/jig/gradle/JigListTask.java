package jig.gradle;

import jig.application.service.AnalyzeService;
import jig.application.service.DependencyService;
import jig.application.service.ReportService;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.project.ProjectLocation;
import jig.domain.model.relation.RelationRepository;
import jig.domain.model.report.template.Reports;
import jig.infrastructure.JigPaths;
import jig.infrastructure.PrefixRemoveIdentifierFormatter;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.JavaparserJapaneseReader;
import jig.infrastructure.mybatis.MyBatisSqlReader;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;
import org.dddjava.jig.infrastracture.ReportFormat;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JigListTask extends DefaultTask {

    @TaskAction
    public void apply() {
        JigListExtension extension = getProject().getExtensions().findByType(JigListExtension.class);

        JigPaths jigPaths = new JigPaths(
                "build/classes/main",
                "build/resources/main",
                "src/main/java"
        );
        CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
        RelationRepository relationRepository = new OnMemoryRelationRepository();
        SqlRepository sqlRepository = new OnMemorySqlRepository();
        AnalyzeService analyzeService = new AnalyzeService(
                new AsmClassFileReader(),
                new MyBatisSqlReader(),
                new JavaparserJapaneseReader(
                        new OnMemoryJapaneseNameRepository(),
                        jigPaths
                ),
                new DependencyService(
                        characteristicRepository,
                        relationRepository
                ),
                jigPaths,
                sqlRepository
        );

        ReportService reportService = new ReportService(
                characteristicRepository,
                relationRepository,
                sqlRepository,
                new OnMemoryJapaneseNameRepository(),
                new PrefixRemoveIdentifierFormatter(extension.getOutputOmitPrefix())
        );

        Path path = getProject().getProjectDir().toPath();
        analyzeService.importProject(new ProjectLocation(path));

        Reports reports = reportService.reports();

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

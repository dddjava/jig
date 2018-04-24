package jig.gradle;

import jig.application.service.*;
import jig.application.usecase.ImportService;
import jig.application.usecase.ReportService;
import jig.diagram.plantuml.PlantumlDriver;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.relation.RelationRepository;
import jig.infrastructure.LocalProject;
import jig.infrastructure.PrefixRemoveIdentifierFormatter;
import jig.infrastructure.PropertySpecificationContext;
import jig.infrastructure.asm.AsmSpecificationReader;
import jig.infrastructure.javaparser.JavaparserJapaneseReader;
import jig.infrastructure.mybatis.MyBatisSqlReader;
import jig.infrastructure.onmemoryrepository.*;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class ServiceFactory {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceFactory.class);

    final CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    final RelationRepository relationRepository = new OnMemoryRelationRepository();
    final SqlRepository sqlRepository = new OnMemorySqlRepository();
    final JapaneseNameRepository japaneseNameRepository = new OnMemoryJapaneseNameRepository();
    final AnnotationDeclarationRepository annotationDeclarationRepository = new OnMemoryAnnotationDeclarationRepository();

    ImportService importService(Project project) {

        // TODO extensionで変更できるようにする
        PropertySpecificationContext specificationContext = new PropertySpecificationContext();

        return new ImportService(
                new SpecificationService(
                        new AsmSpecificationReader(specificationContext),
                        characteristicRepository,
                        relationRepository,
                        annotationDeclarationRepository,
                        dependencyService()),
                new GlossaryService(
                        new JavaparserJapaneseReader(),
                        japaneseNameRepository
                ),
                new DatasourceService(
                        new MyBatisSqlReader(),
                        sqlRepository
                )
        );
    }

    public LocalProject localProject(Project project) {
        JavaPluginConvention javaPluginConvention = project.getConvention().findPlugin(JavaPluginConvention.class);
        if (javaPluginConvention == null) {
            throw new AssertionError("JavaPluginが適用されていません。");
        }

        SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        File srcDir = mainSourceSet.getJava().getSrcDirs().iterator().next();
        File classesOutputDir = mainSourceSet.getOutput().getClassesDir();
        File resourceOutputDir = mainSourceSet.getOutput().getResourcesDir();

        return new LocalProject(
                project.getProjectDir().toString(),
                classesOutputDir.getAbsolutePath(),
                resourceOutputDir.getAbsolutePath(),
                srcDir.getAbsolutePath()
        );
    }

    ReportService reportService(String outputOmitPrefixPath) {
        return new ReportService(
                new PrefixRemoveIdentifierFormatter(outputOmitPrefixPath),
                annotationDeclarationRepository,
                new GlossaryService(
                        new JavaparserJapaneseReader(),
                        japaneseNameRepository),
                new AngleService(
                        characteristicRepository,
                        relationRepository,
                        sqlRepository)
        );
    }

    public DependencyService dependencyService() {
        return new DependencyService(characteristicRepository, relationRepository);
    }

    PlantumlDriver diagramService(String outputOmitPrefix) {
        return new PlantumlDriver(new PrefixRemoveIdentifierFormatter(outputOmitPrefix), japaneseNameRepository);
    }
}

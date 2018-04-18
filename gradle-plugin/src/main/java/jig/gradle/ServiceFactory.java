package jig.gradle;

import jig.application.service.AnalyzeService;
import jig.application.service.DependencyService;
import jig.application.service.DiagramService;
import jig.application.service.ReportService;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.diagram.DiagramConverter;
import jig.domain.model.diagram.DiagramMaker;
import jig.domain.model.diagram.DiagramRepository;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.jdeps.RelationAnalyzer;
import jig.domain.model.relation.RelationRepository;
import jig.infrastructure.JigPaths;
import jig.infrastructure.PrefixRemoveIdentifierFormatter;
import jig.infrastructure.asm.AsmClassFileReader;
import jig.infrastructure.javaparser.JavaparserJapaneseReader;
import jig.infrastructure.jdeps.JdepsExecutor;
import jig.infrastructure.mybatis.MyBatisSqlReader;
import jig.infrastructure.onmemoryrepository.OnMemoryCharacteristicRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import jig.infrastructure.onmemoryrepository.OnMemoryRelationRepository;
import jig.infrastructure.onmemoryrepository.OnMemorySqlRepository;
import jig.infrastructure.plantuml.DiagramRepositoryImpl;
import jig.infrastructure.plantuml.PlantumlDiagramConverter;
import jig.infrastructure.plantuml.PlantumlDiagramMaker;
import org.gradle.api.plugins.Convention;
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
    final DiagramRepository diagramRepository = new DiagramRepositoryImpl();
    final JapaneseNameRepository japaneseNameRepository = new OnMemoryJapaneseNameRepository();

    final DiagramMaker diagramMaker =  new PlantumlDiagramMaker();

    final DiagramConverter diagramConverter(String outputOmitPrefix) {
        return new PlantumlDiagramConverter(new PrefixRemoveIdentifierFormatter(outputOmitPrefix), japaneseNameRepository);
    }


    AnalyzeService analyzeService(Convention convention) {
        JavaPluginConvention javaPluginConvention = convention.findPlugin(JavaPluginConvention.class);
        if (javaPluginConvention == null) {
            throw new AssertionError("JavaPluginが適用されていません。");
        }
        JigPaths jigPaths = jigPaths(javaPluginConvention);

        return new AnalyzeService(
                new AsmClassFileReader(),
                new MyBatisSqlReader(),
                new JavaparserJapaneseReader(
                        japaneseNameRepository,
                        jigPaths
                ),
                new DependencyService(
                        characteristicRepository,
                        relationRepository
                ),
                jigPaths,
                sqlRepository
        );
    }

    private JigPaths jigPaths(JavaPluginConvention javaPluginConvention) {
        SourceSet mainSourceSet = javaPluginConvention.getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        File srcDir = mainSourceSet.getJava().getSrcDirs().iterator().next();
        File classesOutputDir = mainSourceSet.getJava().getOutputDir();
        File resourceOutputDir = mainSourceSet.getResources().getOutputDir();

        return new JigPaths(
                classesOutputDir.getAbsolutePath(),
                resourceOutputDir.getAbsolutePath(),
                srcDir.getAbsolutePath()
        );
    }

    ReportService reportService(String outputOmitPrefixPath) {
        return new ReportService(
                characteristicRepository,
                relationRepository,
                sqlRepository,
                japaneseNameRepository,
                new PrefixRemoveIdentifierFormatter(outputOmitPrefixPath)
        );
    }


    RelationAnalyzer relationAnalyzer() {
        return new JdepsExecutor();
    }

    DiagramService diagramService(String outputOmitPrefix) {
        return new DiagramService(
                diagramRepository,
                diagramMaker,
                diagramConverter(outputOmitPrefix));
    }
}

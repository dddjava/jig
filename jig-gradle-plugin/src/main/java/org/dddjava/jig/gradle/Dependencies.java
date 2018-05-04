package org.dddjava.jig.gradle;

import org.dddjava.jig.application.service.*;
import org.dddjava.jig.application.usecase.ImportService;
import org.dddjava.jig.domain.model.characteristic.CharacteristicRepository;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethodRepository;
import org.dddjava.jig.domain.model.datasource.SqlRepository;
import org.dddjava.jig.domain.model.declaration.annotation.AnnotationDeclarationRepository;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.domain.model.relation.RelationRepository;
import org.dddjava.jig.domain.model.relation.dependency.DependencyRepository;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.PropertySpecificationContext;
import org.dddjava.jig.infrastructure.asm.AsmSpecificationReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.*;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceMethodCallHierarchyController;
import org.dddjava.jig.presentation.controller.classlist.ClassListController;
import org.dddjava.jig.presentation.view.JigViewResolver;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginConvention;
import org.gradle.api.tasks.SourceSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Dependencies {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dependencies.class);

    final CharacteristicRepository characteristicRepository = new OnMemoryCharacteristicRepository();
    final CharacterizedMethodRepository characterizedMethodRepository = new OnMemoryCharacterizedMethodRepository();
    final RelationRepository relationRepository = new OnMemoryRelationRepository();
    final SqlRepository sqlRepository = new OnMemorySqlRepository();
    final JapaneseNameRepository japaneseNameRepository = new OnMemoryJapaneseNameRepository();
    final AnnotationDeclarationRepository annotationDeclarationRepository = new OnMemoryAnnotationDeclarationRepository();
    final DependencyRepository dependencyRepository = new OnMemoryDependencyRepository();

    ImportService importService() {

        // TODO extensionで変更できるようにする
        PropertySpecificationContext specificationContext = new PropertySpecificationContext();

        return new ImportService(
                new SpecificationService(
                        new AsmSpecificationReader(specificationContext),
                        new CharacteristicService(characteristicRepository, characterizedMethodRepository),
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

    LocalProject localProject(Project project) {
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

    DependencyService dependencyService() {
        return new DependencyService(characteristicRepository, dependencyRepository);
    }

    private JigViewResolver jigViewResolver(String outputOmitPrefix) {
        return new JigViewResolver(
                new PrefixRemoveIdentifierFormatter(outputOmitPrefix),
                japaneseNameRepository);
    }

    public ClassListController classListController(String outputOmitPrefix) {
        return new ClassListController(
                jigViewResolver(outputOmitPrefix),
                new PrefixRemoveIdentifierFormatter(outputOmitPrefix),
                annotationDeclarationRepository,
                new GlossaryService(
                        new JavaparserJapaneseReader(),
                        japaneseNameRepository),
                new AngleService(
                        new CharacteristicService(characteristicRepository, characterizedMethodRepository),
                        relationRepository,
                        sqlRepository
                ));
    }

    public PackageDependencyController packageDependencyController(String outputOmitPrefix) {
        return new PackageDependencyController(dependencyService(), jigViewResolver(outputOmitPrefix));
    }

    public ServiceMethodCallHierarchyController serviceMethodCallHierarchyController(String outputOmitPrefix) {
        return new ServiceMethodCallHierarchyController(
                new AngleService(
                        new CharacteristicService(characteristicRepository, characterizedMethodRepository),
                        relationRepository,
                        sqlRepository
                ),
                new JigViewResolver(
                        new PrefixRemoveIdentifierFormatter(outputOmitPrefix),
                        japaneseNameRepository
                )
        );
    }
}

package org.dddjava.jig.gradle;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.PropertyCharacterizedTypeFactory;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.dddjava.jig.presentation.controller.*;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.graphvizj.DiagramFormat;
import org.dddjava.jig.presentation.view.graphvizj.MethodNodeLabelStyle;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dependencies {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dependencies.class);

    final JapaneseNameRepository japaneseNameRepository = new OnMemoryJapaneseNameRepository();

    LocalProject localProject(Project project) {
        return new LocalProject(new GradleProject(project).allDependencyJavaProjects());
    }

    ImplementationService importService() {
        // TODO extensionで変更できるようにする
        PropertyCharacterizedTypeFactory propertyByteCodeAnalyzeContext = new PropertyCharacterizedTypeFactory();

        return new ImplementationService(
                new AsmByteCodeFactory(),
                glossaryService(),
                new MyBatisSqlReader(),
                new PropertyCharacterizedTypeFactory());
    }

    ClassListController classListController(String outputOmitPrefix) {
        return new ClassListController(
                new PrefixRemoveIdentifierFormatter(outputOmitPrefix),
                glossaryService(),
                angleService()
        );
    }

    EnumUsageController enumUsageController(String outputOmitPrefix) {
        return new EnumUsageController(
                angleService(),
                glossaryService(),
                jigViewResolver(outputOmitPrefix));
    }

    PackageDependencyController packageDependencyController(String outputOmitPrefix, int depth) {
        return new PackageDependencyController(
                dependencyService(),
                glossaryService(),
                jigViewResolver(outputOmitPrefix),
                depth);
    }

    ServiceMethodCallHierarchyController serviceMethodCallHierarchyController(String outputOmitPrefix) {
        return new ServiceMethodCallHierarchyController(
                angleService(),
                glossaryService(),
                jigViewResolver(outputOmitPrefix));
    }

    private DependencyService dependencyService() {
        return new DependencyService();
    }

    private AngleService angleService() {
        return new AngleService();
    }

    private ViewResolver jigViewResolver(String outputOmitPrefix) {
        return new ViewResolver(new PrefixRemoveIdentifierFormatter(outputOmitPrefix), MethodNodeLabelStyle.SIMPLE.name(), DiagramFormat.SVG.name());
    }

    private GlossaryService glossaryService() {
        return new GlossaryService(
                new JavaparserJapaneseReader(),
                japaneseNameRepository
        );
    }

    public JigDocumentHandlers localViewContextWith(JigConfig config) {
        return new JigDocumentHandlers(
                serviceMethodCallHierarchyController(config.outputOmitPrefix),
                classListController(config.outputOmitPrefix),
                packageDependencyController(config.outputOmitPrefix, config.depth),
                enumUsageController(config.outputOmitPrefix),
                new BooleanServiceTraceController(
                        angleService(),
                        glossaryService(),
                        jigViewResolver(config.outputOmitPrefix))
        );
    }
}

package org.dddjava.jig.gradle;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.infrastructure.DefaultLocalProject;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.PropertyByteCodeAnalyzeContext;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceMethodCallHierarchyController;
import org.dddjava.jig.presentation.controller.classlist.ClassListController;
import org.dddjava.jig.presentation.view.JigHandlerContext;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.graphvizj.MethodNodeLabelStyle;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Dependencies {
    private static final Logger LOGGER = LoggerFactory.getLogger(Dependencies.class);

    final JapaneseNameRepository japaneseNameRepository = new OnMemoryJapaneseNameRepository();

    DefaultLocalProject localProject(Project project) {
        return new DefaultLocalProject(new GradleProject(project) .allDependencyJavaProjects());
    }

    ImplementationService importService() {
        // TODO extensionで変更できるようにする
        PropertyByteCodeAnalyzeContext propertyByteCodeAnalyzeContext = new PropertyByteCodeAnalyzeContext();

        return new ImplementationService(
                new AsmByteCodeFactory(propertyByteCodeAnalyzeContext),
                glossaryService(),
                new MyBatisSqlReader());
    }

    ClassListController classListController(String outputOmitPrefix) {
        return new ClassListController(
                jigViewResolver(outputOmitPrefix),
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

    PackageDependencyController packageDependencyController(String outputOmitPrefix) {
        return new PackageDependencyController(
                dependencyService(),
                glossaryService(),
                jigViewResolver(outputOmitPrefix));
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
        return new ViewResolver(new PrefixRemoveIdentifierFormatter(outputOmitPrefix), MethodNodeLabelStyle.SIMPLE.name());
    }

    private GlossaryService glossaryService() {
        return new GlossaryService(
                new JavaparserJapaneseReader(),
                japaneseNameRepository
        );
    }

    public JigHandlerContext localViewContextWith(JigConfig config) {
        return new JigHandlerContext(
                serviceMethodCallHierarchyController(config.outputOmitPrefix),
                classListController(config.outputOmitPrefix),
                packageDependencyController(config.outputOmitPrefix),
                enumUsageController(config.outputOmitPrefix),
                config.depth
        );
    }
}

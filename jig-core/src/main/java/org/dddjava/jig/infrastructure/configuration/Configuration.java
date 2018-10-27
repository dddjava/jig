package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.service.*;
import org.dddjava.jig.domain.model.configuration.ConfigurationContext;
import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.javaparser.JavaparserJapaneseReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJapaneseNameRepository;
import org.dddjava.jig.presentation.controller.ClassListController;
import org.dddjava.jig.presentation.controller.EnumUsageController;
import org.dddjava.jig.presentation.controller.PackageDependencyController;
import org.dddjava.jig.presentation.controller.ServiceDiagramController;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.graphvizj.DiagramFormat;
import org.dddjava.jig.presentation.view.graphvizj.MethodNodeLabelStyle;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;

public class Configuration {

    final LocalProject localProject;
    final ImplementationService implementationService;
    final JigDocumentHandlers documentHandlers;
    final ApplicationService applicationService;
    final DependencyService dependencyService;
    final ConfigurationContext configurationContext;
    final BusinessRuleService businessRuleService;
    final GlossaryService glossaryService;

    public Configuration(Layout layout, JigProperties properties, ConfigurationContext configurationContext) {
        this.configurationContext = configurationContext;
        Architecture architecture = new Architecture(properties.getBusinessRuleCondition());
        this.businessRuleService = new BusinessRuleService(architecture);
        this.dependencyService = new DependencyService(configurationContext, businessRuleService);
        this.glossaryService = new GlossaryService(new JavaparserJapaneseReader(), new OnMemoryJapaneseNameRepository());
        this.applicationService = new ApplicationService(architecture);
        PrefixRemoveIdentifierFormatter prefixRemoveIdentifierFormatter = new PrefixRemoveIdentifierFormatter(
                properties.getOutputOmitPrefix()
        );
        ViewResolver viewResolver = new ViewResolver(
                // TODO MethodNodeLabelStyleとDiagramFormatをプロパティで受け取れるようにする
                // @Value("${methodNodeLabelStyle:SIMPLE}") String methodNodeLabelStyle
                // @Value("${diagram.format:SVG}") String diagramFormat
                prefixRemoveIdentifierFormatter, MethodNodeLabelStyle.SIMPLE, DiagramFormat.SVG
        );
        ClassListController classListController = new ClassListController(
                prefixRemoveIdentifierFormatter,
                glossaryService,
                applicationService,
                businessRuleService
        );
        EnumUsageController enumUsageController = new EnumUsageController(
                businessRuleService,
                glossaryService,
                viewResolver
        );
        PackageDependencyController packageDependencyController = new PackageDependencyController(
                dependencyService,
                glossaryService,
                viewResolver,
                properties.getDepth()
        );
        ServiceDiagramController serviceDiagramController = new ServiceDiagramController(
                applicationService,
                glossaryService,
                viewResolver
        );
        this.localProject = new LocalProject(layout);
        this.implementationService = new ImplementationService(
                new AsmByteCodeFactory(),
                glossaryService,
                new MyBatisSqlReader()
        );
        this.documentHandlers = new JigDocumentHandlers(
                serviceDiagramController,
                classListController,
                packageDependencyController,
                enumUsageController,
                properties.jigDebugMode()
        );
    }

    public ApplicationService applicationService() {
        return applicationService;
    }

    public BusinessRuleService businessRuleService() {
        return businessRuleService;
    }

    public LocalProject localProject() {
        return localProject;
    }

    public ImplementationService implementationService() {
        return implementationService;
    }

    public JigDocumentHandlers documentHandlers() {
        return documentHandlers;
    }

    public DependencyService dependencyService() {
        return dependencyService;
    }

    public ConfigurationContext configurationContext() {
        return configurationContext;
    }

    public GlossaryService glossaryService() {
        return glossaryService;
    }
}

package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.service.*;
import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.infrastructure.LocalFileRawSourceFactory;
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

    ImplementationService implementationService;
    JigDocumentHandlers documentHandlers;
    ApplicationService applicationService;
    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    GlossaryService glossaryService;

    public Configuration(JigProperties properties) {
        Architecture architecture = new Architecture(properties.getBusinessRuleCondition());
        this.businessRuleService = new BusinessRuleService(architecture);
        this.dependencyService = new DependencyService(businessRuleService);
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
        this.implementationService = new ImplementationService(
                new AsmByteCodeFactory(),
                glossaryService,
                new MyBatisSqlReader(),
                new LocalFileRawSourceFactory()
        );
        this.documentHandlers = new JigDocumentHandlers(
                serviceDiagramController,
                classListController,
                packageDependencyController,
                enumUsageController,
                properties.jigDebugMode()
        );
    }

    public ImplementationService implementationService() {
        return implementationService;
    }

    public JigDocumentHandlers documentHandlers() {
        return documentHandlers;
    }
}

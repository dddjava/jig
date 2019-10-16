package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.service.*;
import org.dddjava.jig.domain.model.interpret.alias.SourceCodeAliasReader;
import org.dddjava.jig.domain.model.interpret.architecture.Architecture;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAliasRepository;
import org.dddjava.jig.presentation.controller.*;
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
    AliasService aliasService;

    public Configuration(JigProperties properties, SourceCodeAliasReader sourceCodeAliasReader) {
        Architecture architecture = new Architecture(properties);
        this.businessRuleService = new BusinessRuleService(architecture);
        this.dependencyService = new DependencyService(businessRuleService);
        this.aliasService = new AliasService(sourceCodeAliasReader, new OnMemoryAliasRepository());
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
        BusinessRuleListController businessRuleListController= new BusinessRuleListController(
                prefixRemoveIdentifierFormatter,
                aliasService,
                applicationService,
                businessRuleService
        );
        ClassListController classListController = new ClassListController(
                prefixRemoveIdentifierFormatter,
                aliasService,
                applicationService,
                businessRuleService
        );
        DiagramController diagramController = new DiagramController(
                dependencyService,
                businessRuleService,
                applicationService,
                aliasService,
                viewResolver
        );
        this.implementationService = new ImplementationService(
                new AsmByteCodeFactory(),
                aliasService,
                new MyBatisSqlReader(),
                new LocalFileSourceReader()
        );
        this.documentHandlers = new JigDocumentHandlers(
                businessRuleListController,
                classListController,
                diagramController
        );
    }

    public ImplementationService implementationService() {
        return implementationService;
    }

    public JigDocumentHandlers documentHandlers() {
        return documentHandlers;
    }
}

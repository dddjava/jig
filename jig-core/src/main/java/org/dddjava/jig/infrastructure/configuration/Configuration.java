package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.application.service.*;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.sources.jigfactory.Architecture;
import org.dddjava.jig.domain.model.sources.jigreader.CommentRepository;
import org.dddjava.jig.domain.model.sources.jigreader.SourceCodeAliasReader;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.asm.AsmFactReader;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
import org.dddjava.jig.infrastructure.logger.MessageLogger;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryCommentRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJigSourceRepository;
import org.dddjava.jig.presentation.controller.ApplicationListController;
import org.dddjava.jig.presentation.controller.BusinessRuleListController;
import org.dddjava.jig.presentation.controller.DiagramController;
import org.dddjava.jig.presentation.view.ResourceBundleJigDocumentContext;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;

import java.nio.file.Path;
import java.util.List;

public class Configuration {
    JigProperties properties;

    JigSourceReadService jigSourceReadService;
    JigDocumentHandlers documentHandlers;
    ApplicationService applicationService;
    DependencyService dependencyService;
    BusinessRuleService businessRuleService;

    public Configuration(JigProperties jigProperties, SourceCodeAliasReader sourceCodeAliasReader) {
        this.properties = new JigPropertyLoader(jigProperties).load();
        this.properties.prepareOutputDirectory();

        CommentRepository commentRepository = new OnMemoryCommentRepository();

        JigSourceRepository jigSourceRepository = new OnMemoryJigSourceRepository(commentRepository);

        Architecture architecture = new PropertyArchitectureFactory(properties).architecture();

        this.businessRuleService = new BusinessRuleService(architecture, jigSourceRepository);
        this.dependencyService = new DependencyService(businessRuleService, new MessageLogger(DependencyService.class), jigSourceRepository);
        this.applicationService = new ApplicationService(architecture, new MessageLogger(ApplicationService.class), jigSourceRepository);

        BusinessRuleListController businessRuleListController = new BusinessRuleListController(
                applicationService,
                businessRuleService
        );
        ApplicationListController applicationListController = new ApplicationListController(
                applicationService,
                businessRuleService
        );
        DiagramController diagramController = new DiagramController(
                dependencyService,
                businessRuleService,
                applicationService
        );
        this.jigSourceReadService = new JigSourceReadService(
                jigSourceRepository,
                new AsmFactReader(),
                sourceCodeAliasReader,
                new MyBatisSqlReader(),
                new LocalFileSourceReader()
        );

        JigDocumentContext jigDocumentContext = ResourceBundleJigDocumentContext.getInstanceWithAliasFinder(
                new AliasService(commentRepository), properties.linkPrefix(), new PrefixRemoveIdentifierFormatter(properties.getOutputOmitPrefix()));
        this.documentHandlers = new JigDocumentHandlers(
                new ViewResolver(
                        properties.outputDiagramFormat,
                        jigDocumentContext
                ),
                businessRuleListController,
                applicationListController,
                diagramController
        );
    }

    public JigSourceReadService implementationService() {
        return jigSourceReadService;
    }

    public JigDocumentHandlers documentHandlers() {
        return documentHandlers;
    }

    public Path outputDirectory() {
        return properties.outputDirectory;
    }

    public List<JigDocument> jigDocuments() {
        return properties.jigDocuments;
    }
}

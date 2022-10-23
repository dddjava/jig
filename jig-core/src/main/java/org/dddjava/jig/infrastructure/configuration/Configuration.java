package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.application.service.*;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.models.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.sources.jigreader.AdditionalTextSourceReader;
import org.dddjava.jig.domain.model.sources.jigreader.CommentRepository;
import org.dddjava.jig.domain.model.sources.jigreader.TextSourceReader;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.asm.AsmFactReader;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
import org.dddjava.jig.infrastructure.javaparser.JavaparserReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryCommentRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJigSourceRepository;
import org.dddjava.jig.presentation.controller.ApplicationListController;
import org.dddjava.jig.presentation.controller.BusinessRuleListController;
import org.dddjava.jig.presentation.controller.DiagramController;
import org.dddjava.jig.presentation.view.JigDocumentContextImpl;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;
import org.dddjava.jig.presentation.view.handler.ViewResolver;

public class Configuration {
    JigProperties properties;

    JigSourceReadService jigSourceReadService;
    JigDocumentHandlers documentHandlers;
    ApplicationService applicationService;
    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    AliasService aliasService;

    public Configuration(JigProperties jigProperties) {
        this(jigProperties, new AdditionalTextSourceReader());
    }

    public Configuration(JigProperties jigProperties, AdditionalTextSourceReader additionalTextSourceReader) {
        this.properties = new JigPropertyLoader(jigProperties).load();
        this.properties.prepareOutputDirectory();

        CommentRepository commentRepository = new OnMemoryCommentRepository();

        JigSourceRepository jigSourceRepository = new OnMemoryJigSourceRepository(commentRepository);

        Architecture architecture = new PropertyArchitectureFactory(properties).architecture();

        this.businessRuleService = new BusinessRuleService(architecture, jigSourceRepository);
        this.dependencyService = new DependencyService(businessRuleService);
        this.applicationService = new ApplicationService(jigSourceRepository);

        JavaparserReader javaparserReader = new JavaparserReader(properties);
        TextSourceReader textSourceReader = new TextSourceReader(javaparserReader, additionalTextSourceReader);

        this.jigSourceReadService = new JigSourceReadService(
                jigSourceRepository,
                new AsmFactReader(),
                textSourceReader,
                new MyBatisSqlReader(),
                new LocalFileSourceReader()
        );

        this.aliasService = new AliasService(commentRepository);
        JigDocumentContext jigDocumentContext = new JigDocumentContextImpl(
                aliasService, properties.linkPrefix(), new PrefixRemoveIdentifierFormatter(properties.getOutputOmitPrefix()));

        this.documentHandlers = new JigDocumentHandlers(
                new ViewResolver(
                        properties.outputDiagramFormat,
                        jigDocumentContext
                ),
                new BusinessRuleListController(businessRuleService),
                new ApplicationListController(applicationService),
                new DiagramController(dependencyService, businessRuleService, applicationService),
                properties.jigDocuments,
                properties.outputDirectory
        );
    }

    public JigSourceReadService implementationService() {
        return jigSourceReadService;
    }

    public JigDocumentHandlers documentHandlers() {
        return documentHandlers;
    }

}

package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.application.service.*;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.*;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.services.MethodNodeLabelStyle;
import org.dddjava.jig.domain.model.jigsource.jigloader.SourceCodeAliasReader;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.asm.AsmFactFactory;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
import org.dddjava.jig.infrastructure.logger.MessageLogger;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAliasRepository;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryJigSourceRepository;
import org.dddjava.jig.presentation.controller.ApplicationListController;
import org.dddjava.jig.presentation.controller.BusinessRuleListController;
import org.dddjava.jig.presentation.controller.DiagramController;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.graphvizj.DiagramFormat;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;

public class Configuration {

    ImplementationService implementationService;
    JigDocumentHandlers documentHandlers;
    ApplicationService applicationService;
    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    AliasService aliasService;

    public Configuration(JigProperties properties, SourceCodeAliasReader sourceCodeAliasReader) {
        // AliasFinderが無くなったらなくせる
        AliasRepository aliasRepository = new OnMemoryAliasRepository();

        JigSourceRepository jigSourceRepository = new OnMemoryJigSourceRepository(aliasRepository);
        this.aliasService = new AliasService(sourceCodeAliasReader, jigSourceRepository, aliasRepository);

        PropertyArchitectureFactory architectureFactory = new PropertyArchitectureFactory(properties);

        this.businessRuleService = new BusinessRuleService(architectureFactory.architecture(), jigSourceRepository);
        this.dependencyService = new DependencyService(businessRuleService, new MessageLogger(DependencyService.class), jigSourceRepository);
        this.applicationService = new ApplicationService(architectureFactory.architecture(), new MessageLogger(ApplicationService.class), jigSourceRepository);
        PrefixRemoveIdentifierFormatter prefixRemoveIdentifierFormatter = new PrefixRemoveIdentifierFormatter(
                properties.getOutputOmitPrefix()
        );

        // TypeやMethodにAliasを持たせて無くす
        AliasFinder aliasFinder = new AliasFinder() {
            @Override
            public PackageAlias find(PackageIdentifier packageIdentifier) {
                return aliasRepository.get(packageIdentifier);
            }

            @Override
            public TypeAlias find(TypeIdentifier typeIdentifier) {
                return aliasRepository.get(typeIdentifier);
            }

            @Override
            public MethodAlias find(MethodIdentifier methodIdentifier) {
                return aliasRepository.get(methodIdentifier);
            }
        };

        ViewResolver viewResolver = new ViewResolver(
                aliasFinder,
                // TODO MethodNodeLabelStyleとDiagramFormatをプロパティで受け取れるようにする
                // @Value("${methodNodeLabelStyle:SIMPLE}") String methodNodeLabelStyle
                // @Value("${diagram.format:SVG}") String diagramFormat
                prefixRemoveIdentifierFormatter, MethodNodeLabelStyle.SIMPLE, DiagramFormat.SVG
        );
        BusinessRuleListController businessRuleListController = new BusinessRuleListController(
                aliasService,
                applicationService,
                businessRuleService
        );
        ApplicationListController applicationListController = new ApplicationListController(
                aliasService,
                applicationService,
                businessRuleService
        );
        DiagramController diagramController = new DiagramController(
                dependencyService,
                businessRuleService,
                applicationService,
                architectureFactory
        );
        this.implementationService = new ImplementationService(
                jigSourceRepository,
                new AsmFactFactory(),
                aliasService,
                new MyBatisSqlReader(),
                new LocalFileSourceReader()
        );
        this.documentHandlers = new JigDocumentHandlers(
                viewResolver,
                businessRuleListController,
                applicationListController,
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

package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.service.*;
import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.*;
import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.architecture.IsBusinessRule;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.asm.AsmByteCodeFactory;
import org.dddjava.jig.infrastructure.filesystem.LocalFileSourceReader;
import org.dddjava.jig.infrastructure.mybatis.MyBatisSqlReader;
import org.dddjava.jig.infrastructure.onmemoryrepository.OnMemoryAliasRepository;
import org.dddjava.jig.presentation.controller.BusinessRuleListController;
import org.dddjava.jig.presentation.controller.ApplicationListController;
import org.dddjava.jig.presentation.controller.DiagramController;
import org.dddjava.jig.presentation.view.ViewResolver;
import org.dddjava.jig.presentation.view.graphvizj.DiagramFormat;
import org.dddjava.jig.presentation.view.graphvizj.MethodNodeLabelStyle;
import org.dddjava.jig.presentation.view.handler.JigDocumentHandlers;

import java.util.regex.Pattern;

public class Configuration {

    ImplementationService implementationService;
    JigDocumentHandlers documentHandlers;
    ApplicationService applicationService;
    DependencyService dependencyService;
    BusinessRuleService businessRuleService;
    AliasService aliasService;

    public Configuration(JigProperties properties, SourceCodeAliasReader sourceCodeAliasReader) {

        PropertyArchitectureFactory architectureFactory = new PropertyArchitectureFactory(properties);

        this.businessRuleService = new BusinessRuleService(architectureFactory);
        this.dependencyService = new DependencyService(businessRuleService);
        this.aliasService = new AliasService(sourceCodeAliasReader, new OnMemoryAliasRepository());
        this.applicationService = new ApplicationService(architectureFactory);
        PrefixRemoveIdentifierFormatter prefixRemoveIdentifierFormatter = new PrefixRemoveIdentifierFormatter(
                properties.getOutputOmitPrefix()
        );
        AliasFinder aliasFinder = new AliasFinder() {
            @Override
            public PackageAlias find(PackageIdentifier packageIdentifier) {
                return aliasService.packageAliasOf(packageIdentifier);
            }

            @Override
            public TypeAlias find(TypeIdentifier typeIdentifier) {
                return aliasService.typeAliasOf(typeIdentifier);
            }

            @Override
            public MethodAlias find(MethodIdentifier methodIdentifier) {
                return aliasService.methodAliasOf(methodIdentifier);
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
                prefixRemoveIdentifierFormatter,
                aliasService,
                applicationService,
                businessRuleService
        );
        ApplicationListController applicationListController = new ApplicationListController(
                prefixRemoveIdentifierFormatter,
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
                new AsmByteCodeFactory(),
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

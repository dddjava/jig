package org.dddjava.jig.infrastructure.configuration;

import org.dddjava.jig.application.service.AngleService;
import org.dddjava.jig.application.service.DependencyService;
import org.dddjava.jig.application.service.GlossaryService;
import org.dddjava.jig.application.service.ImplementationService;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypeFactory;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodeFactory;
import org.dddjava.jig.domain.model.implementation.datasource.SqlReader;
import org.dddjava.jig.domain.model.implementation.sourcecode.JapaneseReader;
import org.dddjava.jig.domain.model.japanese.JapaneseNameRepository;
import org.dddjava.jig.infrastructure.Layout;
import org.dddjava.jig.infrastructure.LocalProject;
import org.dddjava.jig.infrastructure.PrefixRemoveIdentifierFormatter;
import org.dddjava.jig.infrastructure.PropertyCharacterizedTypeFactory;
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

public class Configuration {

    final LocalProject localProject;
    final ImplementationService implementationService;
    final ClassListController classListController;
    final EnumUsageController enumUsageController;
    final PackageDependencyController packageDependencyController;
    final ServiceDiagramController serviceDiagramController;

    public Configuration(Layout layout, JigProperties properties) {
        JapaneseNameRepository japaneseNameRepository = new OnMemoryJapaneseNameRepository();
        CharacterizedTypeFactory characterizedTypeFactory = new PropertyCharacterizedTypeFactory(
                properties.getModelPattern(),
                properties.getRepositoryPattern()
        );
        JapaneseReader japaneseReader = new JavaparserJapaneseReader();
        GlossaryService glossaryService = new GlossaryService(
                japaneseReader,
                japaneseNameRepository
        );
        SqlReader sqlReader = new MyBatisSqlReader();
        ByteCodeFactory byteCodeFactory = new AsmByteCodeFactory();
        AngleService angleService = new AngleService();
        PrefixRemoveIdentifierFormatter typeIdentifierFormatter = new PrefixRemoveIdentifierFormatter(properties.getOutputOmitPrefix());
        ViewResolver viewResolver = new ViewResolver(typeIdentifierFormatter, MethodNodeLabelStyle.SIMPLE.name(), DiagramFormat.SVG.name());
        DependencyService dependencyService = new DependencyService();

        this.localProject = new LocalProject(layout);
        this.implementationService = new ImplementationService(
                byteCodeFactory,
                glossaryService,
                sqlReader,
                characterizedTypeFactory);
        this.classListController = new ClassListController(
                typeIdentifierFormatter,
                glossaryService,
                angleService
        );
        this.enumUsageController = new EnumUsageController(
                angleService,
                glossaryService,
                viewResolver
        );
        this.packageDependencyController = new PackageDependencyController(
                dependencyService,
                glossaryService,
                viewResolver,
                properties.getDepth()
        );
        this.serviceDiagramController = new ServiceDiagramController(
                angleService,
                glossaryService,
                viewResolver
        );
    }

    public LocalProject localProject() {
        return localProject;
    }

    public ImplementationService importService() {
        return implementationService;
    }
    public ClassListController classListController() {
        return classListController;
    }

    public EnumUsageController enumUsageController() {
        return enumUsageController;
    }

    public PackageDependencyController packageDependencyController() {
        return packageDependencyController;
    }

    public ServiceDiagramController serviceMethodCallHierarchyController() {
        return serviceDiagramController;
    }
}

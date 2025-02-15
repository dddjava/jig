package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.documents.diagrams.ArchitectureDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.information.Architecture;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethodDetector;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;
import org.dddjava.jig.domain.model.sources.JigRepository;

@Service
public class JigService {

    private final Architecture architecture;
    private final JigEventRepository jigEventRepository;

    public JigService(Architecture architecture, JigEventRepository jigEventRepository) {
        this.architecture = architecture;
        this.jigEventRepository = jigEventRepository;
    }

    public JigTypes jigTypes(JigRepository jigRepository) {
        return jigRepository.fetchJigTypes();
    }

    public Glossary glossary(JigRepository jigRepository) {
        return jigRepository.fetchGlossary();
    }

    public ArchitectureDiagram architectureDiagram(JigRepository jigRepository) {
        PackageBasedArchitecture packageBasedArchitecture = PackageBasedArchitecture.from(jigTypes(jigRepository));
        return new ArchitectureDiagram(packageBasedArchitecture);
    }

    public JigTypes coreDomainJigTypes(JigRepository jigRepository) {
        JigTypes coreDomainJigTypes = jigTypes(jigRepository).filter(architecture::isCoreDomain);
        if (coreDomainJigTypes.empty()) jigEventRepository.registerコアドメインが見つからない();
        return coreDomainJigTypes;
    }

    public PackageRelationDiagram packageDependencies(JigRepository jigRepository) {
        JigTypes coreDomainJigTypes = coreDomainJigTypes(jigRepository);
        if (coreDomainJigTypes.empty()) return PackageRelationDiagram.empty();
        return new PackageRelationDiagram(coreDomainJigTypes.typeIdentifiers().packageIdentifiers(), ClassRelations.internalRelation(coreDomainJigTypes));
    }

    public MethodSmellList methodSmells(JigRepository jigRepository) {
        return new MethodSmellList(coreDomainJigTypes(jigRepository));
    }

    public JigTypes categoryTypes(JigRepository jigRepository) {
        return coreDomainJigTypes(jigRepository).filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);
    }

    public CategoryDiagram categories(JigRepository jigRepository) {
        return CategoryDiagram.create(categoryTypes(jigRepository));
    }

    public JigTypes serviceTypes(JigRepository jigRepository) {
        return jigTypes(jigRepository).filter(jigType -> jigType.typeCategory() == TypeCategory.Usecase);
    }

    private ServiceMethods serviceMethods(JigRepository jigRepository) {
        JigTypes serviceJigTypes = serviceTypes(jigRepository);
        ServiceMethods serviceMethods = ServiceMethods.from(serviceJigTypes, MethodRelations.from(jigTypes(jigRepository)));
        if (serviceMethods.empty()) jigEventRepository.registerサービスが見つからない();
        return serviceMethods;
    }

    private DatasourceMethods repositoryMethods(JigRepository jigRepository) {
        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes(jigRepository));
        if (datasourceMethods.empty()) jigEventRepository.registerリポジトリが見つからない();
        return datasourceMethods;
    }

    public Entrypoints entrypoint(JigRepository jigRepository) {
        var entrypointMethodDetector = new EntrypointMethodDetector();
        Entrypoints from = Entrypoints.from(entrypointMethodDetector, jigTypes(jigRepository));
        if (from.isEmpty()) jigEventRepository.registerエントリーポイントが見つからない();
        return from;
    }

    public ServiceAngles serviceAngles(JigRepository jigRepository) {
        ServiceMethods serviceMethods = serviceMethods(jigRepository);
        DatasourceMethods datasourceMethods = repositoryMethods(jigRepository);
        return ServiceAngles.from(serviceMethods, entrypoint(jigRepository), datasourceMethods);
    }

    public DatasourceAngles datasourceAngles(JigRepository jigRepository) {
        JigTypes jigTypes = jigTypes(jigRepository);
        DatasourceMethods datasourceMethods = repositoryMethods(jigRepository);
        return new DatasourceAngles(datasourceMethods, jigRepository.jigDataProvider().fetchMybatisStatements(), MethodRelations.from(jigTypes));
    }

    public CategoryUsageDiagram categoryUsages(JigRepository jigRepository) {
        ServiceMethods serviceMethods = serviceMethods(jigRepository);
        JigTypes coreDomainJigTypes = coreDomainJigTypes(jigRepository);

        return new CategoryUsageDiagram(serviceMethods, coreDomainJigTypes);
    }

    public StringComparingMethodList stringComparing(JigRepository jigRepository) {
        Entrypoints entrypoints = entrypoint(jigRepository);
        ServiceMethods serviceMethods = serviceMethods(jigRepository);
        return StringComparingMethodList.createFrom(entrypoints, serviceMethods);
    }

    public void notifyReportInformation() {
        jigEventRepository.notifyWithLogger();
    }
}

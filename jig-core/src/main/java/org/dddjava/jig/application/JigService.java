package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.documents.diagrams.ArchitectureDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.information.Architecture;
import org.dddjava.jig.domain.model.information.JigDataProvider;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethodDetector;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.type.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.type.JigTypes;
import org.dddjava.jig.domain.model.information.type.TypeCategory;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;

@Service
public class JigService {

    private final Architecture architecture;
    private final JigEventRepository jigEventRepository;

    public JigService(Architecture architecture, JigEventRepository jigEventRepository) {
        this.architecture = architecture;
        this.jigEventRepository = jigEventRepository;
    }

    public JigTypes jigTypes(JigTypesRepository jigTypesRepository) {
        return jigTypesRepository.fetchJigTypes();
    }

    public Glossary glossary(JigDataProvider jigDataProvider) {
        return jigDataProvider.fetchGlossary();
    }

    public ArchitectureDiagram architectureDiagram(JigTypesRepository jigTypesRepository) {
        PackageBasedArchitecture packageBasedArchitecture = PackageBasedArchitecture.from(jigTypes(jigTypesRepository));
        return new ArchitectureDiagram(packageBasedArchitecture);
    }

    public JigTypes coreDomainJigTypes(JigTypesRepository jigTypesRepository) {
        JigTypes coreDomainJigTypes = jigTypes(jigTypesRepository).filter(architecture::isCoreDomain);
        if (coreDomainJigTypes.empty()) jigEventRepository.registerコアドメインが見つからない();
        return coreDomainJigTypes;
    }

    public PackageRelationDiagram packageDependencies(JigTypesRepository jigTypesRepository) {
        JigTypes coreDomainJigTypes = coreDomainJigTypes(jigTypesRepository);
        if (coreDomainJigTypes.empty()) return PackageRelationDiagram.empty();
        return new PackageRelationDiagram(coreDomainJigTypes.typeIdentifiers().packageIdentifiers(), ClassRelations.internalRelation(coreDomainJigTypes));
    }

    public MethodSmellList methodSmells(JigTypesRepository jigTypesRepository) {
        return new MethodSmellList(coreDomainJigTypes(jigTypesRepository));
    }

    public JigTypes categoryTypes(JigTypesRepository jigTypesRepository) {
        return coreDomainJigTypes(jigTypesRepository).filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);
    }

    public CategoryDiagram categories(JigTypesRepository jigTypesRepository) {
        return CategoryDiagram.create(categoryTypes(jigTypesRepository));
    }

    public JigTypes serviceTypes(JigTypesRepository jigTypesRepository) {
        return jigTypes(jigTypesRepository).filter(jigType -> jigType.typeCategory() == TypeCategory.Usecase);
    }

    private ServiceMethods serviceMethods(JigTypesRepository jigTypesRepository) {
        JigTypes serviceJigTypes = serviceTypes(jigTypesRepository);
        ServiceMethods serviceMethods = ServiceMethods.from(serviceJigTypes, MethodRelations.from(jigTypes(jigTypesRepository)));
        if (serviceMethods.empty()) jigEventRepository.registerサービスが見つからない();
        return serviceMethods;
    }

    private DatasourceMethods repositoryMethods(JigTypesRepository jigTypesRepository) {
        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes(jigTypesRepository));
        if (datasourceMethods.empty()) jigEventRepository.registerリポジトリが見つからない();
        return datasourceMethods;
    }

    public Entrypoint entrypoint(JigTypesRepository jigTypesRepository) {
        var entrypointMethodDetector = new EntrypointMethodDetector();
        Entrypoint from = Entrypoint.from(entrypointMethodDetector, jigTypes(jigTypesRepository));
        if (from.isEmpty()) jigEventRepository.registerエントリーポイントが見つからない();
        return from;
    }

    public ServiceAngles serviceAngles(JigTypesRepository jigTypesRepository) {
        ServiceMethods serviceMethods = serviceMethods(jigTypesRepository);
        DatasourceMethods datasourceMethods = repositoryMethods(jigTypesRepository);
        return ServiceAngles.from(serviceMethods, entrypoint(jigTypesRepository), datasourceMethods);
    }

    public DatasourceAngles datasourceAngles(JigDataProvider jigDataProvider) {
        JigTypes jigTypes = jigTypes(jigDataProvider);
        DatasourceMethods datasourceMethods = repositoryMethods(jigDataProvider);
        return new DatasourceAngles(datasourceMethods, jigDataProvider.fetchMybatisStatements(), MethodRelations.from(jigTypes));
    }

    public CategoryUsageDiagram categoryUsages(JigTypesRepository jigTypesRepository) {
        ServiceMethods serviceMethods = serviceMethods(jigTypesRepository);
        JigTypes coreDomainJigTypes = coreDomainJigTypes(jigTypesRepository);

        return new CategoryUsageDiagram(serviceMethods, coreDomainJigTypes);
    }

    public StringComparingMethodList stringComparing(JigTypesRepository jigTypesRepository) {
        Entrypoint entrypoint = entrypoint(jigTypesRepository);
        ServiceMethods serviceMethods = serviceMethods(jigTypesRepository);
        return StringComparingMethodList.createFrom(entrypoint, serviceMethods);
    }

    public void notifyReportInformation() {
        jigEventRepository.notifyWithLogger();
    }
}

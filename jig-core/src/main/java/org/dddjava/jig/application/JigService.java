package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.Architecture;
import org.dddjava.jig.domain.model.data.JigDataProvider;
import org.dddjava.jig.domain.model.data.classes.type.JigTypeValueKind;
import org.dddjava.jig.domain.model.data.classes.type.JigTypes;
import org.dddjava.jig.domain.model.data.classes.type.TypeCategory;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.documents.diagrams.ArchitectureDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethodDetector;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.relation.classes.ClassRelations;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;

@Service
public class JigService {

    private final Architecture architecture;
    private final JigReporter jigReporter;

    public JigService(Architecture architecture, JigReporter jigReporter) {
        this.architecture = architecture;
        this.jigReporter = jigReporter;
    }

    public JigTypes jigTypes(JigDataProvider jigDataProvider) {
        return jigDataProvider.fetchJigTypes();
    }

    public Terms terms(JigDataProvider jigDataProvider) {
        return jigDataProvider.fetchTerms();
    }

    public ArchitectureDiagram architectureDiagram(JigDataProvider jigDataProvider) {
        PackageBasedArchitecture packageBasedArchitecture = PackageBasedArchitecture.from(jigTypes(jigDataProvider));
        return new ArchitectureDiagram(packageBasedArchitecture);
    }

    public JigTypes coreDomainJigTypes(JigDataProvider jigDataProvider) {
        JigTypes coreDomainJigTypes = jigTypes(jigDataProvider).filter(architecture::isCoreDomain);
        jigReporter.registerコアドメインが見つからない();
        return coreDomainJigTypes;
    }

    public PackageRelationDiagram packageDependencies(JigDataProvider jigDataProvider) {
        JigTypes coreDomainJigTypes = coreDomainJigTypes(jigDataProvider);
        if (coreDomainJigTypes.empty()) return PackageRelationDiagram.empty();
        return new PackageRelationDiagram(coreDomainJigTypes.typeIdentifiers().packageIdentifiers(), ClassRelations.internalRelation(coreDomainJigTypes));
    }

    public MethodSmellList methodSmells(JigDataProvider jigDataProvider) {
        return new MethodSmellList(coreDomainJigTypes(jigDataProvider));
    }

    public JigTypes categoryTypes(JigDataProvider jigDataProvider) {
        return coreDomainJigTypes(jigDataProvider).filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);
    }

    public CategoryDiagram categories(JigDataProvider jigDataProvider) {
        return CategoryDiagram.create(categoryTypes(jigDataProvider));
    }

    public JigTypes serviceTypes(JigDataProvider jigDataProvider) {
        return jigTypes(jigDataProvider).filter(jigType -> jigType.typeCategory() == TypeCategory.Usecase);
    }

    private ServiceMethods serviceMethods(JigDataProvider jigDataProvider) {
        JigTypes serviceJigTypes = serviceTypes(jigDataProvider);
        ServiceMethods serviceMethods = ServiceMethods.from(serviceJigTypes, MethodRelations.from(jigTypes(jigDataProvider)));
        if (serviceMethods.empty()) jigReporter.registerサービスが見つからない();
        return serviceMethods;
    }

    private DatasourceMethods repositoryMethods(JigDataProvider jigDataProvider) {
        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes(jigDataProvider));
        if (datasourceMethods.empty()) jigReporter.registerリポジトリが見つからない();
        return datasourceMethods;
    }

    public Entrypoint entrypoint(JigDataProvider jigDataProvider) {
        var entrypointMethodDetector = new EntrypointMethodDetector();
        Entrypoint from = Entrypoint.from(entrypointMethodDetector, jigTypes(jigDataProvider));
        if (from.isEmpty()) jigReporter.registerエントリーポイントが見つからない();
        return from;
    }

    public ServiceAngles serviceAngles(JigDataProvider jigDataProvider) {
        ServiceMethods serviceMethods = serviceMethods(jigDataProvider);
        DatasourceMethods datasourceMethods = repositoryMethods(jigDataProvider);
        return ServiceAngles.from(serviceMethods, entrypoint(jigDataProvider), datasourceMethods);
    }

    public DatasourceAngles datasourceAngles(JigDataProvider jigDataProvider) {
        JigTypes jigTypes = jigTypes(jigDataProvider);
        DatasourceMethods datasourceMethods = repositoryMethods(jigDataProvider);
        return new DatasourceAngles(datasourceMethods, jigDataProvider.fetchMybatisStatements(), MethodRelations.from(jigTypes));
    }

    public CategoryUsageDiagram categoryUsages(JigDataProvider jigDataProvider) {
        ServiceMethods serviceMethods = serviceMethods(jigDataProvider);
        JigTypes coreDomainJigTypes = coreDomainJigTypes(jigDataProvider);

        return new CategoryUsageDiagram(serviceMethods, coreDomainJigTypes);
    }

    public StringComparingMethodList stringComparing(JigDataProvider jigDataProvider) {
        Entrypoint entrypoint = entrypoint(jigDataProvider);
        ServiceMethods serviceMethods = serviceMethods(jigDataProvider);
        return StringComparingMethodList.createFrom(entrypoint, serviceMethods);
    }

    public void notifyReportInformation() {
        jigReporter.notifyWithLogger();
    }

    public void initialize(JigDataProvider jigDataProvider) {
        jigTypes(jigDataProvider);
    }
}

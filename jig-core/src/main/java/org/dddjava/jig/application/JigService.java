package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.documents.diagrams.ArchitectureDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.class_.TypeCategory;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;

@Service
public class JigService {

    private final Architecture architecture;
    private final JigReporter jigReporter;

    public JigService(Architecture architecture) {
        this.architecture = architecture;
        this.jigReporter = new JigReporter();
    }

    public JigTypes jigTypes(JigSource jigSource) {
        return jigSource.typeFacts().jigTypes();
    }

    public Terms terms(JigSource jigSource) {
        return jigSource.terms();
    }

    public ArchitectureDiagram architectureDiagram(JigSource jigSource) {
        PackageBasedArchitecture packageBasedArchitecture = PackageBasedArchitecture.from(jigTypes(jigSource));
        return new ArchitectureDiagram(packageBasedArchitecture);
    }

    public JigTypes domainCoreTypes(JigSource jigSource) {
        JigTypes domainCoreTypes = jigTypes(jigSource).filter(architecture::isDomainCore);
        jigReporter.registerドメインコアが見つからない();
        return domainCoreTypes;
    }

    public PackageRelationDiagram packageDependencies(JigSource jigSource) {
        JigTypes domainCoreTypes = domainCoreTypes(jigSource);
        if (domainCoreTypes.empty()) return PackageRelationDiagram.empty();
        return new PackageRelationDiagram(domainCoreTypes.typeIdentifiers().packageIdentifiers(), domainCoreTypes.internalClassRelations());
    }

    public MethodSmellList methodSmells(JigSource jigSource) {
        return new MethodSmellList(domainCoreTypes(jigSource));
    }

    public CategoryTypes categoryTypes(JigSource jigSource) {
        return CategoryTypes.from(domainCoreTypes(jigSource));
    }

    public CategoryDiagram categories(JigSource jigSource) {
        CategoryTypes categoryTypes = categoryTypes(jigSource);
        return CategoryDiagram.create(categoryTypes);
    }

    public JigTypes serviceTypes(JigSource jigSource) {
        return jigTypes(jigSource).filter(jigType -> jigType.typeCategory() == TypeCategory.Application);
    }

    private ServiceMethods serviceMethods(JigSource jigSource) {
        JigTypes serviceJigTypes = serviceTypes(jigSource);
        ServiceMethods serviceMethods = ServiceMethods.from(serviceJigTypes, jigTypes(jigSource));
        if (serviceMethods.empty()) jigReporter.registerサービスが見つからない();
        return serviceMethods;
    }

    private DatasourceMethods repositoryMethods(JigSource jigSource) {
        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes(jigSource));
        if (datasourceMethods.empty()) jigReporter.registerリポジトリが見つからない();
        return datasourceMethods;
    }

    public Entrypoint entrypoint(JigSource jigSource) {
        Entrypoint from = Entrypoint.from(jigTypes(jigSource));
        if (from.isEmpty()) jigReporter.registerエントリーポイントが見つからない();
        return from;
    }

    public ServiceAngles serviceAngles(JigSource jigSource) {
        ServiceMethods serviceMethods = serviceMethods(jigSource);
        DatasourceMethods datasourceMethods = repositoryMethods(jigSource);
        return ServiceAngles.from(serviceMethods, entrypoint(jigSource), datasourceMethods);
    }

    public DatasourceAngles datasourceAngles(JigSource jigSource) {
        JigTypes jigTypes = jigTypes(jigSource);
        DatasourceMethods datasourceMethods = repositoryMethods(jigSource);
        return new DatasourceAngles(datasourceMethods, jigSource.sqls(), jigTypes);
    }

    public CategoryUsageDiagram categoryUsages(JigSource jigSource) {
        CategoryTypes categoryTypes = categoryTypes(jigSource);
        ServiceMethods serviceMethods = serviceMethods(jigSource);
        JigTypes domainCoreJigTypes = domainCoreTypes(jigSource);

        return new CategoryUsageDiagram(serviceMethods, categoryTypes, domainCoreJigTypes);
    }

    public StringComparingMethodList stringComparing(JigSource jigSource) {
        Entrypoint entrypoint = entrypoint(jigSource);
        ServiceMethods serviceMethods = serviceMethods(jigSource);
        return StringComparingMethodList.createFrom(entrypoint, serviceMethods);
    }

    public void notifyReportInformation() {
        jigReporter.notifyWithLogger();
    }
}

package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.data.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.data.term.Terms;
import org.dddjava.jig.domain.model.documents.diagrams.ArchitectureDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.CategoryUsageDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.PackageRelationDiagram;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.architecture.PackageBasedArchitecture;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class JigService {

    static Logger logger = LoggerFactory.getLogger(JigService.class);
    private final Architecture architecture;

    public JigService(Architecture architecture) {
        this.architecture = architecture;
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellList methodSmells(JigSource jigSource) {
        return new MethodSmellList(domainCoreTypes(jigSource));
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryDiagram categories(JigSource jigSource) {
        CategoryTypes categoryTypes = categoryTypes(jigSource);
        return CategoryDiagram.create(categoryTypes);
    }

    public CategoryTypes categoryTypes(JigSource jigSource) {
        return CategoryTypes.from(domainCoreTypes(jigSource));
    }

    /**
     * 区分使用図
     */
    public CategoryUsageDiagram categoryUsages(JigSource jigSource) {
        CategoryTypes categoryTypes = categoryTypes(jigSource);
        ServiceMethods serviceMethods = serviceMethods(jigSource);
        JigTypes domainCoreJigTypes = domainCoreTypes(jigSource);

        return new CategoryUsageDiagram(serviceMethods, categoryTypes, domainCoreJigTypes);
    }

    private ServiceMethods serviceMethods(JigSource jigSource) {
        TypeFacts typeFacts = jigSource.typeFacts();
        JigTypes serviceJigTypes = serviceTypes(jigSource);
        return ServiceMethods.from(serviceJigTypes, typeFacts.toMethodRelations());
    }

    public Terms terms(JigSource jigSource) {
        return jigSource.terms();
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(JigSource jigSource) {
        ServiceMethods serviceMethods = serviceMethods(jigSource);

        if (serviceMethods.empty()) {
            logger.warn(Warning.サービスメソッドが見つからないので出力されない通知.localizedMessage());
        }

        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes(jigSource));

        return ServiceAngles.from(serviceMethods, entrypoint(jigSource), datasourceMethods);
    }

    /**
     * データソースを分析する
     */
    public DatasourceAngles datasourceAngles(JigSource jigSource) {
        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes(jigSource));

        if (datasourceMethods.empty()) {
            logger.warn(Warning.リポジトリが見つからないので出力されない通知.localizedMessage());
        }

        MethodRelations methodRelations = jigSource.typeFacts().toMethodRelations();
        return new DatasourceAngles(datasourceMethods, jigSource.sqls(), methodRelations);
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingMethodList stringComparing(JigSource jigSource) {
        Entrypoint entrypoint = entrypoint(jigSource);
        ServiceMethods serviceMethods = serviceMethods(jigSource);

        return StringComparingMethodList.createFrom(entrypoint, serviceMethods);
    }

    public ArchitectureDiagram architectureDiagram(JigSource jigSource) {
        PackageBasedArchitecture packageBasedArchitecture = PackageBasedArchitecture.from(jigTypes(jigSource));
        return new ArchitectureDiagram(packageBasedArchitecture);
    }

    public Entrypoint entrypoint(JigSource jigSource) {
        TypeFacts typeFacts = jigSource.typeFacts();
        return Entrypoint.from(typeFacts.jigTypes(), typeFacts.toMethodRelations());
    }

    /**
     * パッケージの関連を取得する
     */
    public PackageRelationDiagram packageDependencies(JigSource jigSource) {
        JigTypes domainCoreTypes = domainCoreTypes(jigSource);

        if (domainCoreTypes.empty()) {
            logger.warn(Warning.ビジネスルールが見つからないので出力されない通知.localizedMessage());
            return PackageRelationDiagram.empty();
        }

        return new PackageRelationDiagram(domainCoreTypes.typeIdentifiers().packageIdentifiers(), domainCoreTypes.internalClassRelations());
    }

    public JigTypes jigTypes(JigSource jigSource) {
        return jigSource.typeFacts().jigTypes();
    }

    public JigTypes serviceTypes(JigSource jigSource) {
        return jigTypes(jigSource).filter(architecture::isService);
    }

    public JigTypes domainCoreTypes(JigSource jigSource) {
        return jigTypes(jigSource).filter(architecture::isDomainCore);
    }
}

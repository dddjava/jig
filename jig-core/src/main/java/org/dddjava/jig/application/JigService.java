package org.dddjava.jig.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.term.Glossary;
import org.dddjava.jig.domain.model.information.Architecture;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethodDetector;
import org.dddjava.jig.domain.model.information.inputs.Entrypoints;
import org.dddjava.jig.domain.model.information.outputs.DatasourceMethods;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;
import org.dddjava.jig.domain.model.information.types.relations.TypeRelationships;
import org.dddjava.jig.domain.model.knowledge.adapter.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.core.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.core.usecases.StringComparingMethodList;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmellList;

@Service
public class JigService {

    private final Architecture architecture;
    private final JigEventRepository jigEventRepository;
    private final Cache<String, JigTypes> jigTypesCache;

    public JigService(Architecture architecture, JigEventRepository jigEventRepository) {
        this.architecture = architecture;
        this.jigEventRepository = jigEventRepository;
        this.jigTypesCache = Caffeine.newBuilder().build();
    }

    /**
     * 全JigTypeを取得する
     *
     * ここで取得されるJigTypesにはsourceで定義された全てのJigTypeが含まれる。
     * ライブラリのクラスなど、使用しているだけのものは入らない。
     */
    public JigTypes jigTypes(JigRepository jigRepository) {
        return jigRepository.fetchJigTypes();
    }

    /**
     * 用語集を取得する
     */
    public Glossary glossary(JigRepository jigRepository) {
        return jigRepository.fetchGlossary();
    }

    /**
     * コアドメインのみのJigTypesを取得する
     *
     * コアドメインは実行時に指定するパターンなどによって識別する。
     */
    public JigTypes coreDomainJigTypes(JigRepository jigRepository) {
        return jigTypesCache.get("core", key -> {
            JigTypes coreDomainJigTypes = jigTypes(jigRepository).filter(architecture::isCoreDomain);
            if (coreDomainJigTypes.empty()) jigEventRepository.registerコアドメインが見つからない();
            return coreDomainJigTypes;
        });
    }

    public MethodSmellList methodSmells(JigRepository jigRepository) {
        return new MethodSmellList(coreDomainJigTypes(jigRepository));
    }

    public JigTypes categoryTypes(JigRepository jigRepository) {
        return jigTypesCache.get("category", key -> {
            return coreDomainJigTypes(jigRepository).filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);
        });
    }

    public JigTypes serviceTypes(JigRepository jigRepository) {
        return jigTypesCache.get("service", key -> {
            return jigTypes(jigRepository).filter(jigType -> jigType.typeCategory() == TypeCategory.Usecase);
        });
    }

    public ServiceMethods serviceMethods(JigRepository jigRepository) {
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

    public StringComparingMethodList stringComparing(JigRepository jigRepository) {
        Entrypoints entrypoints = entrypoint(jigRepository);
        ServiceMethods serviceMethods = serviceMethods(jigRepository);
        return StringComparingMethodList.createFrom(entrypoints, serviceMethods);
    }

    public void notifyReportInformation() {
        jigEventRepository.notifyWithLogger();
    }

    public JigTypesWithRelationships coreDomainJigTypesWithRelationships(JigRepository jigRepository) {
        JigTypes coreDomainJigTypes = coreDomainJigTypes(jigRepository);
        TypeRelationships typeRelationships = TypeRelationships.internalRelation(coreDomainJigTypes);
        return new JigTypesWithRelationships(coreDomainJigTypes, typeRelationships);
    }
}

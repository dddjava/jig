package org.dddjava.jig.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.terms.TermId;
import org.dddjava.jig.domain.model.data.terms.TermKind;
import org.dddjava.jig.domain.model.information.Architecture;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inputs.EntrypointMethodDetector;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.outputs.OutputAdapters;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngles;
import org.dddjava.jig.domain.model.knowledge.insight.Insights;
import org.dddjava.jig.domain.model.knowledge.insight.MethodInsight;
import org.dddjava.jig.domain.model.knowledge.module.JigPackage;
import org.dddjava.jig.domain.model.knowledge.module.JigPackages;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmells;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.StringComparingMethodList;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.groupingBy;

@Service
public class JigService {

    private final Architecture architecture;
    private final JigEventRepository jigEventRepository;

    // 何度も呼ばれる計算量の多いメソッドをキャッシュするためのフィールド
    // 現状は引数に揺れがないので、キャッシュキーはメソッド名にしておく
    private final Cache<String, JigTypes> jigTypesCache;
    private final Cache<String, CoreTypesAndRelations> JigTypesWithRelationshipsCache;

    public JigService(Architecture architecture, JigEventRepository jigEventRepository) {
        this.architecture = architecture;
        this.jigEventRepository = jigEventRepository;

        if (System.getProperty("jig.debug", "false").equals("true")) {
            this.jigTypesCache = Caffeine.newBuilder().recordStats().build();
            this.JigTypesWithRelationshipsCache = Caffeine.newBuilder().recordStats().build();

            CaffeineCacheMetrics.monitor(Metrics.globalRegistry, jigTypesCache, "jigTypesCache");
            CaffeineCacheMetrics.monitor(Metrics.globalRegistry, JigTypesWithRelationshipsCache, "JigTypesWithRelationshipsCache");
        } else {
            this.jigTypesCache = Caffeine.newBuilder().build();
            this.JigTypesWithRelationshipsCache = Caffeine.newBuilder().build();
        }
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
        return jigTypesCache.get("coreDomainJigTypes", key -> {
            JigTypes coreDomainJigTypes = jigTypes(jigRepository).filter(architecture::isCoreDomain);
            if (coreDomainJigTypes.empty()) jigEventRepository.registerコアドメインが見つからない();
            return coreDomainJigTypes;
        });
    }

    public MethodSmells methodSmells(JigRepository jigRepository) {
        return MethodSmells.from(coreDomainJigTypes(jigRepository));
    }

    public JigTypes categoryTypes(JigRepository jigRepository) {
        return jigTypesCache.get("categoryTypes", key -> {
            return coreDomainJigTypes(jigRepository).filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分);
        });
    }

    public JigTypes serviceTypes(JigRepository jigRepository) {
        return jigTypesCache.get("serviceTypes", key -> {
            return jigTypes(jigRepository).filter(jigType -> jigType.typeCategory() == TypeCategory.InputPort);
        });
    }

    public ServiceMethods serviceMethods(JigRepository jigRepository) {
        JigTypes serviceJigTypes = serviceTypes(jigRepository);
        ServiceMethods serviceMethods = ServiceMethods.from(serviceJigTypes, MethodRelations.from(jigTypes(jigRepository)));
        if (serviceMethods.empty()) jigEventRepository.registerサービスが見つからない();
        return serviceMethods;
    }

    private OutputImplementations repositoryMethods(JigRepository jigRepository) {
        var jigTypes = jigTypes(jigRepository);
        var outputPorts = OutputAdapters.from(jigTypes);
        OutputImplementations outputImplementations = OutputImplementations.from(jigTypes, outputPorts);
        if (outputImplementations.empty()) jigEventRepository.registerリポジトリが見つからない();
        return outputImplementations;
    }

    public InputAdapters entrypoint(JigRepository jigRepository) {
        var entrypointMethodDetector = new EntrypointMethodDetector();
        InputAdapters from = InputAdapters.from(entrypointMethodDetector, jigTypes(jigRepository));
        if (from.isEmpty()) jigEventRepository.registerエントリーポイントが見つからない();
        return from;
    }

    public ServiceAngles serviceAngles(JigRepository jigRepository) {
        ServiceMethods serviceMethods = serviceMethods(jigRepository);
        OutputImplementations outputImplementations = repositoryMethods(jigRepository);
        return ServiceAngles.from(serviceMethods, entrypoint(jigRepository), outputImplementations);
    }

    public DatasourceAngles datasourceAngles(JigRepository jigRepository) {
        JigTypes jigTypes = jigTypes(jigRepository);
        OutputImplementations outputImplementations = repositoryMethods(jigRepository);
        return new DatasourceAngles(outputImplementations, jigRepository.jigDataProvider().fetchMybatisStatements(), MethodRelations.from(jigTypes));
    }

    public StringComparingMethodList stringComparing(JigRepository jigRepository) {
        InputAdapters inputAdapters = entrypoint(jigRepository);
        ServiceMethods serviceMethods = serviceMethods(jigRepository);
        return StringComparingMethodList.createFrom(inputAdapters, serviceMethods);
    }

    public CoreTypesAndRelations coreTypesAndRelations(JigRepository jigRepository) {
        return JigTypesWithRelationshipsCache.get("coreTypesAndRelations", key -> {
            JigTypes jigTypes = coreDomainJigTypes(jigRepository);
            TypeRelationships typeRelationships = TypeRelationships.internalRelation(jigTypes);
            return new CoreTypesAndRelations(jigTypes, typeRelationships);
        });
    }

    public JigPackages packages(JigRepository jigRepository) {
        JigTypes jigTypes = jigTypes(jigRepository);
        Glossary glossary = glossary(jigRepository);

        Map<PackageId, List<JigType>> packageAndJigTypes = jigTypes.orderedStream()
                .collect(groupingBy(JigType::packageId));

        List<Term> packageTerms = glossary.terms().stream()
                .filter(term -> term.termKind() == TermKind.パッケージ)
                .toList();

        List<JigPackage> jigPackages = Stream.concat(
                        packageAndJigTypes.keySet().stream(),
                        packageTerms.stream().map(Term::id).map(TermId::asText).map(PackageId::valueOf))
                .distinct()
                .map(packageId -> {
                    var packageTerm = glossary.termOf(packageId.asText(), TermKind.パッケージ);
                    return new JigPackage(packageId, packageTerm, packageAndJigTypes.getOrDefault(packageId, List.of()));
                })
                .toList();

        return new JigPackages(jigPackages);
    }

    public Insights insights(JigRepository repository) {
        return new Insights(
                jigTypes(repository).orderedStream()
                        .flatMap(JigType::allJigMethodStream)
                        .map(MethodInsight::new)
                        .toList(),
                glossary(repository)
        );
    }
}

package org.dddjava.jig.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.information.core.CoreDomainCondition;
import org.dddjava.jig.domain.model.information.core.CoreDomainJigTypes;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.information.types.TypeCategory;
import org.dddjava.jig.domain.model.knowledge.smell.MethodSmells;

/**
 * 型・用語などの基本取得/フィルタに関する問い合わせサービス
 */
@Service
public class TypesQueryService {

    private final CoreDomainCondition coreDomainCondition;
    private final JigEventRepository jigEventRepository;

    private final Cache<String, JigTypes> jigTypesCache;
    private final Cache<String, CoreDomainJigTypes> coreDomainJigTypesCache;

    public TypesQueryService(CoreDomainCondition coreDomainCondition, JigEventRepository jigEventRepository) {
        this.coreDomainCondition = coreDomainCondition;
        this.jigEventRepository = jigEventRepository;

        if (System.getProperty("jig.debug", "false").equals("true")) {
            this.jigTypesCache = Caffeine.newBuilder().recordStats().build();
            this.coreDomainJigTypesCache = Caffeine.newBuilder().recordStats().build();
            CaffeineCacheMetrics.monitor(Metrics.globalRegistry, jigTypesCache, "jigTypesCache");
            CaffeineCacheMetrics.monitor(Metrics.globalRegistry, coreDomainJigTypesCache, "coreDomainJigTypesCache");
        } else {
            this.jigTypesCache = Caffeine.newBuilder().build();
            this.coreDomainJigTypesCache = Caffeine.newBuilder().build();
        }
    }

    public JigTypes jigTypes(JigRepository jigRepository) {
        return jigRepository.fetchJigTypes();
    }

    public Glossary glossary(JigRepository jigRepository) {
        return jigRepository.fetchGlossary();
    }

    public CoreDomainJigTypes coreDomainJigTypes(JigRepository jigRepository) {
        return coreDomainJigTypesCache.get("coreDomainJigTypes", key -> {
            var jigTypes = jigTypes(jigRepository);
            var coreDomainJigTypes = coreDomainCondition.coreDomainJigTypes(jigTypes);
            if (coreDomainJigTypes.isEmpty()) jigEventRepository.registerコアドメインが見つからない();
            return coreDomainJigTypes;
        });
    }

    public MethodSmells methodSmells(JigRepository jigRepository) {
        return MethodSmells.from(coreDomainJigTypes(jigRepository).jigTypes());
    }

    public JigTypes serviceTypes(JigRepository jigRepository) {
        return jigTypesCache.get("serviceTypes", key ->
                jigTypes(jigRepository).filter(jigType -> jigType.typeCategory() == TypeCategory.InboundPort)
        );
    }
}

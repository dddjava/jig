package org.dddjava.jig.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.data.terms.Glossary;
import org.dddjava.jig.domain.model.documents.diagrams.CoreTypesAndRelations;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.core.CoreDomainCondition;
import org.dddjava.jig.domain.model.information.relation.types.TypeRelationships;
import org.dddjava.jig.domain.model.information.types.JigTypeValueKind;
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
    private final Cache<String, CoreTypesAndRelations> jigTypesWithRelationshipsCache;

    public TypesQueryService(CoreDomainCondition coreDomainCondition, JigEventRepository jigEventRepository) {
        this.coreDomainCondition = coreDomainCondition;
        this.jigEventRepository = jigEventRepository;

        if (System.getProperty("jig.debug", "false").equals("true")) {
            this.jigTypesCache = Caffeine.newBuilder().recordStats().build();
            this.jigTypesWithRelationshipsCache = Caffeine.newBuilder().recordStats().build();
            CaffeineCacheMetrics.monitor(Metrics.globalRegistry, jigTypesCache, "jigTypesCache");
            CaffeineCacheMetrics.monitor(Metrics.globalRegistry, jigTypesWithRelationshipsCache, "jigTypesWithRelationshipsCache");
        } else {
            this.jigTypesCache = Caffeine.newBuilder().build();
            this.jigTypesWithRelationshipsCache = Caffeine.newBuilder().build();
        }
    }

    public JigTypes jigTypes(JigRepository jigRepository) {
        return jigRepository.fetchJigTypes();
    }

    public Glossary glossary(JigRepository jigRepository) {
        return jigRepository.fetchGlossary();
    }

    public JigTypes coreDomainJigTypes(JigRepository jigRepository) {
        return jigTypesCache.get("coreDomainJigTypes", key -> {
            var jigTypes = jigTypes(jigRepository);
            var coreDomainJigTypes = coreDomainCondition.coreDomainJigTypes(jigTypes);
            if (coreDomainJigTypes.empty()) jigEventRepository.registerコアドメインが見つからない();
            return coreDomainJigTypes.jigTypes();
        });
    }

    public MethodSmells methodSmells(JigRepository jigRepository) {
        return MethodSmells.from(coreDomainJigTypes(jigRepository));
    }

    public JigTypes categoryTypes(JigRepository jigRepository) {
        return jigTypesCache.get("categoryTypes", key ->
                coreDomainJigTypes(jigRepository).filter(jigType -> jigType.toValueKind() == JigTypeValueKind.区分)
        );
    }

    public JigTypes serviceTypes(JigRepository jigRepository) {
        return jigTypesCache.get("serviceTypes", key ->
                jigTypes(jigRepository).filter(jigType -> jigType.typeCategory() == TypeCategory.InputPort)
        );
    }

    public CoreTypesAndRelations coreTypesAndRelations(JigRepository jigRepository) {
        return jigTypesWithRelationshipsCache.get("coreTypesAndRelations", key -> {
            var jigTypes = coreDomainJigTypes(jigRepository);
            var typeRelationships = TypeRelationships.internalRelation(jigTypes);
            return new CoreTypesAndRelations(jigTypes, typeRelationships);
        });
    }
}

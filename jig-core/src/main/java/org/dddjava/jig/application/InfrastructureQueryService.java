package org.dddjava.jig.application;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.binder.cache.CaffeineCacheMetrics;
import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngles;

@Service
public class InfrastructureQueryService {

    private final JigEventRepository jigEventRepository;
    private final TypesQueryService typesQueryService;

    private final Cache<JigRepository, OutboundAdapters> outboundAdaptersCache;

    public InfrastructureQueryService(JigEventRepository jigEventRepository, TypesQueryService typesQueryService) {
        this.jigEventRepository = jigEventRepository;
        this.typesQueryService = typesQueryService;

        if (System.getProperty("jig.debug", "false").equals("true")) {
            this.outboundAdaptersCache = Caffeine.newBuilder().recordStats().build();
            CaffeineCacheMetrics.monitor(Metrics.globalRegistry, outboundAdaptersCache, "outboundAdaptersCache");
        } else {
            this.outboundAdaptersCache = Caffeine.newBuilder().build();
        }
    }

    public OutboundAdapters outboundAdapters(JigRepository jigRepository) {
        return outboundAdaptersCache.get(jigRepository, key -> {
            var jigTypes = typesQueryService.jigTypes(jigRepository);
            var accessorRepositories = jigRepository.externalAccessorRepositories();
            var outboundAdapters = OutboundAdapters.from(jigTypes, accessorRepositories);
            if (outboundAdapters.isEmpty()) jigEventRepository.registerリポジトリが見つからない();
            return outboundAdapters;
        });
    }

    public DatasourceAngles datasourceAngles(JigRepository jigRepository) {
        var outboundAdapters = outboundAdapters(jigRepository);
        return DatasourceAngles.from(outboundAdapters, jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), typesQueryService.methodRelations(jigRepository));
    }
}

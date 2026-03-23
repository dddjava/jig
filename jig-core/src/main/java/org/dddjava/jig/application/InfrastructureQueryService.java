package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.knowledge.datasource.DatasourceAngles;

@Service
public class InfrastructureQueryService {

    private final JigEventRepository jigEventRepository;
    private final TypesQueryService typesQueryService;

    public InfrastructureQueryService(JigEventRepository jigEventRepository, TypesQueryService typesQueryService) {
        this.jigEventRepository = jigEventRepository;
        this.typesQueryService = typesQueryService;
    }

    public OutboundAdapters outboundAdapters(JigRepository jigRepository) {
        var jigTypes = typesQueryService.jigTypes(jigRepository);
        var accessorRepositories = jigRepository.externalAccessorRepositories();
        var outboundAdapters = OutboundAdapters.from(jigTypes, accessorRepositories);
        if (outboundAdapters.isEmpty()) jigEventRepository.registerリポジトリが見つからない();
        return outboundAdapters;
    }

    public DatasourceAngles datasourceAngles(JigRepository jigRepository) {
        var jigTypes = typesQueryService.jigTypes(jigRepository);
        var outboundAdapters = outboundAdapters(jigRepository);
        return DatasourceAngles.from(outboundAdapters, jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), MethodRelations.from(jigTypes));
    }
}

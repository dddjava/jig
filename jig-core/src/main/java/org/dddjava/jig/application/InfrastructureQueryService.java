package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.pair.OutboundImplementations;
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
        return OutboundAdapters.from(jigTypes, accessorRepositories);
    }

    public OutboundImplementations outputImplementations(JigRepository jigRepository) {
        var outboundAdapters = outboundAdapters(jigRepository);
        var outputImplementations = OutboundImplementations.from(outboundAdapters);
        if (outputImplementations.empty()) jigEventRepository.registerリポジトリが見つからない();
        return outputImplementations;
    }

    public DatasourceAngles datasourceAngles(JigRepository jigRepository) {
        var jigTypes = typesQueryService.jigTypes(jigRepository);
        var outputImplementations = outputImplementations(jigRepository);
        return DatasourceAngles.from(outputImplementations, jigRepository.externalAccessorRepositories().persistenceAccessorRepository(), MethodRelations.from(jigTypes));
    }
}

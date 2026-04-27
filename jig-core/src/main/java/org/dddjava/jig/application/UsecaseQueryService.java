package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inbound.InboundAdapters;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;

@Service
public class UsecaseQueryService {

    private final JigEventRepository jigEventRepository;
    private final TypesQueryService typesQueryService;
    private final InfrastructureQueryService infrastructureQueryService;

    public UsecaseQueryService(JigEventRepository jigEventRepository,
                               TypesQueryService typesQueryService,
                               InfrastructureQueryService infrastructureQueryService) {
        this.jigEventRepository = jigEventRepository;
        this.typesQueryService = typesQueryService;
        this.infrastructureQueryService = infrastructureQueryService;
    }

    public InboundAdapters inboundAdapters(JigRepository jigRepository) {
        var inboundAdapters = InboundAdapters.from(typesQueryService.jigTypes(jigRepository));
        if (inboundAdapters.isEmpty()) jigEventRepository.registerエントリーポイントが見つからない();
        return inboundAdapters;
    }

    public ServiceMethods serviceMethods(JigRepository jigRepository) {
        JigTypes serviceJigTypes = typesQueryService.serviceTypes(jigRepository);
        ServiceMethods serviceMethods = ServiceMethods.from(serviceJigTypes, MethodRelations.from(typesQueryService.jigTypes(jigRepository)));
        if (serviceMethods.isEmpty()) jigEventRepository.registerサービスが見つからない();
        return serviceMethods;
    }

    public ServiceAngles serviceAngles(JigRepository jigRepository) {
        var serviceMethods = serviceMethods(jigRepository);
        var outboundAdapters = infrastructureQueryService.outboundAdapters(jigRepository);
        return ServiceAngles.from(serviceMethods, inboundAdapters(jigRepository), outboundAdapters);
    }

}

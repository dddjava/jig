package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.applications.ServiceMethods;
import org.dddjava.jig.domain.model.information.inbound.InputAdapters;
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

    public InputAdapters inputAdapters(JigRepository jigRepository) {
        var inputAdapters = InputAdapters.from(typesQueryService.jigTypes(jigRepository));
        if (inputAdapters.isEmpty()) jigEventRepository.registerエントリーポイントが見つからない();
        return inputAdapters;
    }

    public ServiceMethods serviceMethods(JigRepository jigRepository) {
        JigTypes serviceJigTypes = typesQueryService.serviceTypes(jigRepository);
        ServiceMethods serviceMethods = ServiceMethods.from(serviceJigTypes, MethodRelations.from(typesQueryService.jigTypes(jigRepository)));
        if (serviceMethods.empty()) jigEventRepository.registerサービスが見つからない();
        return serviceMethods;
    }

    public ServiceAngles serviceAngles(JigRepository jigRepository) {
        var serviceMethods = serviceMethods(jigRepository);
        var outboundAdapters = infrastructureQueryService.outboundAdapters(jigRepository);
        return ServiceAngles.from(serviceMethods, inputAdapters(jigRepository), outboundAdapters);
    }

}

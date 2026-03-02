package org.dddjava.jig.application;

import org.dddjava.jig.annotation.Service;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outputs.OutputAdapters;
import org.dddjava.jig.domain.model.information.outputs.OutputImplementations;
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

    public OutputImplementations outputImplementations(JigRepository jigRepository) {
        var jigTypes = typesQueryService.jigTypes(jigRepository);

        var sqlStatements = jigRepository.jigDataProvider().fetchSqlStatements();
        var outputAdapters = OutputAdapters.from(jigTypes, sqlStatements);
        var outputImplementations = OutputImplementations.from(jigTypes, outputAdapters);
        if (outputImplementations.empty()) jigEventRepository.registerリポジトリが見つからない();
        return outputImplementations;
    }

    public DatasourceAngles datasourceAngles(JigRepository jigRepository) {
        var jigTypes = typesQueryService.jigTypes(jigRepository);
        var outputImplementations = outputImplementations(jigRepository);
        return DatasourceAngles.from(outputImplementations, jigRepository.jigDataProvider().fetchSqlStatements(), MethodRelations.from(jigTypes));
    }
}

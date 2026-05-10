package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.knowledge.external.ExternalDependencyDiagram;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部依存図（external-dependency-data.js）
 */
public class ExternalDependencyDataAdapter implements DataAdapter {

    private final JigService jigService;

    public ExternalDependencyDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "externalDependencyData";
    }

    @Override
    public String dataFileName() {
        return "external-dependency-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        ExternalDependencyDiagram diagram = jigService.externalDependencyDiagram(jigRepository);

        var groups = diagram.groups().stream()
                .map(group -> Json.object("id", group.id())
                        .and("displayName", group.displayName())
                        .and("isJdk", group.isJdk())
                        .and("samplePackages", Json.array(new ArrayList<>(group.samplePackages())))
                        .and("usingClasses", Json.array(new ArrayList<>(group.usingClasses()))))
                .toList();

        var edges = diagram.edges().stream()
                .map(edge -> Json.object("from", edge.from()).and("to", edge.to()))
                .toList();

        return Json.object("internalPackages", Json.array(List.copyOf(diagram.internalPackageFqns())))
                .and("externalGroups", Json.arrayObjects(groups))
                .and("relations", Json.arrayObjects(edges))
                .build();
    }
}

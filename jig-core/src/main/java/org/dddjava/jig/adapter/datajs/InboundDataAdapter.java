package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.inbound.Entrypoint;
import org.dddjava.jig.domain.model.information.inbound.InboundAdapters;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * 入力インタフェース（inbound-data.js）
 */
public class InboundDataAdapter implements DataAdapter {

    private final JigService jigService;

    public InboundDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "inboundData";
    }

    @Override
    public String dataFileName() {
        return "inbound-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        var contextJigTypes = jigService.jigTypes(jigRepository);
        var inboundAdapters = jigService.inboundAdapters(jigRepository);
        return buildInboundJson(inboundAdapters, contextJigTypes);
    }

    public static String buildInboundJson(InboundAdapters inboundAdapters, JigTypes jigTypes) {
        List<JsonObjectBuilder> controllerList = new ArrayList<>();

        MethodRelations springComponentMethodRelations = inboundAdapters.methodRelations().filterApplicationComponent(jigTypes).inlineLambda();

        inboundAdapters.groups().forEach(inboundAdapter -> {
            var jigType = inboundAdapter.jigType();

            List<JsonObjectBuilder> edges = new ArrayList<>();
            List<JsonObjectBuilder> entrypointList = new ArrayList<>();

            inboundAdapter.entrypoints().forEach(entrypoint -> {
                var entrypointMethodId = entrypoint.jigMethod().jigMethodId();

                MethodRelations declaredMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethodId, jigTypes::isService);
                declaredMethodRelations.relations().forEach(relation -> {
                    edges.add(Json.object("from", relation.from().fqn())
                            .and("to", relation.to().fqn()));
                });

                entrypointList.add(JsonSupport.buildMethodJson(entrypoint.jigMethod())
                        .and("entrypointType", entrypoint.entrypointType().name())
                        .and("path", entrypoint.pathText()));
            });

            var classPath = inboundAdapter.entrypoints().stream()
                    .findFirst()
                    .map(Entrypoint::classPathText)
                    .orElse("");

            controllerList.add(Json.object("fqn", jigType.fqn())
                    .and("classPath", classPath)
                    .and("relations", Json.arrayObjects(edges))
                    .and("entrypoints", Json.arrayObjects(entrypointList)));
        });

        return Json.object("inboundAdapters", Json.arrayObjects(controllerList)).build();
    }
}

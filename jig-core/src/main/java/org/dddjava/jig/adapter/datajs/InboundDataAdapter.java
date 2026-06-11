package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.inbound.Entrypoint;
import org.dddjava.jig.domain.model.information.inbound.InboundAdapters;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.*;
import java.util.stream.Stream;

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

        var ioTypes = collectIoTypes(inboundAdapters, jigTypes);

        return Json.object("inboundAdapters", Json.arrayObjects(controllerList))
                .and("ioTypes", Json.arrayObjects(ioTypes.types()))
                .and("rootIoTypeFqns", Json.array(ioTypes.rootFqns()))
                .build();
    }

    private record IoTypeCollection(List<JsonObjectBuilder> types, List<String> rootFqns) {
    }

    private static IoTypeCollection collectIoTypes(InboundAdapters inboundAdapters, JigTypes jigTypes) {
        var queue = new ArrayDeque<TypeId>();
        var rootTypeIds = new HashSet<TypeId>();
        var visited = new HashMap<TypeId, JigType>();

        inboundAdapters.groups().stream()
                .flatMap(group -> group.entrypoints().stream())
                .map(Entrypoint::jigMethod)
                .flatMap(method -> Stream.concat(method.parameterTypeStream(), Stream.of(method.returnType())))
                .flatMap(JigTypeReference::toTypeIdStream)
                .forEach(id -> {
                    rootTypeIds.add(id);
                    queue.add(id);
                });

        while (!queue.isEmpty()) {
            var typeId = queue.poll();
            if (visited.containsKey(typeId)) continue;
            jigTypes.resolveJigType(typeId).ifPresent(jigType -> {
                visited.put(typeId, jigType);
                jigType.instanceJigFields().fields().forEach(field ->
                        field.jigTypeReference().toTypeIdStream().forEach(queue::add)
                );
            });
        }

        List<String> rootFqns = rootTypeIds.stream()
                .filter(visited::containsKey)
                .map(TypeId::fqn)
                .sorted()
                .toList();

        List<JsonObjectBuilder> types = visited.values().stream()
                .sorted(Comparator.comparing(JigType::fqn))
                .map(jigType -> {
                    var fields = jigType.instanceJigFields().fields().stream()
                            .map(JsonSupport::buildFieldJson)
                            .toList();
                    return Json.object("fqn", jigType.fqn())
                            .and("fields", Json.arrayObjects(fields))
                            .and("isDeprecated", jigType.isDeprecated());
                })
                .toList();

        return new IoTypeCollection(types, rootFqns);
    }
}

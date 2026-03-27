package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelation;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.nio.file.Path;
import java.util.*;

/**
 * 入力インタフェース概要
 */
public class InboundSummaryAdapter {
    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public InboundSummaryAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.EntrypointSummary)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var contextJigTypes = jigService.jigTypes(repository);
        var inputAdapters = jigService.inputAdapters(repository);

        var json = buildJson(inputAdapters, contextJigTypes);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("inboundData", json);

        return jigDocumentWriter.outputFilePaths();
    }

    private String buildJson(InputAdapters inputAdapters, JigTypes jigTypes) {
        List<JsonObjectBuilder> controllerList = new ArrayList<>();

        MethodRelations springComponentMethodRelations = inputAdapters.methodRelations().filterApplicationComponent(jigTypes).inlineLambda();

        inputAdapters.groups().forEach(inputAdapter -> {
            var jigType = inputAdapter.jigType();

            // グラフをコントローラー単位で構築
            List<JsonObjectBuilder> nodes = new ArrayList<>();
            List<JsonObjectBuilder> edges = new ArrayList<>();
            Map<TypeId, List<JsonObjectBuilder>> serviceMethodsByClass = new LinkedHashMap<>();
            Set<String> addedNodes = new HashSet<>();

            List<JsonObjectBuilder> entrypointList = new ArrayList<>();
            inputAdapter.entrypoints().forEach(entrypoint -> {
                var entrypointMethodId = entrypoint.jigMethod().jigMethodId();

                // エントリーポイントのノード（コントローラー共有の addedNodes で重複排除）
                if (addedNodes.add(entrypointMethodId.fqn())) {
                    nodes.add(Json.object("fqn", entrypointMethodId.fqn()).and("type", "entrypoint"));
                }

                // 関連メソッドの探索（コントローラー共有のコレクションに追加）
                var declaredMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethodId,
                        jigTypes::isService
                );

                declaredMethodRelations.list().forEach(relation -> {
                    addEdge(edges, relation);
                    addNode(nodes, relation.from(), jigTypes, addedNodes, serviceMethodsByClass);
                    addNode(nodes, relation.to(), jigTypes, addedNodes, serviceMethodsByClass);
                });

                entrypointList.add(JsonSupport.buildMethodJson(entrypoint.jigMethod())
                        .and("path", entrypoint.pathText()));
            });

            // サービスグループの生成（コントローラー単位）
            List<JsonObjectBuilder> serviceGroups = new ArrayList<>();
            serviceMethodsByClass.forEach((typeId, methods) ->
                    serviceGroups.add(Json.object("fqn", typeId.fqn())
                            .and("methods", Json.arrayObjects(methods))));

            controllerList.add(Json.object("fqn", jigType.fqn())
                    .and("graph", Json.object("nodes", Json.arrayObjects(nodes))
                            .and("edges", Json.arrayObjects(edges))
                            .and("serviceGroups", Json.arrayObjects(serviceGroups)))
                    .and("entrypoints", Json.arrayObjects(entrypointList)));
        });

        return Json.object("controllers", Json.arrayObjects(controllerList)).build();
    }

    private void addNode(List<JsonObjectBuilder> nodes, JigMethodId methodId, JigTypes jigTypes, Set<String> addedNodes, Map<TypeId, List<JsonObjectBuilder>> serviceMethodsByClass) {
        String fqn = methodId.fqn();
        if (!addedNodes.add(fqn)) return;

        if (jigTypes.isService(methodId)) {
            jigTypes.resolveJigMethod(methodId).ifPresent(method -> {
                var typeId = method.declaringType();
                serviceMethodsByClass.computeIfAbsent(typeId, k -> new ArrayList<>())
                        .add(Json.object("fqn", fqn));
            });
        } else {
            nodes.add(Json.object("fqn", fqn).and("type", "method"));
        }
    }

    private void addEdge(List<JsonObjectBuilder> edges, MethodRelation relation) {
        edges.add(Json.object("from", relation.from().fqn())
                .and("to", relation.to().fqn()));
    }
}

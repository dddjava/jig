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
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

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
            Set<String> addedNodes = new HashSet<>();
            Set<JigMethodId> usecaseMethods = new HashSet<>();

            List<JsonObjectBuilder> entrypointList = new ArrayList<>();
            inputAdapter.entrypoints().forEach(entrypoint -> {
                var entrypointMethodId = entrypoint.jigMethod().jigMethodId();

                // エントリーポイントのノード（コントローラー共有の addedNodes で重複排除）
                if (addedNodes.add(entrypointMethodId.fqn())) {
                    nodes.add(Json.object("fqn", entrypointMethodId.fqn()).and("type", "entrypoint"));
                }

                // 関連メソッドの探索（コントローラー共有のコレクションに追加）
                MethodRelations declaredMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethodId, jigTypes::isService);

                declaredMethodRelations.list().forEach(relation -> {
                    edges.add(Json.object("from", relation.from().fqn())
                            .and("to", relation.to().fqn()));

                    addNode(nodes, relation.from(), jigTypes, addedNodes, usecaseMethods);
                    addNode(nodes, relation.to(), jigTypes, addedNodes, usecaseMethods);
                });

                entrypointList.add(JsonSupport.buildMethodJson(entrypoint.jigMethod())
                        .and("path", entrypoint.pathText()));
            });

            Map<TypeId, List<JigMethod>> usecaseMap = usecaseMethods.stream()
                    .flatMap(jigMethodId -> jigTypes.resolveJigMethod(jigMethodId).stream())
                    .collect(Collectors.groupingBy(JigMethod::declaringType));

            List<JsonObjectBuilder> usecases = usecaseMap.entrySet().stream().map(entry ->
                            Json.object("fqn", entry.getKey().fqn())
                                    .and("methods", Json.arrayObjects(entry.getValue().stream()
                                            .map(JsonSupport::buildMethodJson).toList())))
                    .toList();

            controllerList.add(Json.object("fqn", jigType.fqn())
                    .and("graph", Json.object("nodes", Json.arrayObjects(nodes))
                            .and("edges", Json.arrayObjects(edges))
                            .and("usecases", Json.arrayObjects(usecases)))
                    .and("entrypoints", Json.arrayObjects(entrypointList)));
        });

        return Json.object("controllers", Json.arrayObjects(controllerList)).build();
    }

    private void addNode(List<JsonObjectBuilder> nodes, JigMethodId methodId, JigTypes jigTypes, Set<String> addedNodes, Set<JigMethodId> usecaseMethods) {
        String fqn = methodId.fqn();
        if (!addedNodes.add(fqn)) return;

        if (jigTypes.isService(methodId)) {
            usecaseMethods.add(methodId);
        } else {
            nodes.add(Json.object("fqn", fqn).and("type", "method"));
        }
    }
}

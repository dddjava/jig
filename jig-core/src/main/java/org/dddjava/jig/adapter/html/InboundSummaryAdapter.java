package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.html.view.HtmlSupport;
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
 *
 * FIXME Mermaidのダイアグラムに対応した出力をほとんどここで記述してしまっている。
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
            List<JsonObjectBuilder> entrypointList = new ArrayList<>();

            inputAdapter.entrypoints().forEach(entrypoint -> {
                var entrypointMethodId = entrypoint.jigMethod().jigMethodId();
                var entrypointMmdId = HtmlSupport.htmlMethodIdText(entrypointMethodId);

                List<JsonObjectBuilder> nodes = new ArrayList<>();
                List<JsonObjectBuilder> edges = new ArrayList<>();

                // エントリーポイントとパスのノード
                nodes.add(Json.object("id", entrypointMmdId).and("label", entrypoint.methodLabelText()).and("type", "entrypoint"));
                nodes.add(Json.object("id", "__" + entrypointMmdId).and("label", entrypoint.pathText()).and("type", "path"));
                edges.add(Json.object("from", "__" + entrypointMmdId).and("to", entrypointMmdId).and("style", "dotted"));

                // 関連メソッドの探索
                var decraleMethodRelations = springComponentMethodRelations.filterFromRecursive(entrypointMethodId,
                        jigTypes::isService
                );

                Map<TypeId, List<JsonObjectBuilder>> serviceMethodsByClass = new LinkedHashMap<>();
                Set<String> addedNodes = new HashSet<>();
                addedNodes.add(entrypointMmdId);
                addedNodes.add("__" + entrypointMmdId);

                decraleMethodRelations.list().forEach(relation -> {
                    addEdge(edges, relation);
                    addNode(nodes, relation.from(), jigTypes, entrypoint.typeId(), addedNodes, serviceMethodsByClass);
                    addNode(nodes, relation.to(), jigTypes, entrypoint.typeId(), addedNodes, serviceMethodsByClass);
                });

                // サービスメソッドをクラスごとにまとめる
                List<JsonObjectBuilder> serviceGroups = new ArrayList<>();
                serviceMethodsByClass.forEach((typeId, methods) -> {
                    serviceGroups.add(Json.object("typeId", typeId.fqn())
                            .and("label", typeId.asSimpleText())
                            .and("methods", Json.arrayObjects(methods)));
                });

                entrypointList.add(Json.object("methodId", entrypointMmdId)
                        .and("label", entrypoint.jigMethod().name())
                        .and("path", entrypoint.pathText())
                        .and("graph", Json.object("nodes", Json.arrayObjects(nodes))
                                .and("edges", Json.arrayObjects(edges))
                                .and("serviceGroups", Json.arrayObjects(serviceGroups))));
            });

            controllerList.add(Json.object("fqn", jigType.fqn())
                    .and("label", jigType.label())
                    .and("description", jigType.term().description())
                    .and("entrypoints", Json.arrayObjects(entrypointList)));
        });

        return Json.object("controllers", Json.arrayObjects(controllerList)).build();
    }

    private void addNode(List<JsonObjectBuilder> nodes, JigMethodId methodId, JigTypes jigTypes, TypeId controllerTypeId, Set<String> addedNodes, Map<TypeId, List<JsonObjectBuilder>> serviceMethodsByClass) {
        String mmdId = HtmlSupport.htmlMethodIdText(methodId);
        if (!addedNodes.add(mmdId)) return;

        if (jigTypes.isService(methodId)) {
            jigTypes.resolveJigMethod(methodId).ifPresent(method -> {
                var typeId = method.declaringType();
                serviceMethodsByClass.computeIfAbsent(typeId, k -> new ArrayList<>())
                        .add(Json.object("id", mmdId)
                                .and("label", method.labelText())
                                .and("link", HtmlSupport.htmlMethodIdText(methodId)));
            });
        } else {
            String label;
            var declaringTypeId = methodId.tuple().declaringTypeId();
            if (controllerTypeId.equals(declaringTypeId)) {
                label = methodId.name();
            } else {
                label = declaringTypeId.asSimpleName() + "." + methodId.name();
            }
            nodes.add(Json.object("id", mmdId).and("label", label).and("type", "method"));
        }
    }

    private void addEdge(List<JsonObjectBuilder> edges, MethodRelation relation) {
        edges.add(Json.object("from", HtmlSupport.htmlMethodIdText(relation.from()))
                .and("to", HtmlSupport.htmlMethodIdText(relation.to())));
    }
}

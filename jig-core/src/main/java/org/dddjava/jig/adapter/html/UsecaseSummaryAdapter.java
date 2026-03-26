package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.relation.methods.MethodRelations;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ユースケース概要
 *
 * FIXME Mermaidのダイアグラムに対応した出力をほとんどここで記述してしまっている。
 */
public class UsecaseSummaryAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public UsecaseSummaryAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.UsecaseSummary)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var contextJigTypes = jigService.serviceTypes(repository);
        var methodRelations = MethodRelations.lambdaInlined(contextJigTypes);

        var json = buildJson(contextJigTypes, methodRelations);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("usecaseData", json);

        return jigDocumentWriter.outputFilePaths();
    }

    private String buildJson(JigTypes contextJigTypes, MethodRelations methodRelations) {
        List<JsonObjectBuilder> usecaseList = new ArrayList<>();

        for (var jigType : contextJigTypes.list()) {
            List<JsonObjectBuilder> fields = jigType.instanceJigFields().fields().stream()
                    .map(JsonSupport::buildFieldJson)
                    .collect(Collectors.toList());

            List<JsonObjectBuilder> staticMethods = jigType.staticJigMethods()
                    .filterProgrammerDefined()
                    .excludeNotNoteworthyObjectMethod()
                    .listRemarkable()
                    .stream()
                    .map(jigMethod -> buildMethodJson(jigMethod, contextJigTypes, methodRelations))
                    .collect(Collectors.toList());

            List<JsonObjectBuilder> methodList = new ArrayList<>();
            for (var jigMethod : jigType.instanceJigMethods().filterProgrammerDefined().excludeNotNoteworthyObjectMethod().listRemarkable()) {
                methodList.add(buildMethodJson(jigMethod, contextJigTypes, methodRelations));
            }

            if (!methodList.isEmpty() || !fields.isEmpty() || !staticMethods.isEmpty()) {
                usecaseList.add(Json.object("fqn", jigType.fqn())
                        .and("fields", Json.arrayObjects(fields))
                        .and("staticMethods", Json.arrayObjects(staticMethods))
                        .and("methods", Json.arrayObjects(methodList)));
            }
        }

        return Json.object("usecases", Json.arrayObjects(usecaseList)).build();
    }

    private JsonObjectBuilder buildMethodJson(JigMethod jigMethod, JigTypes contextJigTypes, MethodRelations methodRelations) {
        return JsonSupport.buildMethodJson(jigMethod)
                .and("callMethods", Json.array(jigMethod.lambdaInlinedMethodCallStream()
                        .map(MethodCall::fqn)
                        .toList()))
                // 以下をなくしたらこのメソッドがいらなくなる
                .and("declaration", jigMethod.simpleMethodDeclarationText())
                .and("graph", buildGraphJson(jigMethod, contextJigTypes, methodRelations));
    }

    private JsonObjectBuilder buildGraphJson(JigMethod jigMethod, JigTypes contextJigTypes, MethodRelations methodRelations) {
        List<JsonObjectBuilder> nodes = new ArrayList<>();
        List<JsonObjectBuilder> edges = new ArrayList<>();

        // 基点からの呼び出し全部 + 直近の呼び出し元
        var filteredRelations = methodRelations.filterFromRecursive(jigMethod.jigMethodId())
                .merge(methodRelations.filterTo(jigMethod.jigMethodId()));

        Set<JigMethodId> resolved = new HashSet<>();

        // メソッドのノード
        filteredRelations.toJigMethodIdStream().forEach(jigMethodId -> {
            // 自分
            if (jigMethodId.equals(jigMethod.jigMethodId())) {
                resolved.add(jigMethodId);
                nodes.add(Json.object()
                        .and("fqn", jigMethodId.fqn())
                        .and("type", "usecase"));
            } else {
                contextJigTypes.resolveJigMethod(jigMethodId)
                        .ifPresent(method -> {
                            resolved.add(jigMethodId);
                            if (method.remarkable()) {
                                // 出力対象のメソッドはusecase型＆クリックできるように
                                nodes.add(Json.object()
                                        .and("fqn", method.fqn())
                                        .and("type", "usecase"));
                            } else {
                                // remarkableでないものは普通の。privateメソッドなど該当。
                                nodes.add(Json.object()
                                        .and("fqn", method.fqn())
                                        .and("type", jigMethodId.isLambda() ? "lambda" : "normal"));
                            }
                        });
            }
        });

        Set<TypeId> others = new HashSet<>();

        // エッジ
        filteredRelations.list().forEach(relation -> {
            String fromId = resolveNodeId(relation.from(), resolved, others, jigMethod);
            String toId = resolveNodeId(relation.to(), resolved, others, jigMethod);

            if (fromId != null && toId != null) {
                edges.add(Json.object("from", fromId).and("to", toId));
            }
        });

        // JigMethodにならないものはクラスノードとして出力する
        others.forEach(typeId ->
                nodes.add(Json.object()
                        .and("fqn", typeId.fqn())
                        .and("type", "other")));

        return Json.object("nodes", Json.arrayObjects(nodes))
                .and("edges", Json.arrayObjects(edges));
    }

    private String resolveNodeId(JigMethodId jigMethodId, Set<JigMethodId> resolved, Set<TypeId> others, JigMethod contextMethod) {
        if (resolved.contains(jigMethodId)) {
            return jigMethodId.fqn();
        }
        // 解決できなかったものは関心が薄いとして、メソッドではなくクラスとして解釈し
        var typeId = jigMethodId.tuple().declaringTypeId();
        if (typeId.packageId().equals(contextMethod.declaringType().packageId())) {
            // 暫定的に同じパッケージのもののみ出力する
            others.add(typeId);
            return typeId.fqn();
        }
        // パッケージ外は出力しない
        return null;
    }
}

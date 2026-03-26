package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
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

        var json = buildJson(contextJigTypes);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("usecaseData", json);

        return jigDocumentWriter.outputFilePaths();
    }

    private String buildJson(JigTypes contextJigTypes) {
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
                    .map(jigMethod -> buildMethodJson(jigMethod))
                    .collect(Collectors.toList());

            List<JsonObjectBuilder> methodList = new ArrayList<>();
            for (var jigMethod : jigType.instanceJigMethods().filterProgrammerDefined().excludeNotNoteworthyObjectMethod().listRemarkable()) {
                methodList.add(buildMethodJson(jigMethod));
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

    private JsonObjectBuilder buildMethodJson(JigMethod jigMethod) {
        return JsonSupport.buildMethodJson(jigMethod)
                .and("callMethods", Json.array(jigMethod.lambdaInlinedMethodCallStream()
                        .map(MethodCall::fqn)
                        .toList()))
                // 以下をなくしたらこのメソッドがいらなくなる
                .and("declaration", jigMethod.simpleMethodDeclarationText());
    }

}

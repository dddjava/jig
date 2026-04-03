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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ユースケース
 */
@HandleDocument
public class UsecaseModelAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public UsecaseModelAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.UsecaseModel)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var contextJigTypes = jigService.serviceTypes(repository);

        var json = buildJson(contextJigTypes);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtml();
        jigDocumentWriter.writeData("usecaseData", json);

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(JigTypes contextJigTypes) {
        var usecaseList = contextJigTypes.stream()
                .flatMap(jigType -> {
                    List<JsonObjectBuilder> fields = jigType.instanceJigFields().fields().stream()
                            .map(JsonSupport::buildFieldJson)
                            .collect(Collectors.toList());

                    List<JsonObjectBuilder> staticMethods = jigType.staticJigMethods().stream()
                            .filter(jigMethod -> jigMethod.isProgrammerDefined())
                            .map(UsecaseModelAdapter::buildMethodJson)
                            .collect(Collectors.toList());

                    List<JsonObjectBuilder> methodList = jigType.instanceJigMethods().stream()
                            .filter(jigMethod -> jigMethod.isProgrammerDefined())
                            .map(UsecaseModelAdapter::buildMethodJson)
                            .toList();

                    if (methodList.isEmpty() && fields.isEmpty() && staticMethods.isEmpty()) {
                        return Stream.empty();
                    }

                    return Stream.of(
                            Json.object("fqn", jigType.fqn())
                                    .and("fields", Json.arrayObjects(fields))
                                    .and("staticMethods", Json.arrayObjects(staticMethods))
                                    .and("methods", Json.arrayObjects(methodList)));
                })
                .toList();

        return Json.object("usecases", Json.arrayObjects(usecaseList)).build();
    }

    private static JsonObjectBuilder buildMethodJson(JigMethod jigMethod) {
        return JsonSupport.buildMethodJson(jigMethod)
                .and("callMethods", Json.array(jigMethod.lambdaInlinedMethodCallStream()
                        .filter(methodCall -> methodCall.isXxx())
                        .map(MethodCall::fqn)
                        .toList()));
    }
}

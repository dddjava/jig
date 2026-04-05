package org.dddjava.jig.adapter.documents;

import org.dddjava.jig.adapter.JigDocumentAdapter;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
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
public class UsecaseModelAdapter implements JigDocumentAdapter {

    private final JigService jigService;
    private final Path outputDirectory;

    public UsecaseModelAdapter(JigService jigService, Path outputDirectory) {
        this.jigService = jigService;
        this.outputDirectory = outputDirectory;
    }

    @Override
    public JigDocument supportedDocument() {
        return JigDocument.UsecaseModel;
    }

    @Override
    public List<Path> write(JigDocument jigDocument, JigRepository jigRepository) {
        var contextJigTypes = jigService.serviceTypes(jigRepository);
        return List.of(JigDocumentWriter.writeData(outputDirectory, jigDocument, "usecaseData", buildJson(contextJigTypes)));
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

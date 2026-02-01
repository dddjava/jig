package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.fields.JigFieldId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.inputs.Entrypoint;
import org.dddjava.jig.domain.model.information.inputs.InputAdapters;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

@HandleDocument
public class ListOutputAdapter {

    private static final Collector<CharSequence, ?, String> STREAM_COLLECTOR = joining(", ", "[", "]");

    private final JigService jigService;
    private final TemplateEngine templateEngine;
    private final JigDocumentContext jigDocumentContext;

    public ListOutputAdapter(JigService jigService, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.ListOutput)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        InputAdapters inputAdapters = jigService.inputAdapters(repository);
        String controllerJson = inputAdapters.listEntrypoint().stream()
                .map(this::formatControllerJson)
                .collect(Collectors.joining(",", "[", "]"));

        String listJson = """
                {"controllers": %s}
                """.formatted(controllerJson);

        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());
        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "listJson", listJson
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }

    private String formatControllerJson(Entrypoint entrypoint) {
        String usingFieldTypesJson = entrypoint.jigMethod().usingFields().jigFieldIds().stream()
                .map(JigFieldId::declaringTypeId)
                .map(TypeId::asSimpleText)
                .sorted()
                .map(this::escape)
                .map(value -> "\"" + value + "\"")
                .collect(Collectors.joining(",", "[", "]"));
        return """
                {"packageName": "%s", "typeName": "%s", "methodSignature": "%s", "returnType": "%s", "typeLabel": "%s", "usingFieldTypes": %s, "cyclomaticComplexity": %d, "path": "%s"}
                """.formatted(
                escape(entrypoint.packageId().asText()),
                escape(entrypoint.typeId().asSimpleText()),
                escape(entrypoint.jigMethod().simpleMethodSignatureText()),
                escape(entrypoint.jigMethod().returnType().simpleName()),
                escape(entrypoint.jigType().label()),
                usingFieldTypesJson,
                entrypoint.jigMethod().instructions().cyclomaticComplexity(),
                escape(entrypoint.fullPathText()));
    }

    private String escape(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}

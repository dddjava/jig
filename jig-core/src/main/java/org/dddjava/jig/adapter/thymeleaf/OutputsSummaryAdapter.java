package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outputs.*;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 外部利用概要
 */
public class OutputsSummaryAdapter {
    private final JigService jigService;
    private final TemplateEngine templateEngine;
    private final JigDocumentContext jigDocumentContext;

    public OutputsSummaryAdapter(JigService jigService, TemplateEngine templateEngine, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.templateEngine = templateEngine;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.OutputsSummary)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var jigTypes = jigService.jigTypes(repository);

        var sqlStatements = repository.jigDataProvider().persistenceOperationsRepository();
        var outputAdapters = OutputAdapters.from(jigTypes, sqlStatements);

        String outputsJson = outputAdapters.stream()
                // output adapterが実装しているoutput portのoperationを
                .flatMap(outputAdapter -> outputAdapter.implementsPortStream(jigTypes)
                        .flatMap(outputPort -> outputPort.operationStream()
                                // 実装しているexecitonが
                                .flatMap(outputPortOperation -> outputAdapter.findExecution(outputPortOperation).stream()
                                        .map(outputAdapterExecution -> formatLinkJson(
                                                outputAdapter,
                                                outputPort,
                                                outputPortOperation,
                                                outputAdapterExecution)))))
                .collect(Collectors.joining(",", "[", "]"));

        String json = """
                {"links":%s}
                """.formatted(outputsJson);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label(),
                "outputsJson", json
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String template = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(template, context, writer));
        return jigDocumentWriter.outputFilePaths();
    }

    private String formatLinkJson(OutputAdapter outputAdapter,
                                  OutputPort outputPort,
                                  OutputPortOperation outputPortOperation,
                                  OutputAdapterExecution outputAdapterExecution) {
        String persistenceOperationsJson = outputAdapterExecution.persistenceOperations().stream()
                .map(persistenceOperation -> """
                        {"id":"%s","sqlType":"%s","targets":%s}
                        """.formatted(
                        escape(persistenceOperation.persistenceOperationId().value()),
                        persistenceOperation.sqlType().name(),
                        toJsonStringList(persistenceOperation.persistenceTargets().persistenceTargets().stream()
                                .map(persistenceTarget -> persistenceTarget.name())
                                .toList())))
                .collect(Collectors.joining(",", "[", "]"));

        return """
                {"outputPort":{"fqn":"%s","label":"%s"},
                "outputPortOperation":{"fqn":"%s","name":"%s","signature":"%s"},
                "outputAdapter":{"fqn":"%s","label":"%s"},
                "outputAdapterExecution":{"fqn":"%s","name":"%s","signature":"%s"},
                "persistenceOperations":%s}
                """.formatted(
                escape(outputPort.jigType().fqn()),
                escape(outputPort.jigType().label()),
                escape(outputPortOperation.jigMethod().fqn()),
                escape(outputPortOperation.jigMethod().name()),
                escape(outputPortOperation.jigMethod().simpleMethodSignatureText()),
                escape(outputAdapter.jigType().fqn()),
                escape(outputAdapter.jigType().label()),
                escape(outputAdapterExecution.jigMethod().fqn()),
                escape(outputAdapterExecution.jigMethod().name()),
                escape(outputAdapterExecution.jigMethod().simpleMethodSignatureText()),
                persistenceOperationsJson);
    }

    private String toJsonStringList(List<String> values) {
        return values.stream()
                .map(this::escape)
                .map(value -> "\"" + value + "\"")
                .collect(Collectors.joining(",", "[", "]"));
    }

    private String escape(String string) {
        return string
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}

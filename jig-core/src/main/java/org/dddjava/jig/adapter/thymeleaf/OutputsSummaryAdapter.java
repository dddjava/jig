package org.dddjava.jig.adapter.thymeleaf;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.persistence.PersistenceTarget;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outputs.OutputAdapters;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.nio.file.Path;
import java.util.*;
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

        Map<String, String> ports = new LinkedHashMap<>();
        Map<String, String> operations = new LinkedHashMap<>();
        Map<String, String> adapters = new LinkedHashMap<>();
        Map<String, String> executions = new LinkedHashMap<>();
        Map<String, String> persistenceOperations = new LinkedHashMap<>();

        List<String> links = new ArrayList<>();

        outputAdapters.stream().forEach(outputAdapter -> {
            String adapterFqn = outputAdapter.jigType().fqn();
            adapters.putIfAbsent(adapterFqn, """
                    {"fqn":"%s","label":"%s"}
                    """.formatted(escape(adapterFqn), escape(outputAdapter.jigType().label())));

            outputAdapter.implementsPortStream(jigTypes).forEach(outputPort -> {
                String portFqn = outputPort.jigType().fqn();
                ports.putIfAbsent(portFqn, """
                        {"fqn":"%s","label":"%s"}
                        """.formatted(escape(portFqn), escape(outputPort.jigType().label())));

                outputPort.operationStream().forEach(outputPortOperation -> {
                    String opFqn = outputPortOperation.jigMethod().fqn();
                    operations.putIfAbsent(opFqn, """
                            {"fqn":"%s","name":"%s","signature":"%s"}
                            """.formatted(
                            escape(opFqn),
                            escape(outputPortOperation.jigMethod().name()),
                            escape(outputPortOperation.jigMethod().simpleMethodSignatureText())));

                    outputAdapter.findExecution(outputPortOperation).ifPresent(outputAdapterExecution -> {
                        String execFqn = outputAdapterExecution.jigMethod().fqn();
                        executions.putIfAbsent(execFqn, """
                                {"fqn":"%s","name":"%s","signature":"%s"}
                                """.formatted(
                                escape(execFqn),
                                escape(outputAdapterExecution.jigMethod().name()),
                                escape(outputAdapterExecution.jigMethod().simpleMethodSignatureText())));

                        List<String> pOpIds = new ArrayList<>();
                        outputAdapterExecution.persistenceOperations().forEach(pOp -> {
                            String pOpId = pOp.persistenceOperationId().value();
                            pOpIds.add(pOpId);
                            persistenceOperations.putIfAbsent(pOpId, """
                                    {"id":"%s","sqlType":"%s","targets":%s}
                                    """.formatted(
                                    escape(pOpId),
                                    pOp.sqlType().name(),
                                    toJsonStringList(pOp.persistenceTargets().persistenceTargets().stream()
                                            .map(PersistenceTarget::name)
                                            .toList())));
                        });

                        links.add("""
                                {"port":"%s","operation":"%s","adapter":"%s","execution":"%s","persistenceOperations":%s}
                                """.formatted(
                                escape(portFqn),
                                escape(opFqn),
                                escape(adapterFqn),
                                escape(execFqn),
                                toJsonStringList(pOpIds)));
                    });
                });
            });
        });

        String json = """
                {
                  "ports": %s,
                  "operations": %s,
                  "adapters": %s,
                  "executions": %s,
                  "persistenceOperations": %s,
                  "links": [%s]
                }
                """.formatted(
                mapToJson(ports),
                mapToJson(operations),
                mapToJson(adapters),
                mapToJson(executions),
                mapToJson(persistenceOperations),
                String.join(",", links)
        );

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

    private String mapToJson(Map<String, String> map) {
        return map.entrySet().stream()
                .map(e -> "\"%s\":%s".formatted(escape(e.getKey()), e.getValue()))
                .collect(Collectors.joining(",", "{", "}"));
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

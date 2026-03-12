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
            adapters.putIfAbsent(adapterFqn, Json.object("fqn", adapterFqn)
                    .and("label", outputAdapter.jigType().label())
                    .build());

            outputAdapter.implementsPortStream(jigTypes).forEach(outputPort -> {
                String portFqn = outputPort.jigType().fqn();
                ports.putIfAbsent(portFqn, Json.object("fqn", portFqn)
                        .and("label", outputPort.jigType().label())
                        .build());

                outputPort.operationStream().forEach(outputPortOperation -> {
                    String opFqn = outputPortOperation.jigMethod().fqn();
                    operations.putIfAbsent(opFqn, Json.object("fqn", opFqn)
                            .and("name", outputPortOperation.jigMethod().name())
                            .and("signature", outputPortOperation.jigMethod().simpleMethodSignatureText())
                            .build());

                    outputAdapter.findExecution(outputPortOperation).ifPresent(outputAdapterExecution -> {
                        String execFqn = outputAdapterExecution.jigMethod().fqn();
                        executions.putIfAbsent(execFqn, Json.object("fqn", execFqn)
                                .and("name", outputAdapterExecution.jigMethod().name())
                                .and("signature", outputAdapterExecution.jigMethod().simpleMethodSignatureText())
                                .build());

                        List<String> pOpIds = new ArrayList<>();
                        outputAdapterExecution.persistenceOperations().forEach(pOp -> {
                            String pOpId = pOp.persistenceOperationId().value();
                            pOpIds.add(pOpId);
                            persistenceOperations.putIfAbsent(pOpId, Json.object("id", pOpId)
                                    .and("sqlType", pOp.sqlType().name())
                                    .and("targets", Json.array(pOp.persistenceTargets().persistenceTargets().stream()
                                            .map(PersistenceTarget::name)
                                            .toList()))
                                    .and("group", pOp.persistenceOperationId().typeId().fqn())
                                    .build());
                        });

                        links.add(Json.object("port", portFqn)
                                .and("operation", opFqn)
                                .and("adapter", adapterFqn)
                                .and("execution", execFqn)
                                .and("persistenceOperations", Json.array(pOpIds))
                                .build());
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
                JsonSupport.mapToJson(ports),
                JsonSupport.mapToJson(operations),
                JsonSupport.mapToJson(adapters),
                JsonSupport.mapToJson(executions),
                JsonSupport.mapToJson(persistenceOperations),
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

}

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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

        var sqlStatements = repository.jigDataProvider().persistenceAccessorsRepository();
        var outputAdapters = OutputAdapters.from(jigTypes, sqlStatements);

        var ports = Json.object();
        var operations = Json.object();
        var adapters = Json.object();
        var executions = Json.object();
        var persistenceOperations = Json.object();

        List<JsonObjectBuilder> links = new ArrayList<>();

        outputAdapters.stream().forEach(outputAdapter -> {
            String adapterFqn = outputAdapter.jigType().fqn();
            adapters.and(adapterFqn, Json.object("fqn", adapterFqn)
                    .and("label", outputAdapter.jigType().label()));

            outputAdapter.implementsPortStream(jigTypes).forEach(outputPort -> {
                String portFqn = outputPort.jigType().fqn();
                ports.and(portFqn, Json.object("fqn", portFqn)
                        .and("label", outputPort.jigType().label()));

                outputPort.operationStream().forEach(outputPortOperation -> {
                    String opFqn = outputPortOperation.jigMethod().fqn();
                    operations.and(opFqn, Json.object("fqn", opFqn)
                            .and("name", outputPortOperation.jigMethod().name())
                            .and("signature", outputPortOperation.jigMethod().simpleMethodSignatureText()));

                    outputAdapter.findExecution(outputPortOperation).ifPresent(outputAdapterExecution -> {
                        String execFqn = outputAdapterExecution.jigMethod().fqn();
                        executions.and(execFqn, Json.object("fqn", execFqn)
                                .and("name", outputAdapterExecution.jigMethod().name())
                                .and("signature", outputAdapterExecution.jigMethod().simpleMethodSignatureText()));

                        List<String> pOpIds = new ArrayList<>();
                        outputAdapterExecution.persistenceAccessors().forEach(pOp -> {
                            String pOpId = pOp.persistenceAccessorId().value();
                            pOpIds.add(pOpId);
                            persistenceOperations.and(pOpId, Json.object("id", pOpId)
                                    .and("sqlType", pOp.sqlType().name())
                                    .and("targets", Json.array(pOp.persistenceTargets().persistenceTargets().stream()
                                            .map(PersistenceTarget::name)
                                            .toList()))
                                    .and("group", pOp.persistenceAccessorId().typeId().fqn()));
                        });

                        links.add(Json.object("port", portFqn)
                                .and("operation", opFqn)
                                .and("adapter", adapterFqn)
                                .and("execution", execFqn)
                                .and("persistenceOperations", Json.array(pOpIds)));
                    });
                });
            });
        });

        String json = Json.object("ports", ports)
                .and("operations", operations)
                .and("adapters", adapters)
                .and("executions", executions)
                .and("persistenceOperations", persistenceOperations)
                .and("links", Json.arrayObjects(links))
                .build();

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

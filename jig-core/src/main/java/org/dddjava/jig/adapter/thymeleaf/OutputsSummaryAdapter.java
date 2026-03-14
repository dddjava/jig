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

import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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

        var persistenceAccessorsRepository = repository.jigDataProvider().persistenceAccessorsRepository();
        var outputAdapters = OutputAdapters.from(jigTypes, persistenceAccessorsRepository);

        var json = buildJson(outputAdapters);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        Map<String, Object> contextMap = Map.of(
                "title", jigDocumentWriter.jigDocument().label()
        );

        Context context = new Context(Locale.ROOT, contextMap);
        String fileName = jigDocumentWriter.jigDocument().fileName();

        jigDocumentWriter.writeTextAs(".html",
                writer -> templateEngine.process(fileName, context, writer));

        // JSONの書き出し
        jigDocumentWriter.write(
                outputStream -> {
                    try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                        writer.write("globalThis.outputPortData = " + json);
                    }
                },
                "data/" + fileName + "-data.js"
        );
        return jigDocumentWriter.outputFilePaths();
    }

    static String buildJson(OutputAdapters outputAdapters) {
        var portsMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var adaptersMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var accessorTypesMap = new LinkedHashMap<String, String>();              // typeFqn → typeLabel
        var accessorMethodsMap = new LinkedHashMap<String, List<JsonObjectBuilder>>(); // typeFqn → methods
        var accessorMethodIds = new LinkedHashSet<String>();                     // method ID重複排除
        var targetsSet = new LinkedHashSet<String>();

        List<JsonObjectBuilder> operationToExecution = new ArrayList<>();
        List<JsonObjectBuilder> executionToAccessor = new ArrayList<>();

        outputAdapters.stream().forEach(outputAdapter -> {
            String adapterFqn = outputAdapter.jigType().fqn();
            List<JsonObjectBuilder> execList = new ArrayList<>();

            outputAdapter.implementsPortStream().forEach(outputPort -> {
                String portFqn = outputPort.jigType().fqn();
                List<JsonObjectBuilder> opList = new ArrayList<>();

                outputPort.operationStream().forEach(op -> {
                    String opFqn = op.jigMethod().fqn();
                    opList.add(Json.object("fqn", opFqn)
                            .and("label", op.jigMethod().name())
                            .and("signature", op.jigMethod().simpleMethodSignatureText()));

                    outputAdapter.findExecution(op).ifPresent(exec -> {
                        String execFqn = exec.jigMethod().fqn();
                        execList.add(Json.object("fqn", execFqn)
                                .and("label", exec.jigMethod().name())
                                .and("signature", exec.jigMethod().simpleMethodSignatureText()));

                        operationToExecution.add(Json.object("operation", opFqn)
                                .and("execution", execFqn));

                        exec.persistenceAccessors().forEach(pOp -> {
                            String methodId = pOp.persistenceAccessorId().value();
                            String typeFqn = pOp.persistenceAccessorId().typeId().fqn();
                            String typeLabel = typeFqn.contains(".")
                                    ? typeFqn.substring(typeFqn.lastIndexOf('.') + 1) : typeFqn;
                            List<String> targets = pOp.persistenceTargets().persistenceTargets().stream()
                                    .map(PersistenceTarget::name).toList();

                            targetsSet.addAll(targets);
                            accessorTypesMap.putIfAbsent(typeFqn, typeLabel);
                            if (accessorMethodIds.add(methodId)) {
                                accessorMethodsMap.computeIfAbsent(typeFqn, k -> new ArrayList<>())
                                        .add(Json.object("id", methodId)
                                                .and("sqlType", pOp.sqlType().name())
                                                .and("targets", Json.array(targets)));
                            }

                            executionToAccessor.add(Json.object("execution", execFqn)
                                    .and("accessor", methodId));
                        });
                    });
                });

                portsMap.putIfAbsent(portFqn, Json.object("fqn", portFqn)
                        .and("label", outputPort.jigType().label())
                        .and("operations", Json.arrayObjects(opList)));
            });

            adaptersMap.putIfAbsent(adapterFqn, Json.object("fqn", adapterFqn)
                    .and("label", outputAdapter.jigType().label())
                    .and("executions", Json.arrayObjects(execList)));
        });

        var links = Json.object("operationToExecution", Json.arrayObjects(operationToExecution))
                .and("executionToAccessor", Json.arrayObjects(executionToAccessor));

        List<JsonObjectBuilder> accessorsList = new ArrayList<>();
        accessorTypesMap.forEach((typeFqn, typeLabel) -> {
            List<JsonObjectBuilder> methods = accessorMethodsMap.getOrDefault(typeFqn, List.of());
            accessorsList.add(Json.object("fqn", typeFqn)
                    .and("label", typeLabel)
                    .and("methods", Json.arrayObjects(methods)));
        });

        return Json.object("outputPorts", Json.arrayObjects(new ArrayList<>(portsMap.values())))
                .and("outputAdapters", Json.arrayObjects(new ArrayList<>(adaptersMap.values())))
                .and("persistenceAccessors", Json.arrayObjects(accessorsList))
                .and("targets", Json.array(new ArrayList<>(targetsSet)))
                .and("links", links)
                .build();
    }

}

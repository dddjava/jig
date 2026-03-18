package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.persistence.PersistenceTarget;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outputs.OutputAdapters;

import java.nio.file.Path;
import java.util.*;

/**
 * 外部利用概要
 */
public class OutputsSummaryAdapter {
    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public OutputsSummaryAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.OutputsSummary)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var jigTypes = jigService.jigTypes(repository);

        var accessorRepositories = repository.externalAccessorRepositories();
        var outputAdapters = OutputAdapters.from(jigTypes, accessorRepositories);

        var json = buildJson(outputAdapters);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("outputPortData", json);

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(OutputAdapters outputAdapters) {
        var portsMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var adaptersMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var accessorTypesMap = new LinkedHashMap<String, String>();              // typeFqn → typeLabel
        var accessorMethodsMap = new LinkedHashMap<String, List<JsonObjectBuilder>>(); // typeFqn → methods
        var accessorMethodIds = new LinkedHashSet<String>();                     // method ID重複排除
        var targetsSet = new LinkedHashSet<String>();

        List<JsonObjectBuilder> operationToExecution = new ArrayList<>();
        List<JsonObjectBuilder> executionToAccessor = new ArrayList<>();
        List<JsonObjectBuilder> executionToExternalAccessor = new ArrayList<>();

        var externalAccessorLabels = new LinkedHashMap<String, String>();                                                         // fqn → label
        var externalAccessorMethods = new LinkedHashMap<String, LinkedHashMap<String, List<JsonObjectBuilder>>>();               // accessorFqn → accessorMethodName → externals
        var externalAccessorMethodKeys = new LinkedHashSet<String>();                                                            // dedup key
        var executionToExternalAccessorKeys = new LinkedHashSet<String>();                                                      // dedup key

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

                        exec.persistenceAccessorOperations().forEach(pOp -> {
                            String methodId = pOp.persistenceAccessorOperationId().value();
                            String typeFqn = pOp.persistenceAccessorOperationId().typeId().fqn();
                            String typeLabel = simpleLabel(typeFqn);
                            var targetSqlTypes = Json.object();
                            List<String> targets = pOp.persistenceTargets().persistenceTargets().stream()
                                    .map(t -> {
                                        String sqlType = t.operationType()
                                                .map(Enum::name)
                                                .orElse(pOp.persistenceOperationType().name());
                                        targetSqlTypes.and(t.name(), sqlType);
                                        return t.name();
                                    }).toList();

                            targetsSet.addAll(targets);
                            accessorTypesMap.putIfAbsent(typeFqn, typeLabel);
                            if (accessorMethodIds.add(methodId)) {
                                accessorMethodsMap.computeIfAbsent(typeFqn, k -> new ArrayList<>())
                                        .add(Json.object("id", methodId)
                                                .and("sqlType", pOp.persistenceOperationType().name())
                                                .and("targets", Json.array(targets))
                                                .and("targetSqlTypes", targetSqlTypes));
                            }

                            executionToAccessor.add(Json.object("execution", execFqn)
                                    .and("accessor", methodId));
                        });

                        exec.externalAccessors().forEach(ea -> {
                            String accessorFqn = ea.accessorTypeId().fqn();
                            String accessorLabel = simpleLabel(accessorFqn);
                            String accessorMethodName = ea.accessorMethodName();
                            String externalFqn = ea.externalTypeId().fqn();
                            String externalLabel = simpleLabel(externalFqn);
                            String externalMethodName = ea.externalMethodName();

                            externalAccessorLabels.putIfAbsent(accessorFqn, accessorLabel);

                            String methodKey = accessorFqn + "|" + accessorMethodName + "|" + externalFqn + "|" + externalMethodName;
                            if (externalAccessorMethodKeys.add(methodKey)) {
                                externalAccessorMethods
                                        .computeIfAbsent(accessorFqn, k -> new LinkedHashMap<>())
                                        .computeIfAbsent(accessorMethodName, k -> new ArrayList<>())
                                        .add(Json.object("fqn", externalFqn)
                                                .and("label", externalLabel)
                                                .and("method", externalMethodName));
                            }

                            String linkKey = execFqn + "|" + accessorFqn;
                            if (executionToExternalAccessorKeys.add(linkKey)) {
                                executionToExternalAccessor.add(Json.object("execution", execFqn).and("accessor", accessorFqn));
                            }
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
                .and("executionToAccessor", Json.arrayObjects(executionToAccessor))
                .and("executionToExternalAccessor", Json.arrayObjects(executionToExternalAccessor));

        List<JsonObjectBuilder> accessorsList = new ArrayList<>();
        accessorTypesMap.forEach((typeFqn, typeLabel) -> {
            List<JsonObjectBuilder> methods = accessorMethodsMap.getOrDefault(typeFqn, List.of());
            accessorsList.add(Json.object("fqn", typeFqn)
                    .and("label", typeLabel)
                    .and("methods", Json.arrayObjects(methods)));
        });

        List<JsonObjectBuilder> externalAccessorsList = new ArrayList<>();
        externalAccessorLabels.forEach((accessorFqn, accessorLabel) -> {
            LinkedHashMap<String, List<JsonObjectBuilder>> methodsMap = externalAccessorMethods.getOrDefault(accessorFqn, new LinkedHashMap<>());
            List<JsonObjectBuilder> methodsList = new ArrayList<>();
            methodsMap.forEach((methodName, externals) -> {
                methodsList.add(Json.object("name", methodName)
                        .and("externals", Json.arrayObjects(externals)));
            });
            externalAccessorsList.add(Json.object("fqn", accessorFqn)
                    .and("label", accessorLabel)
                    .and("methods", Json.arrayObjects(methodsList)));
        });

        return Json.object("outputPorts", Json.arrayObjects(new ArrayList<>(portsMap.values())))
                .and("outputAdapters", Json.arrayObjects(new ArrayList<>(adaptersMap.values())))
                .and("persistenceAccessors", Json.arrayObjects(accessorsList))
                .and("externalAccessors", Json.arrayObjects(externalAccessorsList))
                .and("targets", Json.array(new ArrayList<>(targetsSet)))
                .and("links", links)
                .build();
    }

    private static String simpleLabel(String typeFqn) {
        return typeFqn.contains(".") ? typeFqn.substring(typeFqn.lastIndexOf('.') + 1) : typeFqn;
    }

}

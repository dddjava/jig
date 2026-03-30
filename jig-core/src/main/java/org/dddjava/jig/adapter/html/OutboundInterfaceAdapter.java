package org.dddjava.jig.adapter.html;

import org.dddjava.jig.adapter.HandleDocument;
import org.dddjava.jig.adapter.JigDocumentWriter;
import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;
import org.dddjava.jig.domain.model.information.JigRepository;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 出力インタフェース
 */
@HandleDocument
public class OutboundInterfaceAdapter {

    private final JigService jigService;
    private final JigDocumentContext jigDocumentContext;

    public OutboundInterfaceAdapter(JigService jigService, JigDocumentContext jigDocumentContext) {
        this.jigService = jigService;
        this.jigDocumentContext = jigDocumentContext;
    }

    @HandleDocument(JigDocument.OutboundInterface)
    public List<Path> invoke(JigRepository repository, JigDocument jigDocument) {
        var outboundAdapters = jigService.outboundAdapters(repository);

        var json = buildJson(outboundAdapters);

        var jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());

        jigDocumentWriter.writeHtmlTemplate();
        jigDocumentWriter.writeJsData("outboundData", json);

        return jigDocumentWriter.outputFilePaths();
    }

    public static String buildJson(OutboundAdapters outboundAdapters) {
        var portsMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var adaptersMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var accessorTypesMap = new LinkedHashSet<String>();              // typeFqn
        var accessorMethodsMap = new LinkedHashMap<String, List<JsonObjectBuilder>>(); // typeFqn → methods
        var accessorMethodIds = new LinkedHashSet<String>();                     // method ID重複排除
        var targetsSet = new LinkedHashSet<String>();

        List<JsonObjectBuilder> operationToExecution = new ArrayList<>();
        List<JsonObjectBuilder> executionToAccessor = new ArrayList<>();
        List<JsonObjectBuilder> executionToExternalAccessor = new ArrayList<>();

        var externalAccessorFqns = new LinkedHashSet<String>();                                                        // fqn
        var externalAccessorMethods = new LinkedHashMap<String, LinkedHashMap<String, List<JsonObjectBuilder>>>();               // accessorFqn → accessorMethodName → externals
        var externalAccessorMethodKeys = new LinkedHashSet<String>();                                                            // dedup key
        var executionToExternalAccessorKeys = new LinkedHashSet<String>();                                                      // dedup key

        outboundAdapters.stream().forEach(outboundAdapter -> {
            String adapterFqn = outboundAdapter.jigType().fqn();
            List<JsonObjectBuilder> execList = new ArrayList<>();

            outboundAdapter.implementsPortStream().forEach(outboundPort -> {
                String portFqn = outboundPort.jigType().fqn();
                List<JsonObjectBuilder> portOperations = new ArrayList<>();

                outboundPort.operationStream().forEach(portOperation -> {
                    String portOperationFqn = portOperation.jigMethod().fqn();
                    portOperations.add(Json.object("fqn", portOperationFqn)
                            .and("signature", portOperation.jigMethod().simpleMethodSignatureText()));

                    outboundAdapter.findExecution(portOperation).ifPresent(adapterExecution -> {
                        String adapterExecutionFqn = adapterExecution.jigMethod().fqn();
                        execList.add(Json.object("fqn", adapterExecutionFqn)
                                .and("signature", adapterExecution.jigMethod().simpleMethodSignatureText()));

                        operationToExecution.add(Json.object("operation", portOperationFqn)
                                .and("execution", adapterExecutionFqn));

                        adapterExecution.persistenceAccessorOperations().forEach(persistenceAccessorOperation -> {
                            String methodId = persistenceAccessorOperation.id().value();
                            String typeFqn = persistenceAccessorOperation.id().typeId().fqn();
                            var targetOperationTypes = Json.object();
                            List<String> targets = persistenceAccessorOperation.targetOperationTypes().persistenceTargets().stream()
                                    .map(persistenceOperation -> {
                                        String operationType = persistenceOperation.operationType().name();
                                        targetOperationTypes.and(persistenceOperation.persistenceTarget().name(), operationType);
                                        return persistenceOperation.persistenceTarget().name();
                                    }).toList();

                            targetsSet.addAll(targets);
                            accessorTypesMap.add(typeFqn);
                            if (accessorMethodIds.add(methodId)) {
                                accessorMethodsMap.computeIfAbsent(typeFqn, k -> new ArrayList<>())
                                        .add(Json.object("id", methodId)
                                                .and("targetOperationTypes", targetOperationTypes));
                            }

                            executionToAccessor.add(Json.object("execution", adapterExecutionFqn)
                                    .and("accessor", methodId));
                        });

                        adapterExecution.otherExternalAccessorOperations().forEach(accessorOperation -> {
                            String accessorFqn = accessorOperation.accessorTypeId().fqn();
                            String accessorMethodName = accessorOperation.accessorMethodName();
                            externalAccessorFqns.add(accessorFqn);

                            accessorOperation.externalMethodCalls().forEach(methodCall -> {

                                String externalFqn = methodCall.methodOwner().fqn();
                                String externalMethodName = methodCall.methodName();

                                String methodKey = accessorFqn + "|" + accessorMethodName + "|" + externalFqn + "|" + externalMethodName;
                                if (externalAccessorMethodKeys.add(methodKey)) {
                                    externalAccessorMethods
                                            .computeIfAbsent(accessorFqn, k -> new LinkedHashMap<>())
                                            .computeIfAbsent(accessorMethodName, k -> new ArrayList<>())
                                            .add(Json.object("fqn", externalFqn)
                                                    .and("method", externalMethodName));
                                }
                            });

                            String linkKey = adapterExecutionFqn + "|" + accessorFqn + "|" + accessorMethodName;
                            if (executionToExternalAccessorKeys.add(linkKey)) {
                                executionToExternalAccessor.add(Json.object("execution", adapterExecutionFqn)
                                        .and("accessor", accessorFqn)
                                        .and("method", accessorMethodName));
                            }
                        });
                    });
                });

                portsMap.putIfAbsent(portFqn, Json.object("fqn", portFqn)
                        .and("operations", Json.arrayObjects(portOperations)));
            });

            adaptersMap.putIfAbsent(adapterFqn, Json.object("fqn", adapterFqn)
                    .and("executions", Json.arrayObjects(execList)));
        });

        var links = Json.object("operationToExecution", Json.arrayObjects(operationToExecution))
                .and("executionToPersistenceAccessor", Json.arrayObjects(executionToAccessor))
                .and("executionToOtherExternalAccessor", Json.arrayObjects(executionToExternalAccessor));

        List<JsonObjectBuilder> accessorsList = new ArrayList<>();
        accessorTypesMap.forEach(typeFqn -> {
            List<JsonObjectBuilder> methods = accessorMethodsMap.getOrDefault(typeFqn, List.of());
            accessorsList.add(Json.object("fqn", typeFqn)
                    .and("methods", Json.arrayObjects(methods)));
        });

        List<JsonObjectBuilder> externalAccessorsList = new ArrayList<>();
        externalAccessorFqns.forEach(accessorFqn -> {
            LinkedHashMap<String, List<JsonObjectBuilder>> methodsMap = externalAccessorMethods.getOrDefault(accessorFqn, new LinkedHashMap<>());
            List<JsonObjectBuilder> methodsList = new ArrayList<>();
            methodsMap.forEach((methodName, externals) -> {
                methodsList.add(Json.object("name", methodName)
                        .and("externals", Json.arrayObjects(externals)));
            });
            externalAccessorsList.add(Json.object("fqn", accessorFqn)
                    .and("methods", Json.arrayObjects(methodsList)));
        });

        return Json.object("outboundPorts", Json.arrayObjects(new ArrayList<>(portsMap.values())))
                .and("outboundAdapters", Json.arrayObjects(new ArrayList<>(adaptersMap.values())))
                .and("persistenceAccessors", Json.arrayObjects(accessorsList))
                .and("otherExternalAccessors", Json.arrayObjects(externalAccessorsList))
                .and("targets", Json.array(new ArrayList<>(targetsSet)))
                .and("links", links)
                .build();
    }

}

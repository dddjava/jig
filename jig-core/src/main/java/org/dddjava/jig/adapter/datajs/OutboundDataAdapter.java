package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * 出力インタフェース（outbound-data.js）
 */
public class OutboundDataAdapter implements DataAdapter {

    private final JigService jigService;

    public OutboundDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "outboundData";
    }

    @Override
    public String dataFileName() {
        return "outbound-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        return buildOutboundJson(jigService.outboundAdapters(jigRepository));
    }

    public static String buildOutboundJson(OutboundAdapters outboundAdapters) {
        var portsMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var adaptersMap = new LinkedHashMap<String, JsonObjectBuilder>();

        // 永続化
        var persistenceAccessorFqns = new LinkedHashSet<String>();
        var persistenceAccessorMethodsMap = new LinkedHashMap<String, List<JsonObjectBuilder>>();
        var accessorMethodIds = new LinkedHashSet<String>();
        var targetsSet = new LinkedHashSet<String>();

        var externalAccessorFqns = new LinkedHashSet<String>();
        var externalAccessorMethods = new LinkedHashMap<String, LinkedHashMap<String, List<JsonObjectBuilder>>>();

        // links
        var operationToExecution = new ArrayList<JsonObjectBuilder>();
        var executionToAccessor = new ArrayList<JsonObjectBuilder>();
        var executionToExternalAccessor = new ArrayList<JsonObjectBuilder>();

        // 重複排除用
        var externalAccessorMethodKeys = new LinkedHashSet<String>();
        var executionToExternalAccessorKeys = new LinkedHashSet<String>();

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
                            persistenceAccessorFqns.add(typeFqn);
                            if (accessorMethodIds.add(methodId)) {
                                persistenceAccessorMethodsMap.computeIfAbsent(typeFqn, k -> new ArrayList<>())
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

        return Json.object("outboundPorts", Json.arrayObjects(new ArrayList<>(portsMap.values())))
                .and("outboundAdapters", Json.arrayObjects(new ArrayList<>(adaptersMap.values())))
                .and("persistenceAccessors", persistenceAccessors(persistenceAccessorFqns, persistenceAccessorMethodsMap))
                .and("otherExternalAccessors", otherExternalAccessors(externalAccessorFqns, externalAccessorMethods))
                .and("targets", Json.array(new ArrayList<>(targetsSet)))
                .and("links", links)
                .build();
    }

    private static Object persistenceAccessors(LinkedHashSet<String> persistenceAccessorFqns, LinkedHashMap<String, List<JsonObjectBuilder>> accessorMethodsMap) {
        List<JsonObjectBuilder> accessorsList = new ArrayList<>();
        persistenceAccessorFqns.forEach(typeFqn -> {
            List<JsonObjectBuilder> methods = accessorMethodsMap.getOrDefault(typeFqn, List.of());
            accessorsList.add(Json.object("fqn", typeFqn)
                    .and("methods", Json.arrayObjects(methods)));
        });
        return Json.arrayObjects(accessorsList);
    }

    private static Object otherExternalAccessors(LinkedHashSet<String> externalAccessorFqns, LinkedHashMap<String, LinkedHashMap<String, List<JsonObjectBuilder>>> externalAccessorMethods) {
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
        return Json.arrayObjects(externalAccessorsList);
    }
}

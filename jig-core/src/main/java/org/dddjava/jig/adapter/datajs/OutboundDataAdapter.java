package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;

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
        return buildOutboundJson(jigService.outboundAdapters(jigRepository), jigRepository.externalAccessorRepositories().otherExternalAccessorRepository());
    }

    public static String buildOutboundJson(OutboundAdapters outboundAdapters, OtherExternalAccessorRepository otherExternalAccessorRepository) {
        var portsMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var adaptersMap = new LinkedHashMap<String, JsonObjectBuilder>();

        // 永続化
        var persistenceAccessorFqns = new LinkedHashSet<String>();
        var persistenceAccessorMethodsMap = new LinkedHashMap<String, List<JsonObjectBuilder>>();
        var accessorMethodIds = new LinkedHashSet<String>();
        var targetsSet = new LinkedHashSet<String>();

        // links
        var operationToExecution = new ArrayList<JsonObjectBuilder>();
        var executionToAccessor = new ArrayList<JsonObjectBuilder>();
        var executionToExternalAccessor = new ArrayList<JsonObjectBuilder>();

        // 重複排除用
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
                .and("otherExternalAccessors", externalAccessors(otherExternalAccessorRepository))
                .and("targets", Json.array(new ArrayList<>(targetsSet)))
                .and("links", links)
                .build();
    }

    private static Object persistenceAccessors(LinkedHashSet<String> persistenceAccessorFqns, LinkedHashMap<String, List<JsonObjectBuilder>> accessorMethodsMap) {
        List<JsonObjectBuilder> accessorsList = new ArrayList<>();
        persistenceAccessorFqns.forEach(typeFqn -> {
            List<JsonObjectBuilder> methods = accessorMethodsMap.get(typeFqn);
            accessorsList.add(Json.object("fqn", typeFqn)
                    .and("methods", Json.arrayObjects(methods)));
        });
        return Json.arrayObjects(accessorsList);
    }

    private static Object externalAccessors(OtherExternalAccessorRepository otherExternalAccessorRepository) {
        List<JsonObjectBuilder> list = otherExternalAccessorRepository.values().stream()
                .map(externalAccessor -> Json
                        .object("fqn", externalAccessor.typeId().fqn())
                        // TODO メソッドとして出すならJsonSupportにかえたい。かえないならmethodsじゃなくoperationsで出力する方がよさそう。
                        .and("methods", Json.arrayObjects(externalAccessor.operations().stream()
                                .map(operation -> Json
                                        // TODO メソッド名だけ出力しているため、オーバーロードされてると区別つかない
                                        .object("name", operation.accessorJigMethod().name())
                                        .and("externals", Json.arrayObjects(operation.externalMethodCalls().stream()
                                                .map(methodCall -> Json
                                                        .object("fqn", methodCall.methodOwner().fqn())
                                                        .and("method", methodCall.methodName()))
                                                .toList())))
                                .toList())))
                .toList();
        return Json.arrayObjects(list);
    }
}

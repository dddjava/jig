package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.data.persistence.PersistenceAccessorRepository;
import org.dddjava.jig.domain.model.information.outbound.ExternalAccessorRepositories;
import org.dddjava.jig.domain.model.information.outbound.OutboundAdapters;
import org.dddjava.jig.domain.model.information.outbound.other.OtherExternalAccessorRepository;
import org.dddjava.jig.domain.model.knowledge.usecases.ServiceAngles;
import org.dddjava.jig.domain.model.knowledge.usecases.Usecase;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return buildOutboundJson(
                jigService.outboundAdapters(jigRepository),
                jigRepository.externalAccessorRepositories(),
                jigService.serviceAngles(jigRepository)
        );
    }

    public static String buildOutboundJson(OutboundAdapters outboundAdapters, ExternalAccessorRepositories externalAccessorRepositories, ServiceAngles serviceAngles) {
        // JigMethodId → Usecase の逆引きマップ（BFS用）
        Map<JigMethodId, Usecase> usecaseByMethodId = serviceAngles.list().stream()
                .collect(Collectors.toMap(Usecase::jigMethodId, u -> u, (a, b) -> a));

        // portOperationFqn → 呼び出し元 public Usecase FQN セット（間接呼び出しも含む）
        Map<String, LinkedHashSet<String>> portOperationToCallerUsecases = new HashMap<>();
        serviceAngles.list().forEach(usecase ->
                usecase.usingRepositoryMethods().forEach(repoMethod ->
                        findPublicCallerFqns(usecase, usecaseByMethodId).forEach(callerFqn ->
                                portOperationToCallerUsecases
                                        .computeIfAbsent(repoMethod.fqn(), k -> new LinkedHashSet<>())
                                        .add(callerFqn)
                        )
                )
        );

        var portsMap = new LinkedHashMap<String, JsonObjectBuilder>();
        var adaptersMap = new LinkedHashMap<String, JsonObjectBuilder>();

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
                    portOperations.add(JsonSupport.buildMethodJson(portOperation.jigMethod())
                            .and("callerUsecases", Json.array(portOperationToCallerUsecases.getOrDefault(portOperationFqn, new LinkedHashSet<>()))));

                    outboundAdapter.findExecution(portOperation).ifPresent(adapterExecution -> {
                        String adapterExecutionFqn = adapterExecution.jigMethod().fqn();
                        execList.add(JsonSupport.buildMethodJson(adapterExecution.jigMethod()));

                        operationToExecution.add(Json.object("operation", portOperationFqn)
                                .and("execution", adapterExecutionFqn));

                        adapterExecution.persistenceAccessorOperations().forEach(persistenceAccessorOperation -> {
                            persistenceAccessorOperation.targetOperationTypes().persistenceTargets()
                                    .forEach(pt -> targetsSet.add(pt.persistenceTarget().name()));

                            executionToAccessor.add(Json.object("execution", adapterExecutionFqn)
                                    .and("accessor", persistenceAccessorOperation.id().value()));
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

        return Json.object("outboundPorts", Json.arrayObjects(portsMap.values()))
                .and("outboundAdapters", Json.arrayObjects(adaptersMap.values()))
                .and("persistenceAccessors", persistenceAccessors(externalAccessorRepositories.persistenceAccessorRepository()))
                .and("otherExternalAccessors", externalAccessors(externalAccessorRepositories.otherExternalAccessorRepository()))
                .and("targets", Json.array(targetsSet))
                .and("links", links)
                .build();
    }

    private static List<String> findPublicCallerFqns(Usecase usecase, Map<JigMethodId, Usecase> usecaseByMethodId) {
        if (usecase.serviceMethod().method().isPublic()) {
            return List.of(usecase.serviceMethod().method().fqn());
        }
        // 非publicメソッドはBFSでpublicな祖先を辿る
        var result = new ArrayList<String>();
        var visited = new HashSet<JigMethodId>();
        var queue = new ArrayDeque<Usecase>();
        queue.add(usecase);
        while (!queue.isEmpty()) {
            var current = queue.poll();
            for (var callerId : current.userServiceMethods()) {
                if (visited.add(callerId)) {
                    var callerUsecase = usecaseByMethodId.get(callerId);
                    if (callerUsecase != null) {
                        if (callerUsecase.serviceMethod().method().isPublic()) {
                            result.add(callerUsecase.serviceMethod().method().fqn());
                        } else {
                            queue.add(callerUsecase);
                        }
                    }
                }
            }
        }
        return result;
    }

    private static Object persistenceAccessors(PersistenceAccessorRepository persistenceAccessorRepository) {
        List<JsonObjectBuilder> accessorsList = persistenceAccessorRepository.values().stream()
                .map(accessor -> {
                    List<JsonObjectBuilder> methods = accessor.persistenceAccessorOperations().stream()
                            .map(operation -> {
                                var targetOperationTypes = Json.object();
                                operation.targetOperationTypes().persistenceTargets()
                                        .forEach(pt -> targetOperationTypes.and(pt.persistenceTarget().name(), pt.operationType().name()));
                                return Json.object("id", operation.id().value())
                                        .and("targetOperationTypes", targetOperationTypes);
                            })
                            .toList();
                    return Json.object("fqn", accessor.typeId().fqn())
                            .and("methods", Json.arrayObjects(methods));
                })
                .toList();
        return Json.arrayObjects(accessorsList);
    }

    private static Object externalAccessors(OtherExternalAccessorRepository otherExternalAccessorRepository) {
        List<JsonObjectBuilder> list = otherExternalAccessorRepository.values().stream()
                .map(externalAccessor -> Json
                        .object("fqn", externalAccessor.typeId().fqn())
                        .and("operations", Json.arrayObjects(externalAccessor.operations().stream()
                                .map(operation -> JsonSupport.buildMethodJson(operation.accessorJigMethod())
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

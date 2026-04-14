package org.dddjava.jig.adapter.datajs;

import org.dddjava.jig.adapter.json.Json;
import org.dddjava.jig.adapter.json.JsonObjectBuilder;
import org.dddjava.jig.adapter.json.JsonSupport;
import org.dddjava.jig.application.JigRepository;
import org.dddjava.jig.application.JigService;
import org.dddjava.jig.domain.model.data.members.instruction.MethodCall;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigTypes;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ユースケース（usecase-data.js）
 */
public class UsecaseDataAdapter implements DataAdapter {

    private final JigService jigService;

    public UsecaseDataAdapter(JigService jigService) {
        this.jigService = jigService;
    }

    @Override
    public String variableName() {
        return "usecaseData";
    }

    @Override
    public String dataFileName() {
        return "usecase-data";
    }

    @Override
    public String buildJson(JigRepository jigRepository) {
        return buildUsecaseJson(jigService.serviceTypes(jigRepository));
    }

    public static String buildUsecaseJson(JigTypes contextJigTypes) {
        var usecaseList = contextJigTypes.stream()
                .flatMap(jigType -> {
                    List<JsonObjectBuilder> fields = jigType.instanceJigFields().fields().stream()
                            .map(JsonSupport::buildFieldJson)
                            .collect(Collectors.toList());

                    List<JsonObjectBuilder> staticMethods = jigType.staticJigMethods().stream()
                            .filter(jigMethod -> jigMethod.isProgrammerDefined())
                            .map(UsecaseDataAdapter::buildMethodJson)
                            .collect(Collectors.toList());

                    List<JsonObjectBuilder> methodList = jigType.instanceJigMethods().stream()
                            .filter(jigMethod -> jigMethod.isProgrammerDefined())
                            .map(UsecaseDataAdapter::buildMethodJson)
                            .toList();

                    if (methodList.isEmpty() && fields.isEmpty() && staticMethods.isEmpty()) {
                        return Stream.empty();
                    }

                    return Stream.of(
                            Json.object("fqn", jigType.fqn())
                                    .and("fields", Json.arrayObjects(fields))
                                    .and("staticMethods", Json.arrayObjects(staticMethods))
                                    .and("methods", Json.arrayObjects(methodList)));
                })
                .toList();

        return Json.object("usecases", Json.arrayObjects(usecaseList)).build();
    }

    private static JsonObjectBuilder buildMethodJson(JigMethod jigMethod) {
        return JsonSupport.buildMethodJson(jigMethod)
                .and("callMethods", Json.array(jigMethod.lambdaInlinedMethodCallStream()
                        .filter(methodCall -> methodCall.isXxx())
                        .map(MethodCall::fqn)
                        .toList()));
    }
}

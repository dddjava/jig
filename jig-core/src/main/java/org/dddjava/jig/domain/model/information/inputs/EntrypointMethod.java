package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.data.classes.method.JigMethod;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.JigType;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.Optional;

/**
 * ハンドラ
 *
 * 外部からのリクエストを受け取る起点となるメソッドです。
 * 制限事項：RequestMappingをメタアノテーションとした独自アノテーションが付与されたメソッドは、ハンドラとして扱われません。
 */
public record EntrypointMethod(EntrypointType entrypointType, JigType jigType, JigMethod jigMethod) {

    public boolean anyMatch(CallerMethods callerMethods) {
        return callerMethods.contains(jigMethod.declaration());
    }

    public boolean isCall(MethodDeclaration methodDeclaration) {
        return jigMethod.usingMethods().methodDeclarations().contains(methodDeclaration);
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public static final String[] _RequestMapping = {
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
    };

    public static final String[] _RabbitListener = {
            "org.springframework.amqp.rabbit.annotation.RabbitListener"
    };

    public String interfacePointDescription() {
        var methodAnnotations = jigMethod.methodAnnotations();

        var requestMappingPath = methodAnnotations.list().stream()
                .filter(methodAnnotation -> methodAnnotation.annotationType().anyEquals(_RequestMapping))
                .map(methodAnnotation -> {
                    var httpMethod = switch (methodAnnotation.annotationType().asSimpleText()) {
                        case "GetMapping" -> "GET";
                        case "PostMapping" -> "POST";
                        case "PutMapping" -> "PUT";
                        case "DeleteMapping" -> "DELETE";
                        case "PatchMapping" -> "PATCH";
                        default -> "???";
                    };
                    var pathDescription = methodAnnotation.annotation().descriptionTextAnyOf("value", "path")
                            // valueもpathがなかったり空文字であってもRequestMappingがあれば "/" にバインドされるので明示しておく
                            .filter(path -> !path.isEmpty()).orElse("/");

                    return "%s %s".formatted(httpMethod, pathDescription);
                })
                // アノテーションは複数取れないはずなのでこれで。
                .findFirst();

        Optional<String> rabbitListenerQueues = methodAnnotations.list().stream()
                .filter(methodAnnotation -> methodAnnotation.annotationType().anyEquals(_RabbitListener))
                .map(methodAnnotation -> {
                    // queue複数未対応
                    var queueName = methodAnnotation.annotation().descriptionTextAnyOf("queues");
                    return "queue: " + queueName.orElse("???");
                })
                // アノテーションは複数取れないはずなのでこれで。
                .findFirst();

        return requestMappingPath
                .or(() -> rabbitListenerQueues)
                .orElseGet(() -> {
                    // 想定するdescriptionがなかった場合。想定が増えたら追加する。
                    // api methodと判定されてるのにアノテーションがなかった場合もきちゃうけど、それは想定していない感じ
                    return "???";
                });
    }

    public String interfaceLabelText() {
        if (isRequestMappingMethod()) {
            var optOperationSummary = jigMethod.methodAnnotations().list().stream()
                    .filter(methodAnnotation -> methodAnnotation.annotationType().anyEquals("io.swagger.v3.oas.annotations.Operation"))
                    .flatMap(methodAnnotation -> methodAnnotation.annotation().descriptionTextAnyOf("summary").stream())
                    .findAny();

            return optOperationSummary.orElseGet(jigMethod::labelText);
        }

        return jigMethod.labelText();
    }

    private boolean isRequestMappingMethod() {
        return jigMethod.methodAnnotations().list().stream()
                .anyMatch(methodAnnotation -> methodAnnotation.annotationType().anyEquals(_RequestMapping));
    }

    public MethodDeclaration declaration() {
        return jigMethod.declaration();
    }
}

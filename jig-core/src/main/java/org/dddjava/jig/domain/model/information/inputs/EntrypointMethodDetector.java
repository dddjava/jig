package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * エントリポイント検出器
 */
public class EntrypointMethodDetector {

    private final DetectorConditions detectorConditions;

    public EntrypointMethodDetector() {
        detectorConditions = new DetectorConditions(List.of(
                new DetectorCondition(EntrypointType.HTTP_API,
                        List.of("org.springframework.stereotype.Controller",
                                "org.springframework.web.bind.annotation.RestController",
                                "org.springframework.web.bind.annotation.ControllerAdvice"),
                        List.of("org.springframework.web.bind.annotation.RequestMapping",
                                "org.springframework.web.bind.annotation.GetMapping",
                                "org.springframework.web.bind.annotation.PostMapping",
                                "org.springframework.web.bind.annotation.PutMapping",
                                "org.springframework.web.bind.annotation.DeleteMapping",
                                "org.springframework.web.bind.annotation.PatchMapping")),
                new DetectorCondition(EntrypointType.QUEUE_LISTENER,
                        List.of("org.springframework.stereotype.Component"),
                        List.of("org.springframework.amqp.rabbit.annotation.RabbitListener")),
                // TODO カスタムアノテーション対応 https://github.com/dddjava/jig/issues/343
                new DetectorCondition(EntrypointType.OTHER,
                        List.of("org.dddjava.jig.adapter.HandleDocument"),
                        List.of("org.dddjava.jig.adapter.HandleDocument"))
        ));
    }

    record DetectorConditions(List<DetectorCondition> detectorConditions) {

        Stream<DetectorCondition> stream() {
            return detectorConditions.stream();
        }
    }

    record DetectorCondition(EntrypointType entrypointType,
                             List<String> classAnnotations, List<String> methodAnnotations) {
    }

    Collection<EntrypointMethod> collectMethod(JigType jigType) {
        return detectorConditions.stream()
                .flatMap(detectorCondition -> {
                    if (detectorCondition.classAnnotations().stream().map(TypeId::valueOf)
                            .anyMatch(jigType::hasAnnotation)) {
                        return jigType.instanceJigMethodStream().filter(jigMethod ->
                                        detectorCondition.methodAnnotations().stream().map(TypeId::valueOf)
                                                .anyMatch(jigMethod::hasAnnotation))
                                .map(jigMethod -> new EntrypointMethod(detectorCondition.entrypointType(), jigType, jigMethod));
                    }
                    return Stream.empty();
                })
                .toList();
    }
}

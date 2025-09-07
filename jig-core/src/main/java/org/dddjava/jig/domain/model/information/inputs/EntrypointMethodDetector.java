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

    private final List<EntrypointAnnotation> entrypointAnnotations;

    public EntrypointMethodDetector() {
        entrypointAnnotations = List.of(
                new EntrypointAnnotation(EntrypointType.HTTP_API,
                        List.of("org.springframework.stereotype.Controller",
                                "org.springframework.web.bind.annotation.RestController",
                                "org.springframework.web.bind.annotation.ControllerAdvice"),
                        List.of("org.springframework.web.bind.annotation.RequestMapping",
                                "org.springframework.web.bind.annotation.GetMapping",
                                "org.springframework.web.bind.annotation.PostMapping",
                                "org.springframework.web.bind.annotation.PutMapping",
                                "org.springframework.web.bind.annotation.DeleteMapping",
                                "org.springframework.web.bind.annotation.PatchMapping")),
                new EntrypointAnnotation(EntrypointType.QUEUE_LISTENER,
                        List.of("org.springframework.stereotype.Component"),
                        List.of("org.springframework.amqp.rabbit.annotation.RabbitListener")),
                // TODO カスタムアノテーション対応 https://github.com/dddjava/jig/issues/343
                new EntrypointAnnotation(EntrypointType.OTHER,
                        List.of("org.dddjava.jig.adapter.HandleDocument"),
                        List.of("org.dddjava.jig.adapter.HandleDocument"))
        );
    }

    record EntrypointAnnotation(EntrypointType entrypointType,
                                List<String> classAnnotations, List<String> methodAnnotations) {
    }

    Collection<Entrypoint> collectMethod(JigType jigType) {
        return entrypointAnnotations.stream()
                .flatMap(entrypointAnnotation -> {
                    if (entrypointAnnotation.classAnnotations().stream().map(TypeId::valueOf)
                            .anyMatch(jigType::hasAnnotation)) {
                        return jigType.instanceJigMethodStream().filter(jigMethod ->
                                        entrypointAnnotation.methodAnnotations().stream().map(TypeId::valueOf)
                                                .anyMatch(jigMethod::hasAnnotation))
                                .map(jigMethod -> new Entrypoint(entrypointAnnotation.entrypointType(), jigType, jigMethod));
                    }
                    return Stream.empty();
                })
                .toList();
    }
}

package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * エントリポイント検出器
 */
public class EntrypointMethodDetector {

    private static final Logger logger = LoggerFactory.getLogger(EntrypointMethodDetector.class);

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
                        return jigType.instanceJigMethodStream()
                                .filter(jigMethod -> entrypointAnnotation.methodAnnotations().stream().map(TypeId::valueOf)
                                        .anyMatch(jigMethod::hasAnnotation))
                                .map(jigMethod -> {
                                    var entrypointMapping = getEntrypointMapping(jigType, entrypointAnnotation, jigMethod);
                                    return new Entrypoint(entrypointAnnotation.entrypointType(), jigType, jigMethod, entrypointMapping);
                                });
                    }
                    return Stream.empty();
                })
                .toList();
    }

    private EntrypointMapping getEntrypointMapping(JigType jigType, EntrypointAnnotation entrypointAnnotation, JigMethod jigMethod) {
        if (entrypointAnnotation.entrypointType() == EntrypointType.HTTP_API) {
            var methodAnnotations = jigMethod.declarationAnnotationStream()
                    .filter(jigAnnotationReference -> {
                        var typeId = jigAnnotationReference.id();
                        return entrypointAnnotation.methodAnnotations().stream()
                                .map(TypeId::valueOf)
                                .anyMatch(typeId::equals);
                    })
                    .toList();
            if (!methodAnnotations.isEmpty()) {
                JigAnnotationReference httpMapping = methodAnnotations.get(0);
                if (methodAnnotations.size() > 1) {
                    logger.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。出力には1件目を採用します。", jigMethod.simpleText());
                }
                // HTTPハンドラメソッド
                return HttpEntrypointMapping.from(jigType, httpMapping);
            }
        } else if (entrypointAnnotation.entrypointType() == EntrypointType.QUEUE_LISTENER) {
            return new QueueListenerEntrypointMapping(jigMethod);
        }
        // デフォルト
        return new EntrypointMapping() {
        };
    }
}

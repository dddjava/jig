package org.dddjava.jig.domain.model.information.inbound;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigAnnotations;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.dddjava.jig.domain.model.information.types.SpringAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

/**
 * エントリポイント検出器
 */
class EntrypointMethodDetector {

    private static final Logger logger = LoggerFactory.getLogger(EntrypointMethodDetector.class);

    private final List<EntrypointAnnotation> entrypointAnnotations;

    public EntrypointMethodDetector() {
        entrypointAnnotations = List.of(
                new EntrypointAnnotation(EntrypointType.HTTP_API,
                        List.of(SpringAnnotations.CONTROLLER,
                                SpringAnnotations.REST_CONTROLLER,
                                SpringAnnotations.CONTROLLER_ADVICE),
                        List.of(SpringAnnotations.REQUEST_MAPPING,
                                SpringAnnotations.GET_MAPPING,
                                SpringAnnotations.POST_MAPPING,
                                SpringAnnotations.PUT_MAPPING,
                                SpringAnnotations.DELETE_MAPPING,
                                SpringAnnotations.PATCH_MAPPING)),
                new EntrypointAnnotation(EntrypointType.QUEUE_LISTENER,
                        List.of(SpringAnnotations.COMPONENT),
                        List.of(SpringAnnotations.RABBIT_LISTENER)),
                // TODO カスタムアノテーション対応 https://github.com/dddjava/jig/issues/343
                new EntrypointAnnotation(EntrypointType.OTHER,
                        List.of(JigAnnotations.HANDLE_DOCUMENT),
                        List.of(JigAnnotations.HANDLE_DOCUMENT))
        );
    }

    record EntrypointAnnotation(EntrypointType entrypointType,
                                List<TypeId> classAnnotations, List<TypeId> methodAnnotations) {
    }

    Collection<Entrypoint> collectMethod(JigType jigType) {
        return entrypointAnnotations.stream()
                .flatMap(entrypointAnnotation -> {
                    if (entrypointAnnotation.classAnnotations().stream()
                            .anyMatch(jigType::hasAnnotation)) {
                        return jigType.instanceJigMethodStream()
                                .filter(jigMethod -> entrypointAnnotation.methodAnnotations().stream()
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
                                .anyMatch(typeId::equals);
                    })
                    .toList();
            if (!methodAnnotations.isEmpty()) {
                JigAnnotationReference mappingAnnotation = methodAnnotations.get(0);
                if (methodAnnotations.size() > 1) {
                    logger.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。出力には1件目を採用します。", jigMethod.simpleText());
                }
                // HTTPハンドラメソッド
                return HttpEntrypointMapping.from(jigType, mappingAnnotation);
            }
        } else if (entrypointAnnotation.entrypointType() == EntrypointType.QUEUE_LISTENER) {
            return new QueueListenerEntrypointMapping(jigMethod);
        }
        // デフォルト
        return EntrypointMapping.DEFAULT;
    }
}

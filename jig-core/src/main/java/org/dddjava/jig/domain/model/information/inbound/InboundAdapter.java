package org.dddjava.jig.domain.model.information.inbound;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.Optional;

/**
 * 入力アダプタとなるクラス。
 * 複数のエントリーポイントを持つ。
 *
 * - SpringMVCのControllerのRequestMapping
 * - SpringRabbitのRabbitListener
 */
public record InboundAdapter(JigType jigType, Collection<Entrypoint> entrypoints) {
    public InboundAdapter {
        if (entrypoints.isEmpty()) throw new IllegalArgumentException("entrypointMethods is empty");
    }

    static final EntrypointMethodDetector entrypointMethodDetector = new EntrypointMethodDetector();

    static Optional<InboundAdapter> from(JigType jigType) {
        return Optional.of(entrypointMethodDetector.collectMethod(jigType))
                // 1つもエントリーポイントがない場合はInboundAdapterではないものとして弾く
                .filter(detectedMethods -> !detectedMethods.isEmpty())
                .map(detectedMethods -> new InboundAdapter(jigType, detectedMethods));
    }

}

package org.dddjava.jig.domain.model.information.inputs;

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
public record InputAdapter(JigType jigType, Collection<Entrypoint> entrypoints) {
    public InputAdapter {
        if (entrypoints.isEmpty()) throw new IllegalArgumentException("entrypointMethods is empty");
    }

    static Optional<InputAdapter> from(EntrypointMethodDetector entrypointMethodDetector, JigType jigType) {
        var entrypointMethods = entrypointMethodDetector.collectMethod(jigType);
        if (entrypointMethods.isEmpty()) {
            // エントリーポイントではない
            return Optional.empty();
        }
        return Optional.of(new InputAdapter(jigType, entrypointMethods));
    }

}

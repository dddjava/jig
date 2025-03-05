package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Collection;
import java.util.Optional;

/**
 * エントリーポイントメソッドのグループ。
 * グルーピング単位はクラス。
 *
 * - SpringMVCのControllerのRequestMapping
 * - SpringRabbitのRabbitListener
 */
public record EntrypointGroup(JigType jigType, Collection<EntrypointMethod> entrypointMethods) {
    public EntrypointGroup {
        if (entrypointMethods.isEmpty()) throw new IllegalArgumentException("entrypointMethods is empty");
    }

    static Optional<EntrypointGroup> from(EntrypointMethodDetector entrypointMethodDetector, JigType jigType) {
        var entrypointMethods = entrypointMethodDetector.collectMethod(jigType);
        if (!entrypointMethods.isEmpty()) {
            return Optional.of(new EntrypointGroup(jigType, entrypointMethods));
        }
        // not entrypoint
        return Optional.empty();
    }

}

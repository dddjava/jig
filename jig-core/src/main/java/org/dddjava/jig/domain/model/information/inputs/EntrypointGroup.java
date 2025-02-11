package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.List;
import java.util.Optional;

/**
 * エントリーポイントメソッドのグループ。
 * グルーピング単位はクラス。
 *
 * - SpringMVCのControllerのRequestMapping
 * - SpringRabbitのRabbitListener
 */
public record EntrypointGroup(JigType jigType, List<EntrypointMethod> entrypointMethod) {
    public EntrypointGroup {
        if (entrypointMethod.isEmpty()) throw new IllegalArgumentException("entrypointMethod is empty");
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

package org.dddjava.jig.domain.model.models.applications.frontends;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;

/**
 * 外部APIのインタフェース
 *
 * - SpringMVCのControllerのRequestMapping
 * - SpringRabbitのRabbitListener
 */
public record EntrypointGroup
        (JigType jigType, HandlerMethods handlerMethods) {

    public boolean hasEntrypoint() {
        return !handlerMethods().empty();
    }
}

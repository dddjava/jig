package org.dddjava.jig.domain.model.information.inbound;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.SpringAnnotations;

record QueueListenerEntrypointMapping(JigMethod jigMethod) implements EntrypointMapping {

    @Override
    public String fullPathText() {
        return jigMethod.declarationAnnotationStream()
                .filter(jigAnnotationReference -> jigAnnotationReference.id().equals(SpringAnnotations.RABBIT_LISTENER))
                .map(jigAnnotationReference -> {
                    // queueは複数記述できるが、たぶんしないので一件目をとってくる
                    return jigAnnotationReference.elementTextOf("queues").orElse("???");
                })
                // RabbitListenerアノテーションは複数取れないはずなのでAnyでOK。
                .findAny()
                .orElse("???");
    }
}

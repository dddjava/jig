package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;

record QueueListenerEntrypointMapping(JigMethod jigMethod) implements EntrypointMapping {

    @Override
    public String fullPathText() {
        return jigMethod.declarationAnnotationStream()
                .filter(jigAnnotationReference -> jigAnnotationReference.id().equals(TypeId.valueOf("org.springframework.amqp.rabbit.annotation.RabbitListener")))
                .map(jigAnnotationReference -> {
                    // queueは複数記述できるが、たぶんしないので一件目をとってくる
                    return jigAnnotationReference.elementTextOf("queues").orElse("???");
                })
                // RabbitListenerアノテーションは複数取れないはずなのでAnyでOK。
                .findAny()
                .orElse("???");
    }
}

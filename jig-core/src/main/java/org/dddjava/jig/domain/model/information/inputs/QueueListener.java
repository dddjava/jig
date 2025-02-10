package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

public record QueueListener(EntrypointMethod entrypointMethod) {

    public static QueueListener from(EntrypointMethod entrypointMethod) {
        return new QueueListener(entrypointMethod);
    }

    public String queueName() {
        return entrypointMethod.jigMethod().declarationAnnotationStream()
                .filter(jigAnnotationReference -> jigAnnotationReference.id().equals(TypeIdentifier.valueOf("org.springframework.amqp.rabbit.annotation.RabbitListener")))
                .map(jigAnnotationReference -> {
                    // queueは複数記述できるが、たぶんしないので一件目をとってくる
                    return jigAnnotationReference.elementTextOf("queues").orElse("???");
                })
                // RabbitListenerアノテーションは複数取れないはずなのでAnyでOK。
                .findAny()
                .orElse("???");
    }
}

package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

public record QueueListener(EntrypointMethod entrypointMethod) {

    public static QueueListener from(EntrypointMethod entrypointMethod) {
        return new QueueListener(entrypointMethod);
    }

    public String queueName() {
        return entrypointMethod.jigMethod().methodAnnotations().list().stream()
                .filter(methodAnnotation -> methodAnnotation.annotationType().equals(TypeIdentifier.valueOf("org.springframework.amqp.rabbit.annotation.RabbitListener")))
                .map(methodAnnotation -> {
                    // queueは複数記述できるが、たぶんしないので一件目をとってくる
                    var queueName = methodAnnotation.annotation().descriptionTextAnyOf("queues");
                    return queueName.orElse("???");
                })
                // アノテーションは複数取れないはずなのでこれで。
                .findAny()
                .orElse("???");
    }
}

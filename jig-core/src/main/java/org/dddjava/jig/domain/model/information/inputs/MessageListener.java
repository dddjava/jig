package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * メッセージリスナー
 *
 * エントリーポイントの特化型
 */
public record MessageListener(Entrypoint entrypoint) {

    public static MessageListener from(Entrypoint entrypoint) {
        return new MessageListener(entrypoint);
    }

    public String queueName() {
        return entrypoint.jigMethod().declarationAnnotationStream()
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

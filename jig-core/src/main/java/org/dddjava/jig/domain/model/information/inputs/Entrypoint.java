package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * 入力アダプタのエントリーポイント
 *
 * 外部からのリクエストを受け取る起点となるメソッドです。
 * リクエストハンドラメソッドやリスナーメソッド、スケジュールメソッドなどが該当します。
 *
 * 制限事項：RequestMappingをメタアノテーションとした独自アノテーションが付与されたメソッドは、ハンドラとして扱われません。
 */
public record Entrypoint(EntrypointType entrypointType, JigType jigType, JigMethod jigMethod) {

    public boolean anyMatch(CallerMethods callerMethods) {
        return callerMethods.contains(jigMethod.jigMethodId());
    }

    public TypeId typeId() {
        return jigType.id();
    }

    public PackageId packageId() {
        return jigType.packageId();
    }

    /**
     * エントリーポイントのパス
     *
     * HTTP_APIは `GET /get` のようなHTTPメソッドとパスの組み合わせ。
     * QUEUE_LISTENERは `queue: my-queue` のようにキュー名。
     * それ以外は種別の文字列表現。
     */
    public String pathText() {
        return switch (entrypointType()) {
            case HTTP_API -> {
                var httpEndpoint = HttpEntrypointPath.from(this);
                yield "%s %s".formatted(httpEndpoint.method(), httpEndpoint.methodPath());
            }
            case QUEUE_LISTENER -> "queue: %s".formatted(MessageListener.from(this).queueName());
            default -> this.entrypointType().toString();
        };
    }

    public String methodLabelText() {
        if (entrypointType() == EntrypointType.HTTP_API) {
            return HttpEntrypointPath.from(this).interfaceLabel();
        }
        return jigMethod().labelText();
    }
}

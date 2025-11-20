package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.TypeId;
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
public record Entrypoint(EntrypointType entrypointType, JigType jigType, JigMethod jigMethod,
                         EntrypointMapping entrypointMapping) {

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
        return entrypointMapping.shortPathText();
    }

    public String methodLabelText() {
        return entrypointName();
    }

    public String fullPathText() {
        return entrypointMapping.fullPathText();
    }

    private String entrypointName() {
        if (entrypointType() == EntrypointType.HTTP_API) {
            return jigMethod
                    // Swaggerのアノテーションのsummaryが記述されていればそれを採用
                    .declarationAnnotationStream()
                    .filter(methodAnnotation -> methodAnnotation.id().equals(TypeId.valueOf("io.swagger.v3.oas.annotations.Operation")))
                    .flatMap(methodAnnotation -> methodAnnotation.elementTextOf("summary").stream())
                    // アノテーションの仕様上、同じアノテーションが複数あることもあるし、要素の文字列も配列で定義可能なのでAnyで取得。実際は0..1になる。
                    .findAny()
                    // OpenAPIドキュメントの自動生成をしていないなど、解決できない場合は通常のメソッドラベル
                    .orElseGet(jigMethod::labelText);
        } else {
            return jigMethod().labelText();
        }
    }
}

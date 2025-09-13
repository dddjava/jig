package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.packages.PackageId;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

/**
 * 入力アダプタのエントリーポイント
 *
 * 外部からのリクエストを受け取る起点となるメソッドです。
 * リクエストハンドラメソッドやリスナーメソッド、スケジュールメソッドなどが該当します。
 *
 * 制限事項：RequestMappingをメタアノテーションとした独自アノテーションが付与されたメソッドは、ハンドラとして扱われません。
 */
public record Entrypoint(EntrypointType entrypointType, JigType jigType, JigMethod jigMethod,
                         JigAnnotationReference httpMappingAnnotation) {

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
                // これだとクラスのパスがはいってない
                yield "%s %s".formatted(httpEndpoint.method(), httpEndpoint.methodPath());
            }
            case QUEUE_LISTENER -> "queue: %s".formatted(MessageListener.from(this).queueName());
            default -> this.entrypointType().toString();
        };
    }

    public String methodLabelText() {
        if (entrypointType() == EntrypointType.HTTP_API) {
            return HttpEntrypointPath.from(this).entrypointName();
        }
        return jigMethod().labelText();
    }

    public String fullPathText() {
        return HttpEntrypointPath.from(this).pathText();
    }

    /**
     * HTTPリクエストのエンドポイント
     *
     * エントリーポイントから情報を抜き出したもの。
     */
    private record HttpEntrypointPath(String method, String entrypointName, String classPath, String methodPath) {
        private static final Logger logger = LoggerFactory.getLogger(HttpEntrypointPath.class);

        public static HttpEntrypointPath from(Entrypoint entrypoint) {
            var jigMethod = entrypoint.jigMethod();

            // NOTE: valueとpathの両方が指定されている場合は起動失敗（AnnotationConfigurationException）になるので、単純に合わせる
            // org.springframework.core.annotation.AbstractAliasAwareAnnotationAttributeExtractor.getAttributeValue
            // 複数（ @RequestMapping({"a", "b"}) など）への対応は、そのうち。
            String classPath = entrypoint.jigType()
                    .annotationValueOf(TypeId.valueOf("org.springframework.web.bind.annotation.RequestMapping"), "value", "path")
                    .filter(value -> !"/".equals(value)).orElse("");

            var entrypointName = resolveEntrypointName(jigMethod);
            var requestMappingForMethod = entrypoint.httpMappingAnnotation();

            if (requestMappingForMethod != null) {
                var methodPath = resolveMethodPath(requestMappingForMethod);
                var httpMethod = resolveHttpMethod(requestMappingForMethod);
                return new HttpEntrypointPath(httpMethod, entrypointName, classPath, methodPath);
            }
            // RequestMappingでないものをEntrypointと認識しているのはおかしい。
            // Entrypointの時点で解決しているはずなので、ここで分岐があるのが設計ミス。
            logger.warn("{} のRequestMapping系アノテーションが検出されませんでした。JIGの不具合もしくは設定ミスです。", jigMethod.simpleText());
            return new HttpEntrypointPath("???", entrypointName, classPath, "");
        }

        private static String resolveHttpMethod(JigAnnotationReference requestMappingForMethod) {
            var simpleText = requestMappingForMethod.id().asSimpleText();
            // アノテーション名からHTTPメソッド名を解決する。
            // RequestMappingはmethod要素の指定次第となるが、解決するのも手間だし、RequestMappingなどよりGetMappingの使用が推奨なので扱わない。
            return "RequestMapping".equals(simpleText) ? "???" : simpleText.replace("Mapping", "").toUpperCase(Locale.ROOT);
        }

        private static String resolveEntrypointName(JigMethod jigMethod) {
            return jigMethod
                    // Swaggerのアノテーションのsummaryが記述されていればそれを採用
                    .declarationAnnotationStream()
                    .filter(methodAnnotation -> methodAnnotation.id().equals(TypeId.valueOf("io.swagger.v3.oas.annotations.Operation")))
                    .flatMap(methodAnnotation -> methodAnnotation.elementTextOf("summary").stream())
                    // アノテーションの仕様上、同じアノテーションが複数あることもあるし、要素の文字列も配列で定義可能なのでAnyで取得。実際は0..1になる。
                    .findAny()
                    // OpenAPIドキュメントの自動生成をしていないなど、解決できない場合は通常のメソッドラベル
                    .orElseGet(jigMethod::labelText);
        }

        private static String resolveMethodPath(JigAnnotationReference requestMappingForMethod) {
            return requestMappingForMethod.elementTextOf("value")
                    .or(() -> requestMappingForMethod.elementTextOf("path"))
                    .filter(Predicate.not(String::isEmpty))
                    .orElse("/");
        }

        public String pathText() {
            return Arrays.stream((classPath + '/' + methodPath).split("/", -1))
                    .filter(string -> !string.isEmpty())
                    .collect(joining("/", "/", ""));
        }
    }
}

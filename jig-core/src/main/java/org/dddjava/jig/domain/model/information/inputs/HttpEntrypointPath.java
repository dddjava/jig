package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

/**
 * HTTPリクエストのエンドポイント
 *
 * エントリーポイントから情報を抜き出したもの。
 */
public record HttpEntrypointPath(String method, String interfaceLabel, String classPath, String methodPath) {
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
        var optRequestMappingForMethod = findJigAnnotationReference(jigMethod);

        return optRequestMappingForMethod.map(requestMappingForMethod -> {
            var methodPath = resolveMethodPath(requestMappingForMethod);
            var httpMethod = resolveHttpMethod(requestMappingForMethod);
            return new HttpEntrypointPath(httpMethod, entrypointName, classPath, methodPath);
        }).orElseGet(() -> {
            // RequestMappingでないものをEntrypointと認識しているのはおかしい。
            // Entrypointの時点で解決しているはずなので、ここで分岐があるのが設計ミス。
            logger.warn("{} のRequestMapping系アノテーションが検出されませんでした。JIGの不具合もしくは設定ミスです。", jigMethod.simpleText());
            return new HttpEntrypointPath("???", entrypointName, classPath, "");
        });
    }

    private static Optional<JigAnnotationReference> findJigAnnotationReference(JigMethod jigMethod) {
        List<JigAnnotationReference> methodAnnotations = jigMethod.declarationAnnotationStream()
                .filter(jigAnnotationReference -> {
                    TypeId typeId = jigAnnotationReference.id();
                    return typeId.equals(TypeId.valueOf("org.springframework.web.bind.annotation.RequestMapping"))
                            || typeId.equals(TypeId.valueOf("org.springframework.web.bind.annotation.GetMapping"))
                            || typeId.equals(TypeId.valueOf("org.springframework.web.bind.annotation.PostMapping"))
                            || typeId.equals(TypeId.valueOf("org.springframework.web.bind.annotation.PutMapping"))
                            || typeId.equals(TypeId.valueOf("org.springframework.web.bind.annotation.DeleteMapping"))
                            || typeId.equals(TypeId.valueOf("org.springframework.web.bind.annotation.PatchMapping"));
                })
                .toList();
        if (methodAnnotations.isEmpty()) {
            return Optional.empty();
        }
        // メソッドにアノテーションが複数指定されている場合、最初の一つが優先される（SpringMVCの挙動）
        var requestMappingForMethod = methodAnnotations.get(0);
        if (methodAnnotations.size() > 1) {
            logger.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。出力には1件目を採用します。", jigMethod.simpleText());
        }
        return Optional.of(requestMappingForMethod);
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

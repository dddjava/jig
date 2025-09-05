package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
            logger.warn("{} のRequestMapping系アノテーションが検出されませんでした。JIGの不具合もしくは設定ミスです。", jigMethod.simpleText());
            return new HttpEntrypointPath("???", jigMethod.labelText(), classPath, "");
        }
        // メソッドにアノテーションが複数指定されている場合、最初の一つが優先される（SpringMVCの挙動）
        var requestMappingForMethod = methodAnnotations.get(0);
        if (methodAnnotations.size() > 1) {
            logger.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。出力には1件目を採用します。", jigMethod.simpleText());
        }
        var methodPath = resolveMethodPath(requestMappingForMethod);
        var simpleText = requestMappingForMethod.id().asSimpleText();
        // アノテーション名からHTTPメソッド名を作る。RequestMappingは一旦対応しない。
        var method = "RequestMapping".equals(simpleText) ? "???" : simpleText.replace("Mapping", "").toUpperCase(Locale.ROOT);

        // インタフェースラベルとしてはSwaggerアノテーションから概要を取得できる場合はそれを採用する。取得できない場合はJigMethodとしてのラベル。
        var optOperationSummary = jigMethod.declarationAnnotationStream()
                .filter(methodAnnotation -> methodAnnotation.id().equals(TypeId.valueOf("io.swagger.v3.oas.annotations.Operation")))
                .flatMap(methodAnnotation -> methodAnnotation.elementTextOf("summary").stream())
                .findAny();
        String interfaceLabel = optOperationSummary.orElseGet(jigMethod::labelText);

        return new HttpEntrypointPath(method, interfaceLabel, classPath, methodPath);
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

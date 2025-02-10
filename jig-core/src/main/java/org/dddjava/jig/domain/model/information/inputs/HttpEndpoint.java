package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public record HttpEndpoint(String method, String interfaceLabel, String classPath, String methodPath) {
    private static final Logger logger = LoggerFactory.getLogger(HttpEndpoint.class);

    public static HttpEndpoint from(EntrypointMethod entrypointMethod) {
        var jigMethod = entrypointMethod.jigMethod();

        // NOTE: valueとpathの両方が指定されている場合は起動失敗（AnnotationConfigurationException）になるので、単純に合わせる
        // org.springframework.core.annotation.AbstractAliasAwareAnnotationAttributeExtractor.getAttributeValue
        // 複数（ @RequestMapping({"a", "b"}) など）への対応は、そのうち。
        String classPath = entrypointMethod.jigType()
                .annotationValueOf(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"), "value", "path")
                .filter(value -> !"/".equals(value)).orElse("");

        String methodPath;
        List<JigAnnotationReference> methodAnnotations = jigMethod.declarationAnnotationStream()
                .filter(jigAnnotationReference -> {
                    TypeIdentifier typeIdentifier = jigAnnotationReference.id();
                    return typeIdentifier.equals(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"))
                            || typeIdentifier.equals(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.GetMapping"))
                            || typeIdentifier.equals(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PostMapping"))
                            || typeIdentifier.equals(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PutMapping"))
                            || typeIdentifier.equals(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.DeleteMapping"))
                            || typeIdentifier.equals(TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PatchMapping"));
                })
                .toList();
        if (methodAnnotations.isEmpty()) {
            logger.warn("{} のRequestMapping系アノテーションが検出されませんでした。JIGの不具合もしくは設定ミスです。", jigMethod.declaration().asFullNameText());
            return new HttpEndpoint("???", jigMethod.labelText(), classPath, "");
        }
        // メソッドにアノテーションが複数指定されている場合、最初の一つが優先される（SpringMVCの挙動）
        var requestMappingForMethod = methodAnnotations.get(0);
        if (methodAnnotations.size() > 1) {
            logger.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。出力には1件目を採用します。", jigMethod.declaration().asFullNameText());
        }
        methodPath = requestMappingForMethod.elementTextOf("value").orElse(null);
        if (methodPath == null) methodPath = requestMappingForMethod.elementTextOf("path").orElse(null);
        var simpleText = requestMappingForMethod.id().asSimpleText();
        // アノテーション名からHTTPメソッド名を作る。RequestMappingは一旦対応しない。
        var method = "RequestMapping".equals(simpleText) ? "???" : simpleText.replace("Mapping", "").toUpperCase(Locale.ROOT);

        // インタフェースラベルとしてはSwaggerアノテーションから概要を取得できる場合はそれを採用する。取得できない場合はJigMethodとしてのラベル。
        var optOperationSummary = jigMethod.declarationAnnotationStream()
                .filter(methodAnnotation -> methodAnnotation.id().equals(TypeIdentifier.valueOf("io.swagger.v3.oas.annotations.Operation")))
                .flatMap(methodAnnotation -> methodAnnotation.elementTextOf("summary").stream())
                .findAny();
        String interfaceLabel = optOperationSummary.orElseGet(jigMethod::labelText);

        if (methodPath == null || methodPath.isEmpty()) methodPath = "/";
        return new HttpEndpoint(method, interfaceLabel, classPath, methodPath);
    }

    public String pathText() {
        return Arrays.stream((classPath + '/' + methodPath).split("/", -1))
                .filter(string -> !string.isEmpty())
                .collect(Collectors.joining("/", "/", ""));
    }
}

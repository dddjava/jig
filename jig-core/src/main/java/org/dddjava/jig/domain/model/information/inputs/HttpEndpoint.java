package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
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
        List<Annotation> methodAnnotations = jigMethod.methodAnnotations().annotations().filterAny(
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.GetMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PostMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PutMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.DeleteMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PatchMapping")).list();
        if (methodAnnotations.isEmpty()) {
            logger.warn("{} のRequestMapping系アノテーションが検出されませんでした。JIGの不具合もしくは設定ミスです。", jigMethod.declaration().asFullNameText());
            return new HttpEndpoint("???", jigMethod.labelText(), classPath, "");
        }
        // メソッドにアノテーションが複数指定されている場合、最初の一つが優先される（SpringMVCの挙動）
        Annotation requestMappingForMethod = methodAnnotations.get(0);
        if (methodAnnotations.size() > 1) {
            logger.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。出力には1件目を採用します。", jigMethod.declaration().asFullNameText());
        }
        methodPath = requestMappingForMethod.descriptionTextOf("value");
        if (methodPath == null) methodPath = requestMappingForMethod.descriptionTextOf("path");
        var simpleText = requestMappingForMethod.typeIdentifier().asSimpleText();
        // アノテーション名からHTTPメソッド名を作る。RequestMappingは一旦対応しない。
        var method = "RequestMapping".equals(simpleText) ? "???" : simpleText.replace("Mapping", "").toUpperCase(Locale.ROOT);

        // インタフェースラベルとしてはSwaggerアノテーションから概要を取得できる場合はそれを採用する。取得できない場合はJigMethodとしてのラベル。
        var optOperationSummary = jigMethod.methodAnnotations().list().stream()
                .filter(methodAnnotation -> methodAnnotation.annotationType().anyEquals("io.swagger.v3.oas.annotations.Operation"))
                .flatMap(methodAnnotation -> methodAnnotation.annotation().descriptionTextAnyOf("summary").stream())
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

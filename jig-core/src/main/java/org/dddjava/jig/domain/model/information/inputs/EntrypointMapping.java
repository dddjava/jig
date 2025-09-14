package org.dddjava.jig.domain.model.information.inputs;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.types.JigType;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Predicate;

import static java.util.stream.Collectors.joining;

public interface EntrypointMapping {
    EntrypointMapping DEFAULT = new EntrypointMapping() {
    };

    default String shortPathText() {
        return fullPathText();
    }

    default String fullPathText() {
        // パスを実装していないEntrypointで呼び出されるパターン
        return "???";
    }
}

record HttpEntrypointMapping(String httpMethod, String classPath, String methodPath) implements EntrypointMapping {

    public static HttpEntrypointMapping from(JigType jigType, JigAnnotationReference methodMappingAnnotation) {

        // NOTE: valueとpathの両方が指定されている場合は起動失敗（AnnotationConfigurationException）になるので、単純に合わせる
        // org.springframework.core.annotation.AbstractAliasAwareAnnotationAttributeExtractor.getAttributeValue
        // 複数（ @RequestMapping({"a", "b"}) など）への対応は、そのうち。
        String classPath = jigType
                .annotationValueOf(TypeId.valueOf("org.springframework.web.bind.annotation.RequestMapping"), "value", "path")
                .filter(value -> !"/".equals(value)).orElse("");

        var methodPath = resolveMethodPath(methodMappingAnnotation);
        var httpMethod = resolveHttpMethod(methodMappingAnnotation);
        return new HttpEntrypointMapping(httpMethod, classPath, methodPath);
    }

    @Override
    public String shortPathText() {
        // クラスのパスが入らないパターン
        return "%s %s".formatted(httpMethod, methodPath);
    }

    @Override
    public String fullPathText() {
        // HTTPメソッドが入らないパターン
        return Arrays.stream((classPath + '/' + methodPath).split("/", -1))
                .filter(string -> !string.isEmpty())
                .collect(joining("/", "/", ""));
    }

    private static String resolveHttpMethod(JigAnnotationReference requestMappingForMethod) {
        var simpleText = requestMappingForMethod.id().asSimpleText();
        // アノテーション名からHTTPメソッド名を解決する。
        // RequestMappingはmethod要素の指定次第となるが、解決するのも手間だし、RequestMappingなどよりGetMappingの使用が推奨なので扱わない。
        return "RequestMapping".equals(simpleText) ? "???" : simpleText.replace("Mapping", "").toUpperCase(Locale.ROOT);
    }

    private static String resolveMethodPath(JigAnnotationReference requestMappingForMethod) {
        return requestMappingForMethod.elementTextOf("value")
                .or(() -> requestMappingForMethod.elementTextOf("path"))
                .filter(Predicate.not(String::isEmpty))
                .orElse("/");
    }
}

record QueueListenerEntrypointMapping(JigMethod jigMethod) implements EntrypointMapping {

    @Override
    public String fullPathText() {
        return jigMethod.declarationAnnotationStream()
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
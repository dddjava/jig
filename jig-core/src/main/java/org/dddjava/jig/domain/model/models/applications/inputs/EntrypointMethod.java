package org.dddjava.jig.domain.model.models.applications.inputs;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotations;
import org.dddjava.jig.domain.model.parts.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.UsingMethods;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * ハンドラ
 *
 * 外部からのリクエストを受け取る起点となるメソッドです。
 * 制限事項：RequestMappingをメタアノテーションとした独自アノテーションが付与されたメソッドは、ハンドラとして扱われません。
 */
public class EntrypointMethod {

    static Logger logger = LoggerFactory.getLogger(EntrypointMethod.class);

    final JigType jigType;
    final JigMethod method;
    final Annotations requestMappingForClass;
    final Annotations requestMappingsForMethod;

    public EntrypointMethod(JigType jigType, JigMethod method) {
        this.jigType = jigType;
        this.method = method;

        this.requestMappingForClass = jigType.annotationsOf(
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"));

        this.requestMappingsForMethod = method.methodAnnotations().annotations().filterAny(
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.RequestMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.GetMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PostMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PutMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.DeleteMapping"),
                TypeIdentifier.valueOf("org.springframework.web.bind.annotation.PatchMapping"));
    }

    public JigMethod method() {
        return method;
    }

    public String pathText() {
        // NOTE: valueとpathの両方が指定されている場合は起動失敗（AnnotationConfigurationException）になるので、単純に合わせる
        // org.springframework.core.annotation.AbstractAliasAwareAnnotationAttributeExtractor.getAttributeValue

        // 複数（ @RequestMapping({"a", "b"}) など）への対応は、そのうち。

        String typePath = null;
        List<Annotation> list = requestMappingForClass.list();
        if (!list.isEmpty()) {
            Annotation annotation = list.get(0);
            typePath = annotation.descriptionTextOf("value");
            if (typePath == null) typePath = annotation.descriptionTextOf("path");
        }
        if (typePath == null) typePath = "";

        List<Annotation> methodAnnotations = requestMappingsForMethod.list();
        if (methodAnnotations.isEmpty()) {
            return typePath;
        }

        // メソッドにアノテーションが複数指定されている場合、最初の一つが優先される（SpringMVCの挙動）
        Annotation requestMappingForMethod = methodAnnotations.get(0);
        if (methodAnnotations.size() > 1) {
            logger.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。", method.declaration().asFullNameText());
        }

        String methodPath = requestMappingForMethod.descriptionTextOf("value");
        if (methodPath == null) methodPath = requestMappingForMethod.descriptionTextOf("path");
        if (methodPath == null) methodPath = "";

        return combinePath(typePath, methodPath);
    }

    private String combinePath(String typePath, String methodPath) {
        String pathText;
        if (typePath.isEmpty()) {
            pathText = methodPath;
        } else if (methodPath.startsWith("/")) {
            pathText = typePath + methodPath;
        } else {
            pathText = typePath + "/" + methodPath;
        }
        return pathText;
    }

    public boolean isRequestHandler() {
        return method.methodAnnotations().list().stream()
                .anyMatch(annotatedMethod -> {
                    String annotationName = annotatedMethod.annotationType().fullQualifiedName();
                    // RequestMappingをメタアノテーションとして使うものにしたいが、spring-webに依存させたくないので列挙にする
                    // そのため独自アノテーションに対応できない
                    return annotationName.equals("org.springframework.web.bind.annotation.RequestMapping")
                            || annotationName.equals("org.springframework.web.bind.annotation.GetMapping")
                            || annotationName.equals("org.springframework.web.bind.annotation.PostMapping")
                            || annotationName.equals("org.springframework.web.bind.annotation.PutMapping")
                            || annotationName.equals("org.springframework.web.bind.annotation.DeleteMapping")
                            || annotationName.equals("org.springframework.web.bind.annotation.PatchMapping");
                });
    }

    public boolean isRabbitListener() {
        return method.methodAnnotations().list().stream()
                .anyMatch(annotatedMethod -> {
                    String annotationName = annotatedMethod.annotationType().fullQualifiedName();
                    return annotationName.equals("org.springframework.amqp.rabbit.annotation.RabbitListener");
                });
    }

    public boolean anyMatch(CallerMethods callerMethods) {
        return callerMethods.contains(method.declaration());
    }

    public boolean isCall(MethodDeclaration methodDeclaration) {
        return method.usingMethods().methodDeclarations().contains(methodDeclaration);
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public String typeLabel() {
        return jigType.label();
    }


    public static final String[] _RequestMapping = {
            "org.springframework.web.bind.annotation.RequestMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
    };

    public static final String[] _RabbitListener = {
            "org.springframework.amqp.rabbit.annotation.RabbitListener"
    };

    public String interfacePointDescription() {
        var methodAnnotations = method.methodAnnotations();

        var requestMappingPath = methodAnnotations.list().stream()
                .filter(methodAnnotation -> methodAnnotation.annotationType().anyEquals(_RequestMapping))
                .map(methodAnnotation -> {
                    var httpMethod = switch (methodAnnotation.annotationType().asSimpleText()) {
                        case "GetMapping" -> "GET";
                        case "PostMapping" -> "POST";
                        case "PutMapping" -> "PUT";
                        case "DeleteMapping" -> "DELETE";
                        case "PatchMapping" -> "PATCH";
                        default -> "???";
                    };
                    var pathDescription = methodAnnotation.annotation().descriptionTextAnyOf("value", "path")
                            // valueもpathがなかったり空文字であってもRequestMappingがあれば "/" にバインドされるので明示しておく
                            .filter(path -> !path.isEmpty()).orElse("/");

                    return "%s %s".formatted(httpMethod, pathDescription);
                })
                // アノテーションは複数取れないはずなのでこれで。
                .findFirst();

        Optional<String> rabbitListenerQueues = methodAnnotations.list().stream()
                .filter(methodAnnotation -> methodAnnotation.annotationType().anyEquals(_RabbitListener))
                .map(methodAnnotation -> {
                    // queue複数未対応
                    var queueName = methodAnnotation.annotation().descriptionTextAnyOf("queues");
                    return "queue: " + queueName.orElse("???");
                })
                // アノテーションは複数取れないはずなのでこれで。
                .findFirst();

        return requestMappingPath
                .or(() -> rabbitListenerQueues)
                .orElseGet(() -> {
                    // 想定するdescriptionがなかった場合。想定が増えたら追加する。
                    // api methodと判定されてるのにアノテーションがなかった場合もきちゃうけど、それは想定していない感じ
                    return "???";
                });
    }

    public String interfaceLabelText() {
        if (isRequestMappingMethod()) {
            var optOperationSummary = method.methodAnnotations().list().stream()
                    .filter(methodAnnotation -> methodAnnotation.annotationType().anyEquals("io.swagger.v3.oas.annotations.Operation"))
                    .flatMap(methodAnnotation -> methodAnnotation.annotation().descriptionTextAnyOf("summary").stream())
                    .findAny();

            return optOperationSummary.orElseGet(method::labelText);
        }

        return method.labelText();
    }

    private boolean isRequestMappingMethod() {
        return method.methodAnnotations().list().stream()
                .anyMatch(methodAnnotation -> methodAnnotation.annotationType().anyEquals(_RequestMapping));
    }

    public MethodDeclaration declaration() {
        return method.declaration();
    }

    public UsingMethods usingMethods() {
        return method.usingMethods();
    }
}

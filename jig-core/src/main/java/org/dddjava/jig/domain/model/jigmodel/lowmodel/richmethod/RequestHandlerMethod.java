package org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.CallerMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * リクエストハンドラ
 *
 * 制限事項：RequestMappingをメタアノテーションとした独自アノテーションが付与されたメソッドは、
 * リクエストハンドラとして扱われません。
 */
public class RequestHandlerMethod {

    static Logger LOGGER = LoggerFactory.getLogger(RequestHandlerMethod.class);

    private final Method method;
    private final Annotations requestMappingForClass;
    private final Annotations requestMappingsForMethod;

    public RequestHandlerMethod(Method method, TypeAnnotations typeAnnotations) {
        this.method = method;

        this.requestMappingForClass = typeAnnotations.annotations().filterAny(
                new TypeIdentifier("org.springframework.web.bind.annotation.RequestMapping"));

        this.requestMappingsForMethod = method.methodAnnotations().annotations().filterAny(
                new TypeIdentifier("org.springframework.web.bind.annotation.RequestMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.GetMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.PostMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.PutMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.DeleteMapping"),
                new TypeIdentifier("org.springframework.web.bind.annotation.PatchMapping"));
    }

    public Method method() {
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
            LOGGER.warn("{} にマッピングアノテーションが複数記述されているため、正しい検出が行えません。", method.declaration().asFullNameText());
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

    public boolean valid() {
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
                        }
                );
    }

    public boolean anyMatch(CallerMethods callerMethods) {
        return callerMethods.contains(method.declaration());
    }

    public boolean isCall(MethodDeclaration methodDeclaration) {
        return method.usingMethods().methodDeclarations().contains(methodDeclaration);
    }
}

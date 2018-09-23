package org.dddjava.jig.domain.model.controllers;

import org.dddjava.jig.domain.model.declaration.annotation.Annotation;
import org.dddjava.jig.domain.model.declaration.annotation.Annotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.Method;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * リクエストハンドラ
 */
public class RequestHandler {

    static Logger LOGGER = LoggerFactory.getLogger(RequestHandler.class);

    private final Method method;
    private final Annotations requestMappingForClass;
    private final Annotations requestMappingsForMethod;

    public RequestHandler(Method method, TypeAnnotations typeAnnotations) {
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
}

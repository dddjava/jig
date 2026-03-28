package org.dddjava.jig.domain.model.information.types;

import org.dddjava.jig.domain.model.data.types.TypeId;

/**
 * Springフレームワークのアノテーション識別子定数
 */
public class SpringAnnotations {
    private SpringAnnotations() {}

    // org.springframework.stereotype
    public static final TypeId SERVICE = TypeId.valueOf("org.springframework.stereotype.Service");
    public static final TypeId CONTROLLER = TypeId.valueOf("org.springframework.stereotype.Controller");
    public static final TypeId REPOSITORY = TypeId.valueOf("org.springframework.stereotype.Repository");
    public static final TypeId COMPONENT = TypeId.valueOf("org.springframework.stereotype.Component");

    // org.springframework.web.bind.annotation
    public static final TypeId REST_CONTROLLER = TypeId.valueOf("org.springframework.web.bind.annotation.RestController");
    public static final TypeId CONTROLLER_ADVICE = TypeId.valueOf("org.springframework.web.bind.annotation.ControllerAdvice");
    public static final TypeId REQUEST_MAPPING = TypeId.valueOf("org.springframework.web.bind.annotation.RequestMapping");
    public static final TypeId GET_MAPPING = TypeId.valueOf("org.springframework.web.bind.annotation.GetMapping");
    public static final TypeId POST_MAPPING = TypeId.valueOf("org.springframework.web.bind.annotation.PostMapping");
    public static final TypeId PUT_MAPPING = TypeId.valueOf("org.springframework.web.bind.annotation.PutMapping");
    public static final TypeId DELETE_MAPPING = TypeId.valueOf("org.springframework.web.bind.annotation.DeleteMapping");
    public static final TypeId PATCH_MAPPING = TypeId.valueOf("org.springframework.web.bind.annotation.PatchMapping");

    // org.springframework.amqp.rabbit.annotation
    public static final TypeId RABBIT_LISTENER = TypeId.valueOf("org.springframework.amqp.rabbit.annotation.RabbitListener");
}

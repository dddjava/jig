package org.springframework.web.bind.annotation;

public @interface RequestMapping {
    String[] value() default {};
}

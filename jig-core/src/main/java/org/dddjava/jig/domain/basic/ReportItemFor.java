package org.dddjava.jig.domain.basic;

import java.lang.annotation.*;

@Repeatable(ReportItemsFor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReportItemFor {

    ReportItem item();

    int order() default 0;

    String label() default "";
}

package org.dddjava.jig.infrastructure.view.report;

import java.lang.annotation.*;

@Repeatable(ReportItemsFor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReportItemFor {

    ReportItem value();

    int order() default 0;

    // TODO 無くしたい #309
    String label() default "";
}

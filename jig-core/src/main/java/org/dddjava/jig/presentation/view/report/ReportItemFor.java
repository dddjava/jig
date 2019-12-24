package org.dddjava.jig.presentation.view.report;

import java.lang.annotation.*;

@Repeatable(ReportItemsFor.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReportItemFor {

    ReportItem value();

    int order() default 0;

    // 廃止 #309
    @Deprecated
    String label() default "";
}

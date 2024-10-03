package org.dddjava.jig.infrastructure.view.report;

import org.dddjava.jig.domain.model.models.jigobject.member.MethodWorry;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ReportMethodWorryOf {
    MethodWorry value();
}

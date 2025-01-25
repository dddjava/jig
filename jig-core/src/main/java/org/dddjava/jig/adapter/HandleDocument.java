package org.dddjava.jig.adapter;

import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface HandleDocument {
    JigDocument[] value() default {};
}

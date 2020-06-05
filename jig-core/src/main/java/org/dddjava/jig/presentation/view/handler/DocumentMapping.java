package org.dddjava.jig.presentation.view.handler;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDocument;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DocumentMapping {
    JigDocument value();
}

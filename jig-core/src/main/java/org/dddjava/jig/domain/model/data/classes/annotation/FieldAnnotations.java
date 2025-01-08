package org.dddjava.jig.domain.model.data.classes.annotation;

import java.util.Collections;
import java.util.List;

/**
 * フィールドにつけられたアノテーション一覧
 */
public class FieldAnnotations {

    List<FieldAnnotation> list;

    public FieldAnnotations(List<FieldAnnotation> list) {
        this.list = list;
    }

    public static FieldAnnotations none() {
        return new FieldAnnotations(Collections.emptyList());
    }

    public List<FieldAnnotation> list() {
        return list;
    }
}

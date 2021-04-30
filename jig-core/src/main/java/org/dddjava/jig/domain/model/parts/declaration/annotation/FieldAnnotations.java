package org.dddjava.jig.domain.model.parts.declaration.annotation;

import java.util.List;

/**
 * フィールドにつけられたアノテーション一覧
 */
public class FieldAnnotations {

    List<FieldAnnotation> list;

    public FieldAnnotations(List<FieldAnnotation> list) {
        this.list = list;
    }

    public List<FieldAnnotation> list() {
        return list;
    }
}

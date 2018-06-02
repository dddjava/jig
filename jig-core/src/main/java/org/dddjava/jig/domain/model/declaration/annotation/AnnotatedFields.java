package org.dddjava.jig.domain.model.declaration.annotation;

import java.util.List;

/**
 * アノテーションのついたフィールド一覧
 */
public class AnnotatedFields {

    List<AnnotatedField> list;

    public AnnotatedFields(List<AnnotatedField> list) {
        this.list = list;
    }

    public List<AnnotatedField> list() {
        return list;
    }
}

package org.dddjava.jig.domain.model.declaration.annotation;

import java.util.List;

/**
 * アノテーションのついたメソッド一覧
 */
public class AnnotatedMethods {

    List<AnnotatedMethod> list;

    public AnnotatedMethods(List<AnnotatedMethod> list) {
        this.list = list;
    }

    public List<AnnotatedMethod> list() {
        return list;
    }
}

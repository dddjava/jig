package org.dddjava.jig.domain.model.declaration.annotation;

import java.util.ArrayList;
import java.util.List;

/**
 * アノテーションの記述
 */
public class AnnotationDescription {

    final List<String> list = new ArrayList<>();

    public String asText() {
        return list.toString();
    }

    public void addAnnotation(String name, String descriptor) {
        list.add(name + " = " + descriptor + "[...]");
    }

    public void addArray(String name, List<Object> list) {
        this.list.add(name + " = " + list.toString());
    }

    public void addEnum(String name, String value) {
        list.add(name + " = " + value);
    }

    public void addParam(String name, Object value) {
        list.add(name + " = " + value);
    }
}

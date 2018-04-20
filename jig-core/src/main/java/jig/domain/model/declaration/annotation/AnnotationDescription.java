package jig.domain.model.declaration.annotation;

import java.util.ArrayList;
import java.util.List;

public class AnnotationDescription {

    final List<String> list = new ArrayList<>();

    public String asText() {
        return list.toString();
    }

    public void addAnnotation(String name, String descriptor) {
        list.add(name + " = " + descriptor + "[...]");
    }

    public void addArray(String name) {
        list.add(name + " = [...]");
    }

    public void addEnum(String name, String value) {
        list.add(name + " = " + value);
    }

    public void addParam(String name, Object value) {
        if (value instanceof String) {
            list.add(name + " = \"" + value + "\"");
        } else {
            list.add(name + " = " + value);
        }
    }
}

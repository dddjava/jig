package org.dddjava.jig.domain.model.models.jigobject.components;

public enum ComponentType {
    SPRING_CONTROLLER("Controller"),
    SPRING_SERVICE("Service"),
    SPRING_REPOSITORY("Repository"),
    UNNAMED,
    IMPLEMENTED,

    // 非Component
    EXCLUDE;

    final String label;

    ComponentType(String label) {
        this.label = label;
    }

    ComponentType() {
        this("");
    }

    String label() {
        return label;
    }

    public boolean unnamed() {
        return label.isEmpty();
    }
}

package org.dddjava.jig.domain.model.models.jigobject.components;

import java.util.Objects;

public class ComponentRelation {
    String fromComponentName;
    String toComponentName;

    ComponentRelation(String fromComponentName, String toComponentName) {
        this.fromComponentName = fromComponentName;
        this.toComponentName = toComponentName;
    }

    ComponentRelation(SpecifiedComponentRelation specifiedComponentRelation) {
        this(specifiedComponentRelation.fromComponentName(), specifiedComponentRelation.toComponentName());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentRelation that = (ComponentRelation) o;
        return Objects.equals(fromComponentName, that.fromComponentName) && Objects.equals(toComponentName, that.toComponentName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fromComponentName, toComponentName);
    }

    public String edgeTextWithNumber(int count) {
        if (count == 1) {
            return String.format("\"%s\" -> \"%s\"", fromComponentName, toComponentName);
        }
        return String.format("\"%s\" -> \"%s\"[label=\"%d\"]",
                fromComponentName, toComponentName, count);
    }

    public boolean selfRelation() {
        return fromComponentName.equals(toComponentName);
    }
}

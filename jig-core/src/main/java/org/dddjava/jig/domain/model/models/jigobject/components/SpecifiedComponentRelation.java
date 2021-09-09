package org.dddjava.jig.domain.model.models.jigobject.components;

public class SpecifiedComponentRelation {
    SpecifiedComponent from;
    SpecifiedComponent to;

    SpecifiedComponentRelation(SpecifiedComponent from, SpecifiedComponent to) {
        this.from = from;
        this.to = to;
    }

    public String fromComponentName() {
        return from.componentIdentifier();
    }

    public String toComponentName() {
        return to.componentIdentifier();
    }
}

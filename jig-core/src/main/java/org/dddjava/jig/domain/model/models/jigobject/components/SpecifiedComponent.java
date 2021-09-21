package org.dddjava.jig.domain.model.models.jigobject.components;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

public class SpecifiedComponent {

    ComponentType componentType;
    JigType jigType;

    SpecifiedComponent(ComponentType componentType, JigType jigType) {
        this.componentType = componentType;
        this.jigType = jigType;
    }

    public SpecifiedComponent with(ComponentType componentType) {
        return new SpecifiedComponent(componentType, this.jigType);
    }

    public TypeIdentifier identifier() {
        return jigType.identifier();
    }

    public ComponentType componentType() {
        return componentType;
    }

    public String componentIdentifier() {
        if (componentType.unnamed()) return jigType.identifier().fullQualifiedName();
        return componentType.name();
    }

    public boolean implementing(TypeIdentifier identifier) {
        return jigType.implementing(identifier);
    }

    public boolean implementer() {
        return jigType.hasSuperOrInterface();
    }

    String nodeText() {
        if (componentType == ComponentType.UNNAMED) {
            return String.format("\"%s\"[label=\"%s\"];", componentIdentifier(), jigType.simpleName());
        } else if (componentType == ComponentType.IMPLEMENTED) {
            return String.format("\"%s\"[label=\"%s\" shape=ellipse fontsize=9];", componentIdentifier(), jigType.simpleName());
        } else {
            return String.format("%s[shape=component fillcolor=lightgoldenrod" +
                            " label=\"%s\"];",
                    componentIdentifier(), componentType.label());
        }
    }
}

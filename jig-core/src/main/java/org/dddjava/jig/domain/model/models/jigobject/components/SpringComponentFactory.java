package org.dddjava.jig.domain.model.models.jigobject.components;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

import java.util.HashMap;
import java.util.Map;

/**
 * SpringFramework用ComponentFactory
 */
public class SpringComponentFactory {

    Map<TypeIdentifier, ComponentType> map;

    SpringComponentFactory() {
        this.map = new HashMap<>();
        // Controller
        map.put(new TypeIdentifier("org.springframework.stereotype.Controller"), ComponentType.SPRING_CONTROLLER);
        map.put(new TypeIdentifier("org.springframework.web.bind.annotation.RestController"), ComponentType.SPRING_CONTROLLER);
        map.put(new TypeIdentifier("org.springframework.web.bind.annotation.ControllerAdvice"), ComponentType.SPRING_CONTROLLER);
        map.put(new TypeIdentifier("org.springframework.web.bind.annotation.RestControllerAdvice"), ComponentType.SPRING_CONTROLLER);
        // Service
        map.put(new TypeIdentifier("org.springframework.stereotype.Service"), ComponentType.SPRING_SERVICE);
        // Repository
        map.put(new TypeIdentifier("org.springframework.stereotype.Repository"), ComponentType.SPRING_REPOSITORY);
        // Component
        map.put(new TypeIdentifier("org.springframework.stereotype.Component"), ComponentType.UNNAMED);
    }

    SpecifiedComponent create(JigType jigType) {
        ComponentType componentType = resolveComponentType(jigType);
        return new SpecifiedComponent(componentType, jigType);
    }

    private ComponentType resolveComponentType(JigType jigType) {
        for (Map.Entry<TypeIdentifier, ComponentType> entry : map.entrySet()) {
            if (jigType.hasAnnotation(entry.getKey())) {
                return entry.getValue();
            }
        }
        return ComponentType.EXCLUDE;
    }
}

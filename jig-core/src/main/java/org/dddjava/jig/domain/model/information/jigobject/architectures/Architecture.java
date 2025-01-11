package org.dddjava.jig.domain.model.information.jigobject.architectures;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;

/**
 * アーキテクチャ
 */
public interface Architecture {

    default boolean isService(JigType jigType) {
        return jigType.hasAnnotation(TypeIdentifier.valueOf("org.springframework.stereotype.Service"));
    }

    boolean isDomainCore(JigType jigType);
}

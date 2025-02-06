package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.classes.method.Visibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;

import java.util.Collection;
import java.util.List;

public record JigMethodAttribute(
        Visibility visibility,
        Collection<JigAnnotationReference> declarationAnnotationInstances,
        JigTypeReference jigReturnType,
        List<JigTypeReference> argumentList,
        Collection<JigTypeReference> throwTypes
) {
}

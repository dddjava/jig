package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.classes.method.Visibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;

import java.util.Collection;
import java.util.List;

public record JigFieldAttribute(Visibility visibility,
                                Collection<JigAnnotationReference> declarationAnnotations,
                                JigTypeReference typeReference,
                                List<JigTypeReference> argumentList) {
}

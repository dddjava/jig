package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;

import java.util.Collection;
import java.util.EnumSet;

public record JigFieldAttribute(JigMemberVisibility jigMemberVisibility,
                                Collection<JigAnnotationReference> declarationAnnotations,
                                EnumSet<JigFieldFlag> flags,
                                JigTypeReference typeReference) {
}

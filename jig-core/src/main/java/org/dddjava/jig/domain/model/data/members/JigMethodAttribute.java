package org.dddjava.jig.domain.model.data.members;

import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;

public record JigMethodAttribute(JigMemberVisibility jigMemberVisibility,
                                 Collection<JigAnnotationReference> declarationAnnotations,
                                 JigTypeReference returnType,
                                 List<JigTypeReference> argumentList,
                                 Collection<JigTypeReference> throwTypes,
                                 EnumSet<JigMethodFlag> flags) {
    public boolean isAbstract() {
        return flags.contains(JigMethodFlag.ABSTRACT);
    }
}

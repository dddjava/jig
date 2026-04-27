package org.dddjava.jig.domain.model.data.members.fields;

import org.dddjava.jig.domain.model.data.members.JigMemberVisibility;
import org.dddjava.jig.domain.model.data.types.JigAnnotationReference;

import java.util.Collection;
import java.util.EnumSet;

/**
 * フィールドの属性
 */
record JigFieldAttribute(JigMemberVisibility jigMemberVisibility,
                         Collection<JigAnnotationReference> declarationAnnotations,
                         EnumSet<JigFieldFlag> flags) {
    public boolean isDeprecated() {
        return declarationAnnotations.stream().anyMatch(JigAnnotationReference::isDeprecated);
    }
}

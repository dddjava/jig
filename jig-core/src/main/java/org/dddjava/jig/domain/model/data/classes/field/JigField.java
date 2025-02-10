package org.dddjava.jig.domain.model.data.classes.field;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

public class JigField {
    private final JigFieldHeader jigFieldHeader;

    public JigField(JigFieldHeader jigFieldHeader) {
        this.jigFieldHeader = jigFieldHeader;
    }

    public JigTypeReference jigTypeReference() {
        return jigFieldHeader.jigTypeReference();
    }

    static ParameterizedType jigReferenceToParameterizedType(JigTypeReference jigTypeReference) {
        return new ParameterizedType(jigTypeReference.id(),
                jigTypeReference.typeArgumentList().stream()
                        .map(jigTypeArgument -> jigReferenceToParameterizedType(jigTypeArgument.jigTypeReference()))
                        .toList());
    }

    public static JigField from(JigFieldHeader jigFieldHeader) {
        return new JigField(jigFieldHeader);
    }

    public TypeIdentifier typeIdentifier() {
        return jigFieldHeader.jigTypeReference().id();
    }

    public String nameText() {
        return jigFieldHeader.name();
    }

    public boolean isDeprecated() {
        return jigFieldHeader.isDeprecated();
    }
}

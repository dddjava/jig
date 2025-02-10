package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.JigFieldHeader;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

public class JigField {
    private final JigFieldHeader jigFieldHeader;

    public JigField(JigFieldHeader jigFieldHeader) {
        this.jigFieldHeader = jigFieldHeader;
    }

    public static JigField from(JigFieldHeader jigFieldHeader) {
        return new JigField(jigFieldHeader);
    }

    public JigTypeReference jigTypeReference() {
        return jigFieldHeader.jigTypeReference();
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

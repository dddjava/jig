package org.dddjava.jig.domain.model.information.members;

import org.dddjava.jig.domain.model.data.members.fields.JigFieldHeader;
import org.dddjava.jig.domain.model.data.terms.Term;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * フィールド　
 */
public record JigField(JigFieldHeader jigFieldHeader, Term term) {

    public static JigField from(JigFieldHeader jigFieldHeader, Term term) {
        return new JigField(jigFieldHeader, term);
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

    public String simpleNameWithGenerics() {
        return jigFieldHeader.simpleNameWithGenerics();
    }
}

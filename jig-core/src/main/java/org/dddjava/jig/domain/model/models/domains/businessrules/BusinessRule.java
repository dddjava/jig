package org.dddjava.jig.domain.model.models.domains.businessrules;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

/**
 * ビジネスルール
 */
public class BusinessRule extends JigType {

    public BusinessRule(JigType jigType) {
        super(jigType);
    }

    public TypeIdentifier typeIdentifier() {
        return super.identifier();
    }

    public String nodeLabel() {
        return typeAlias().nodeLabel();
    }

    public JigType jigType() {
        return this;
    }
}

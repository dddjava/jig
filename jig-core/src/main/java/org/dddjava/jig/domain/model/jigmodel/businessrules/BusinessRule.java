package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

/**
 * ビジネスルール
 */
public class BusinessRule {

    JigType jigType;

    public BusinessRule(JigType jigType) {
        this.jigType = jigType;
    }

    public TypeIdentifier typeIdentifier() {
        return jigType.identifier();
    }

    public JigTypeValueKind toValueKind() {
        return jigType.toValueKind();
    }

    public MethodDeclarations instanceMethodDeclarations() {
        return jigType.instanceMember().instanceMethods().declarations();
    }

    public String nodeLabel() {
        return jigType.typeAlias().nodeLabel();
    }

    public boolean markedCore() {
        return jigType.typeAlias().markedCore();
    }

    public JigType jigType() {
        return jigType;
    }
}

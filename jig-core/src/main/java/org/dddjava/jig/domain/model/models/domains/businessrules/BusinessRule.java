package org.dddjava.jig.domain.model.models.domains.businessrules;

import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypeValueKind;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;

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

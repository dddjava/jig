package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigInstanceMember;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigStaticMember;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * ビジネスルール
 */
public class BusinessRule {

    JigType jigType;

    public BusinessRule(JigType jigType) {
        this.jigType = jigType;
    }

    public TypeDeclaration typeDeclaration() {
        return jigType.typeDeclaration();
    }

    private JigInstanceMember instanceMember() {
        return jigType.instanceMember();
    }

    public BusinessRuleFields fields() {
        return new BusinessRuleFields(instanceMember().fieldDeclarations());
    }

    public TypeIdentifier typeIdentifier() {
        return typeDeclaration().identifier();
    }

    public BusinessRuleCategory businessRuleCategory() {
        return BusinessRuleCategory.choice(fields(), jigType.typeKind());
    }

    public Visibility visibility() {
        return jigType.visibility();
    }

    public MethodDeclarations instanceMethodDeclarations() {
        return instanceMember().instanceMethods().declarations();
    }

    public String nodeLabel() {
        return jigType.typeAlias().nodeLabel();
    }

    public boolean markedCore() {
        return jigType.typeAlias().markedCore();
    }

    public JigMethods instanceMethods() {
        return instanceMember().instanceMethods();
    }

    public JigInstanceMember jigInstanceMember() {
        return instanceMember();
    }

    public JigStaticMember jigStaticMember() {
        return jigType.staticMember();
    }

    public JigType jigType() {
        return jigType;
    }
}

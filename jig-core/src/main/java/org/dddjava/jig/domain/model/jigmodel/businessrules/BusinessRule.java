package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.jigtype.JigInstanceMember;
import org.dddjava.jig.domain.model.jigmodel.jigtype.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.JigTypeMember;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Methods;

/**
 * ビジネスルール
 */
public class BusinessRule {

    JigType jigType;
    JigTypeMember jigTypeMember;
    JigInstanceMember jigInstanceMember;

    public BusinessRule(JigType jigType, JigInstanceMember jigInstanceMember, JigTypeMember jigTypeMember) {
        this.jigType = jigType;
        this.jigInstanceMember = jigInstanceMember;
        this.jigTypeMember = jigTypeMember;
    }

    public TypeDeclaration typeDeclaration() {
        return jigType.typeDeclaration();
    }

    public BusinessRuleFields fields() {
        return new BusinessRuleFields(jigInstanceMember.fieldDeclarations());
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
        return jigInstanceMember.instanceMethods().declarations();
    }

    public String nodeLabel() {
        return jigType.typeAlias().nodeLabel();
    }

    public boolean markedCore() {
        return jigType.typeAlias().markedCore();
    }

    public Methods instanceMethods() {
        return jigInstanceMember.instanceMethods();
    }

    public JigInstanceMember jigInstanceMember() {
        return jigInstanceMember;
    }

    public JigTypeMember jigTypeMember() {
        return jigTypeMember;
    }

    public JigType jigType() {
        return jigType;
    }
}

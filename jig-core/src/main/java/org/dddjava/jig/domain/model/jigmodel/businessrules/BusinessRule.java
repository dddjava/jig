package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.jigtype.JigInstanceMember;
import org.dddjava.jig.domain.model.jigmodel.jigtype.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.JigTypeMember;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

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

    public TypeDeclaration type() {
        return jigType.getTypeDeclaration();
    }

    public BusinessRuleFields fields() {
        return new BusinessRuleFields(jigInstanceMember.getFieldDeclarations());
    }

    public TypeIdentifier typeIdentifier() {
        return type().identifier();
    }

    public boolean hasInstanceMethod() {
        return !jigInstanceMember.getInstanceMethodDeclarations().empty();
    }

    public TypeKind typeKind() {
        return jigType.getTypeKind();
    }

    public BusinessRuleCategory businessRuleCategory() {
        return BusinessRuleCategory.choice(fields(), typeKind());
    }

    public Visibility visibility() {
        return jigType.getVisibility();
    }

    public String fullName() {
        return typeIdentifier().fullQualifiedName();
    }

    public MethodDeclarations instanceMethodDeclarations() {
        return jigInstanceMember.getInstanceMethodDeclarations();
    }

    public String nodeLabel() {
        return jigType.getTypeAlias().nodeLabel();
    }

    public boolean markedCore() {
        return jigType.getTypeAlias().markedCore();
    }
}

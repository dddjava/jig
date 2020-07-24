package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * ビジネスルール
 */
public class BusinessRule {

    TypeAlias typeAlias;
    TypeDeclaration typeDeclaration;
    TypeKind typeKind;
    Visibility visibility;

    FieldDeclarations fieldDeclarations;

    MethodDeclarations constructorDeclarations;
    MethodDeclarations instanceMethodDeclarations;
    MethodDeclarations staticMethodDeclarations;

    public BusinessRule(
            JigType jigType,
            JigInstanceMember jigInstanceMember,
            JigTypeMember jigTypeMember) {
        this.typeAlias = jigType.getTypeAlias();
        this.typeKind = jigType.getTypeKind();
        this.fieldDeclarations = jigInstanceMember.getFieldDeclarations();
        this.typeDeclaration = jigType.getTypeDeclaration();
        this.constructorDeclarations = jigTypeMember.getConstructorDeclarations();
        this.instanceMethodDeclarations = jigInstanceMember.getInstanceMethodDeclarations();
        this.staticMethodDeclarations = jigTypeMember.getStaticMethodDeclarations();
        this.visibility = jigType.getVisibility();
    }

    public TypeDeclaration type() {
        return typeDeclaration;
    }

    public BusinessRuleFields fields() {
        return new BusinessRuleFields(fieldDeclarations);
    }

    public TypeIdentifier typeIdentifier() {
        return type().identifier();
    }

    public boolean hasInstanceMethod() {
        return !instanceMethodDeclarations.empty();
    }

    public TypeKind typeKind() {
        return typeKind;
    }

    public BusinessRuleCategory businessRuleCategory() {
        return BusinessRuleCategory.choice(fields(), typeKind);
    }

    public Visibility visibility() {
        return visibility;
    }

    public String fullName() {
        return typeIdentifier().fullQualifiedName();
    }

    public MethodDeclarations instanceMethodDeclarations() {
        return instanceMethodDeclarations;
    }

    public String nodeLabel() {
        return typeAlias.nodeLabel();
    }

    public boolean markedCore() {
        return typeAlias.markedCore();
    }
}

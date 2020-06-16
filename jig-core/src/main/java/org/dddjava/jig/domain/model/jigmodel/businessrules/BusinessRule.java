package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;

/**
 * ビジネスルール
 */
public class BusinessRule {

    TypeKind typeKind;
    TypeDeclaration typeDeclaration;

    FieldDeclarations fieldDeclarations;
    MethodDeclarations methodDeclarations;

    boolean hasInstanceMethod;
    TypeIdentifiers userTypes;

    public BusinessRule(TypeKind typeKind, FieldDeclarations fieldDeclarations, TypeDeclaration typeDeclaration, MethodDeclarations methodDeclarations, boolean hasInstanceMethod, TypeIdentifiers userTypes) {
        this.typeKind = typeKind;
        this.fieldDeclarations = fieldDeclarations;
        this.typeDeclaration = typeDeclaration;
        this.methodDeclarations = methodDeclarations;
        this.hasInstanceMethod = hasInstanceMethod;
        this.userTypes = userTypes;
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

    public MethodDeclarations methodDeclarations() {
        return methodDeclarations;
    }

    public boolean hasInstanceMethod() {
        return hasInstanceMethod;
    }

    public TypeKind typeKind() {
        return typeKind;
    }

    public BusinessRuleCategory businessRuleCategory() {
        return BusinessRuleCategory.choice(fields(), typeKind);
    }

    public TypeIdentifiers userTypes() {
        return userTypes;
    }
}

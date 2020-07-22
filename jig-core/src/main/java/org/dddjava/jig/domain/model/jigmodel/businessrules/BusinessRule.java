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
            // TODO オブジェクト化して引数を減らす
            // 型自身のもの
            TypeDeclaration typeDeclaration,
            TypeAlias typeAlias,
            TypeKind typeKind,
            Visibility visibility,
            // メンバに関するもの
            FieldDeclarations fieldDeclarations,
            MethodDeclarations constructorDeclarations,
            MethodDeclarations instanceMethodDeclarations,
            MethodDeclarations staticMethodDeclarations) {
        this.typeAlias = typeAlias;
        this.typeKind = typeKind;
        this.fieldDeclarations = fieldDeclarations;
        this.typeDeclaration = typeDeclaration;
        this.constructorDeclarations = constructorDeclarations;
        this.instanceMethodDeclarations = instanceMethodDeclarations;
        this.staticMethodDeclarations = staticMethodDeclarations;
        this.visibility = visibility;
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
}

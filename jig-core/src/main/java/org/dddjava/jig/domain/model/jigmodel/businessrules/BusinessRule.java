package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * ビジネスルール
 */
public class BusinessRule {

    BusinessRuleFields businessRuleFields;
    private TypeIdentifier typeIdentifier;
    private boolean anEnum;
    private TypeDeclaration typeDeclaration;
    private MethodDeclarations methodDeclarations;
    private CategoryType categoryType;

    public BusinessRule(BusinessRuleFields businessRuleFields, TypeIdentifier typeIdentifier, boolean anEnum, TypeDeclaration typeDeclaration, MethodDeclarations methodDeclarations, CategoryType categoryType) {
        this.businessRuleFields = businessRuleFields;
        this.typeIdentifier = typeIdentifier;
        this.anEnum = anEnum;
        this.typeDeclaration = typeDeclaration;
        this.methodDeclarations = methodDeclarations;
        this.categoryType = categoryType;
    }

    public TypeDeclaration type() {
        return typeDeclaration;
    }

    boolean satisfyCollection() {
        return businessRuleFields.satisfyCollection();
    }

    boolean satisfyValue(ValueKind valueKind) {
        return businessRuleFields.satisfyValue(valueKind);
    }

    boolean satisfyCategory() {
        return anEnum;
    }

    public BusinessRuleFields fields() {
        return businessRuleFields;
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public MethodDeclarations methodDeclarations() {
        return methodDeclarations;
    }

    public CategoryType categoryType() {
        return categoryType;
    }
}

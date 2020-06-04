package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

/**
 * ビジネスルール
 */
public class BusinessRule {

    BusinessRuleCategory businessRuleCategory;
    TypeDeclaration typeDeclaration;

    BusinessRuleFields businessRuleFields;
    TypeIdentifier typeIdentifier;
    MethodDeclarations methodDeclarations;

    CategoryType categoryType;

    public BusinessRule(BusinessRuleFields businessRuleFields, TypeIdentifier typeIdentifier, boolean isEnum, TypeDeclaration typeDeclaration, MethodDeclarations methodDeclarations, CategoryType categoryType) {
        this.businessRuleFields = businessRuleFields;
        this.typeIdentifier = typeIdentifier;
        this.typeDeclaration = typeDeclaration;
        this.methodDeclarations = methodDeclarations;
        this.categoryType = categoryType;

        this.businessRuleCategory = BusinessRuleCategory.choice(businessRuleFields, isEnum);
    }

    public TypeDeclaration type() {
        return typeDeclaration;
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

    public BusinessRuleCategory businessRuleCategory() {
        return businessRuleCategory;
    }
}

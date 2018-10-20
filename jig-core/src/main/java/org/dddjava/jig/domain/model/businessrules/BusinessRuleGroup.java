package org.dddjava.jig.domain.model.businessrules;

import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;

/**
 * ビジネスルールのグループ（パッケージ）
 */
public class BusinessRuleGroup {
    PackageIdentifier packageIdentifier;
    BusinessRules businessRules;

    public BusinessRuleGroup(PackageIdentifier packageIdentifier, BusinessRules businessRules) {
        this.packageIdentifier = packageIdentifier;
        this.businessRules = businessRules;
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public BusinessRules businessRules() {
        return businessRules;
    }
}

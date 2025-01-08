package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

/**
 * ビジネスルールのグループ（パッケージ）
 */
public class BusinessRulePackage {
    PackageIdentifier packageIdentifier;
    BusinessRules businessRules;

    public BusinessRulePackage(PackageIdentifier packageIdentifier, BusinessRules businessRules) {
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

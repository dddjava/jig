package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;

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

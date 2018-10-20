package org.dddjava.jig.domain.model.networks.businessrule;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;

import java.util.List;

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

    public List<BusinessRule> listBusinessRule() {
        return businessRules.list();
    }
}

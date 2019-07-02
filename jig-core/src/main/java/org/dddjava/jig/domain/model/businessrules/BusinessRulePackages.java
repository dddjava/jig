package org.dddjava.jig.domain.model.businessrules;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BusinessRulePackages {
    List<BusinessRulePackage> list;

    BusinessRulePackages(List<BusinessRulePackage> list) {
        this.list = list;
    }

    public List<BusinessRulePackage> list() {
        return list.stream()
                .sorted(Comparator.comparing(businessRulePackage -> businessRulePackage.packageIdentifier.asText()))
                .collect(Collectors.toList());
    }
}

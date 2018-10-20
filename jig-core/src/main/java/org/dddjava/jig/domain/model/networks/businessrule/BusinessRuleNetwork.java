package org.dddjava.jig.domain.model.networks.businessrule;

import org.dddjava.jig.domain.model.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.businessrules.BusinessRuleGroup;
import org.dddjava.jig.domain.model.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.networks.type.TypeDependencies;
import org.dddjava.jig.domain.model.networks.type.TypeDependency;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ビジネスルールの関連網
 */
public class BusinessRuleNetwork {

    BusinessRules businessRules;
    TypeDependencies typeDependencies;

    public BusinessRuleNetwork(BusinessRules businessRules, TypeDependencies typeDependencies) {
        this.businessRules = businessRules;
        this.typeDependencies = typeDependencies;
    }

    public List<BusinessRuleGroup> groups() {
        Map<PackageIdentifier, List<BusinessRule>> collect = businessRules.list().stream()
                .collect(Collectors.groupingBy(
                        businessRule -> businessRule.type().identifier().packageIdentifier()
                ));
        return collect.entrySet().stream()
                .map(entity -> new BusinessRuleGroup(
                        entity.getKey(),
                        new BusinessRules(entity.getValue())
                )).collect(Collectors.toList());
    }

    public BusinessRuleRelations relations() {
        List<BusinessRuleRelation> list = new ArrayList<>();
        for (TypeDependency typeDependency : typeDependencies.list()) {
            if (typeDependency.notSelfDependency()
                    && businessRules.contains(typeDependency.from())
                    && businessRules.contains(typeDependency.to())) {
                list.add(new BusinessRuleRelation(typeDependency));
            }
        }
        return new BusinessRuleRelations(list);
    }
}

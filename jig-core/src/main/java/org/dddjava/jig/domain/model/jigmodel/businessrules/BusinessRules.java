package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.Type;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.declaration.type.TypeIdentifiers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    List<BusinessRule> list;

    public BusinessRules(List<BusinessRule> list) {
        this.list = list;
    }

    public List<BusinessRule> list() {
        return list.stream()
                .sorted(Comparator.comparing(BusinessRule::typeIdentifier))
                .collect(Collectors.toList());
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        for (BusinessRule businessRule : list) {
            if (businessRule.type().identifier().equals(typeIdentifier)) {
                return true;
            }
        }
        return false;
    }

    public boolean empty() {
        return list.isEmpty();
    }

    public TypeIdentifiers identifiers() {
        return list.stream()
                .map(BusinessRule::type)
                .map(Type::identifier)
                .collect(TypeIdentifiers.collector());
    }

    public BusinessRulePackages businessRulePackages() {
        Map<PackageIdentifier, List<BusinessRule>> map = list().stream()
                .collect(Collectors.groupingBy(
                        businessRule -> businessRule.type().identifier().packageIdentifier()
                ));
        List<BusinessRulePackage> list = map.entrySet().stream()
                .map(entity -> new BusinessRulePackage(
                        entity.getKey(),
                        new BusinessRules(entity.getValue())
                )).collect(Collectors.toList());
        return new BusinessRulePackages(list);
    }

    public List<BusinessRule> listCollection() {
        return list.stream()
                .filter(BusinessRule::satisfyCollection)
                .collect(Collectors.toList());
    }

    public List<BusinessRule> listValue(ValueKind valueKind) {
        return list.stream()
                .filter(businessRule -> !businessRule.satisfyCategory() && businessRule.satisfyValue(valueKind))
                .collect(Collectors.toList());
    }

    public List<BusinessRule> listCategory() {
        return list.stream()
                .filter(businessRule -> businessRule.satisfyCategory())
                .collect(Collectors.toList());
    }
}

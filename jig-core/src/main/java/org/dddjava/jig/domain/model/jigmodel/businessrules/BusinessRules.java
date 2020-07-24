package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;

import java.util.*;
import java.util.stream.Collectors;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    List<BusinessRule> list;
    ClassRelations businessRuleRelations;
    ClassRelations classRelations;

    public BusinessRules(List<BusinessRule> list, ClassRelations classRelations) {
        this.list = list;
        this.classRelations = classRelations;

        Set<TypeIdentifier> businessRuleTypeSet = list.stream()
                .map(businessRule -> businessRule.typeIdentifier())
                .collect(Collectors.toSet());
        List<ClassRelation> businessRuleRelationList = new ArrayList<>();
        for (ClassRelation classRelation : classRelations.distinctList()) {
            if (businessRuleTypeSet.contains(classRelation.from())
                    && businessRuleTypeSet.contains(classRelation.to())) {
                businessRuleRelationList.add(classRelation);
            }
        }
        this.businessRuleRelations = new ClassRelations(businessRuleRelationList);
    }

    public List<BusinessRule> list() {
        return list.stream()
                .sorted(Comparator.comparing(BusinessRule::typeIdentifier))
                .collect(Collectors.toList());
    }

    public boolean contains(TypeIdentifier typeIdentifier) {
        return identifiers().contains(typeIdentifier);
    }

    public boolean empty() {
        return list.isEmpty();
    }

    transient TypeIdentifiers cache;

    public TypeIdentifiers identifiers() {
        if (cache != null) {
            return cache;
        }
        return cache = list.stream()
                .map(BusinessRule::type)
                .map(TypeDeclaration::identifier)
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
                        new BusinessRules(entity.getValue(), this.businessRuleRelations)
                )).collect(Collectors.toList());
        return new BusinessRulePackages(list);
    }

    public List<BusinessRule> listCollection() {
        return list.stream()
                .filter(businessRule -> businessRule.businessRuleCategory() == BusinessRuleCategory.コレクション)
                .collect(Collectors.toList());
    }

    public List<BusinessRule> listValue(ValueKind valueKind) {
        return list.stream()
                .filter(businessRule -> businessRule.businessRuleCategory() == valueKind.businessRuleCategory)
                .collect(Collectors.toList());
    }

    public CategoryTypes toCategoryTypes() {
        List<CategoryType> list = this.list.stream()
                .filter(businessRule -> businessRule.businessRuleCategory() == BusinessRuleCategory.区分)
                .map(businessRule -> new CategoryType(businessRule))
                .collect(Collectors.toList());
        return new CategoryTypes(list);
    }

    public ClassRelations businessRuleRelations() {
        return businessRuleRelations;
    }

    public ClassRelations classRelations() {
        return classRelations;
    }

    public TypeIdentifiers allTypesRelatedTo(BusinessRule businessRule) {
        return classRelations().collectTypeIdentifierWhichRelationTo(businessRule.typeIdentifier());
    }

    public ClassRelations internalClassRelations() {
        List<ClassRelation> internalList = classRelations.list().stream()
                .filter(classRelation -> classRelation.within(identifiers()))
                .collect(Collectors.toList());
        return new ClassRelations(internalList);
    }
}

package org.dddjava.jig.domain.model.models.domains.businessrules;

import org.dddjava.jig.domain.model.models.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.packages.PackageIdentifier;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    List<JigType> list;
    ClassRelations businessRuleRelations;
    ClassRelations classRelations;

    public BusinessRules(List<JigType> list, ClassRelations classRelations) {
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

    public static BusinessRules from(Architecture architecture, ClassRelations classRelations, JigTypes jigTypes) {
        List<JigType> list = new ArrayList<>();
        for (JigType jigType : jigTypes.list()) {
            if (architecture.isBusinessRule(jigType)) {
                list.add(jigType);
            }
        }
        return new BusinessRules(list, classRelations);
    }

    public List<JigType> list() {
        return list.stream()
                .sorted(Comparator.comparing(JigType::typeIdentifier))
                .collect(toList());
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
                .map(JigType::typeIdentifier)
                .collect(TypeIdentifiers.collector());
    }

    public List<BusinessRulePackage> listPackages() {
        Map<PackageIdentifier, List<JigType>> map = list().stream()
                .collect(Collectors.groupingBy(
                        businessRule -> businessRule.typeIdentifier().packageIdentifier()
                ));
        return map.entrySet().stream()
                .map(entity -> new BusinessRulePackage(
                        entity.getKey(),
                        new BusinessRules(entity.getValue(), this.businessRuleRelations)
                ))
                .sorted(Comparator.comparing(businessRulePackage -> businessRulePackage.packageIdentifier().asText()))
                .collect(toList());
    }

    public ClassRelations businessRuleRelations() {
        return businessRuleRelations;
    }

    public ClassRelations classRelations() {
        return classRelations;
    }

    public TypeIdentifiers allTypesRelatedTo(JigType jigType) {
        return classRelations().collectTypeIdentifierWhichRelationTo(jigType.typeIdentifier());
    }

    public ClassRelations internalClassRelations() {
        List<ClassRelation> internalList = classRelations.list().stream()
                .filter(classRelation -> classRelation.within(identifiers()))
                .collect(toList());
        return new ClassRelations(internalList);
    }

    public TypeIdentifiers isolatedTypes() {
        return list.stream()
                .map(businessRule -> businessRule.typeIdentifier())
                .filter(typeIdentifier -> businessRuleRelations().unrelated(typeIdentifier))
                .collect(collectingAndThen(toList(), TypeIdentifiers::new));
    }

    public JigTypes jigTypes() {
        return new JigTypes(list);
    }
}

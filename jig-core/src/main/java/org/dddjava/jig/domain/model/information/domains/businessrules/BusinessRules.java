package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelation;
import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.package_.PackageJigTypes;

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

    public BusinessRules(JigTypes jigTypes, ClassRelations classRelations) {
        this.list = jigTypes.list();
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

    public List<JigType> list() {
        return list.stream()
                .sorted(Comparator.comparing(JigType::typeIdentifier))
                .collect(toList());
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

    public List<PackageJigTypes> listPackages() {
        Map<PackageIdentifier, List<JigType>> map = list().stream()
                .collect(Collectors.groupingBy(
                        businessRule -> businessRule.typeIdentifier().packageIdentifier()
                ));
        return map.entrySet().stream()
                .map(entity -> new PackageJigTypes(entity.getKey(), entity.getValue()))
                .sorted(Comparator.comparing(packageJigTypes -> packageJigTypes.packageIdentifier().asText()))
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
                .filter(classRelation -> {
                    // 両端ともbusinessRuleの型であるものに絞りこむ
                    TypeIdentifiers typeIdentifiers = identifiers();
                    return typeIdentifiers.contains(classRelation.from()) && typeIdentifiers.contains(classRelation.to());
                })
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

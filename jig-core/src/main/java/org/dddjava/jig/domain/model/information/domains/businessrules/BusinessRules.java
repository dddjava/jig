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

    JigTypes jigTypes;
    TypeIdentifiers typeIdentifiers;

    ClassRelations businessRuleRelations;
    ClassRelations classRelations;

    public BusinessRules(JigTypes jigTypes, ClassRelations classRelations) {
        this.jigTypes = jigTypes;
        this.classRelations = classRelations;

        typeIdentifiers = jigTypes.stream()
                .map(JigType::typeIdentifier)
                .collect(TypeIdentifiers.collector());
        Set<TypeIdentifier> businessRuleTypeSet = this.jigTypes.stream()
                .map(jigType -> jigType.typeIdentifier())
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
        return jigTypes.stream()
                .sorted(Comparator.comparing(JigType::typeIdentifier))
                .collect(toList());
    }

    public boolean empty() {
        return jigTypes.empty();
    }

    public TypeIdentifiers identifiers() {
        return typeIdentifiers;
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
        return classRelations.list().stream()
                .filter(classRelation -> {
                    // 両端ともbusinessRuleの型であるものに絞りこむ
                    TypeIdentifiers typeIdentifiers = identifiers();
                    return typeIdentifiers.contains(classRelation.from()) && typeIdentifiers.contains(classRelation.to());
                })
                .collect(collectingAndThen(toList(), ClassRelations::new));
    }

    public TypeIdentifiers isolatedTypes() {
        return jigTypes.stream()
                .map(jigType -> jigType.typeIdentifier())
                .filter(typeIdentifier -> businessRuleRelations().unrelated(typeIdentifier))
                .collect(collectingAndThen(toList(), TypeIdentifiers::new));
    }

    public JigTypes jigTypes() {
        return jigTypes;
    }
}

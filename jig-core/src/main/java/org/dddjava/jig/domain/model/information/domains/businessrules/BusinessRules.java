package org.dddjava.jig.domain.model.information.domains.businessrules;

import org.dddjava.jig.domain.model.data.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.data.packages.PackageIdentifier;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.information.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.information.jigobject.package_.PackageJigTypes;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * ビジネスルール一覧
 */
public class BusinessRules {

    JigTypes jigTypes;
    TypeIdentifiers typeIdentifiers;

    ClassRelations classRelations;
    private ClassRelations internalClassRelations;

    public BusinessRules(JigTypes jigTypes, ClassRelations classRelations) {
        this.jigTypes = jigTypes;
        this.classRelations = classRelations;

        this.typeIdentifiers = jigTypes.stream()
                .map(JigType::typeIdentifier)
                .collect(TypeIdentifiers.collector());

        this.internalClassRelations = classRelations.list().stream()
                .filter(classRelation -> typeIdentifiers.contains(classRelation.from()) && typeIdentifiers.contains(classRelation.to()))
                .collect(collectingAndThen(toList(), ClassRelations::new));
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
        return new ClassRelations(internalClassRelations.distinctList());
    }

    public ClassRelations classRelations() {
        return classRelations;
    }

    public TypeIdentifiers allTypesRelatedTo(JigType jigType) {
        return classRelations().collectTypeIdentifierWhichRelationTo(jigType.typeIdentifier());
    }

    public ClassRelations internalClassRelations() {
        return internalClassRelations;
    }

    public TypeIdentifiers isolatedTypes() {
        return typeIdentifiers.list().stream()
                .filter(typeIdentifier -> businessRuleRelations().unrelated(typeIdentifier))
                .collect(collectingAndThen(toList(), TypeIdentifiers::new));
    }

    public JigTypes jigTypes() {
        return jigTypes;
    }
}

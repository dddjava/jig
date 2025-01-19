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

    private ClassRelations internalClassRelations;

    public BusinessRules(JigTypes jigTypes, ClassRelations classRelations) {
        this.jigTypes = jigTypes;

        this.internalClassRelations = classRelations.list().stream()
                .filter(classRelation -> jigTypes.contains(classRelation.from()) && jigTypes.contains(classRelation.to()))
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
        return jigTypes.typeIdentifiers();
    }

    public List<PackageJigTypes> listPackages() {
        Map<PackageIdentifier, List<JigType>> map = jigTypes.stream()
                .collect(Collectors.groupingBy(
                        businessRule -> businessRule.typeIdentifier().packageIdentifier()
                ));
        return map.entrySet().stream()
                .map(entity -> new PackageJigTypes(entity.getKey(), entity.getValue()))
                .sorted(Comparator.comparing(packageJigTypes -> packageJigTypes.packageIdentifier().asText()))
                .collect(toList());
    }

    public ClassRelations businessRuleRelations() {
        return internalClassRelations.distinct();
    }

    public ClassRelations internalClassRelations() {
        return internalClassRelations;
    }

    public TypeIdentifiers isolatedTypes() {
        var relatedTypeIdentifiers = internalClassRelations().allTypeIdentifiers();

        return jigTypes.typeIdentifiers().list().stream()
                .filter(typeIdentifier -> !relatedTypeIdentifiers.contains(typeIdentifier))
                .collect(collectingAndThen(toList(), TypeIdentifiers::new));
    }

    public JigTypes jigTypes() {
        return jigTypes;
    }
}

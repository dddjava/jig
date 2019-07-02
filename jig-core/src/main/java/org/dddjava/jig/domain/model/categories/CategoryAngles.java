package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.interpret.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.interpret.relation.class_.ClassRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 区分の切り口一覧
 */
public class CategoryAngles {

    List<CategoryAngle> list;

    public CategoryAngles(CategoryTypes categoryTypes, AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        this.list = new ArrayList<>();
        for (CategoryType categoryType : categoryTypes.list()) {
            list.add(new CategoryAngle(categoryType, new ClassRelations(typeByteCodes), typeByteCodes.instanceFields(), typeByteCodes.staticFields()));
        }
    }

    public List<CategoryAngle> list() {
        return list;
    }

    public TypeIdentifiers userTypeIdentifiers() {
        List<TypeIdentifier> userTypeIdentifiers = list().stream()
                .flatMap(categoryAngle -> categoryAngle.userTypeIdentifiers().list().stream())
                .distinct()
                .filter(this::notCategory)
                .collect(Collectors.toList());
        return new TypeIdentifiers(userTypeIdentifiers);
    }

    boolean notCategory(TypeIdentifier typeIdentifier) {
        return list.stream()
                .noneMatch(categoryAngle -> categoryAngle.categoryType.typeIdentifier.equals(typeIdentifier));
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream().map(CategoryAngle::typeIdentifier).collect(TypeIdentifiers.collector());
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }
}

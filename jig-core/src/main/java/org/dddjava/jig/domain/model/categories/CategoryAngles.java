package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.relation.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

/**
 * 区分の切り口一覧
 */
public class CategoryAngles {

    List<CategoryAngle> list;

    public CategoryAngles(List<CategoryAngle> list) {
        this.list = list;
    }

    public static CategoryAngles of(TypeIdentifiers enumTypeIdentifies, CharacterizedTypes characterizedTypes, TypeDependencies typeDependencies, FieldDeclarations fieldDeclarations, StaticFieldDeclarations staticFieldDeclarations) {
        List<CategoryAngle> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : enumTypeIdentifies.list()) {
            list.add(CategoryAngle.of(typeIdentifier, characterizedTypes, typeDependencies, fieldDeclarations, staticFieldDeclarations));
        }
        return new CategoryAngles(list);
    }

    public List<CategoryAngle> list() {
        return list;
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream().map(CategoryAngle::typeIdentifier).collect(TypeIdentifiers.collector());
    }
}

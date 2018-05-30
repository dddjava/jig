package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.networks.TypeDependencies;

import java.util.ArrayList;
import java.util.List;

public class EnumAngles {

    List<EnumAngle> list;

    public EnumAngles(List<EnumAngle> list) {
        this.list = list;
    }

    public static EnumAngles of(TypeIdentifiers enumTypeIdentifies, CharacterizedTypes characterizedTypes, TypeDependencies typeDependencies, FieldDeclarations fieldDeclarations, FieldDeclarations staticFieldDeclarations) {
        List<EnumAngle> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : enumTypeIdentifies.list()) {
            list.add(EnumAngle.of(typeIdentifier, characterizedTypes, typeDependencies, fieldDeclarations, staticFieldDeclarations));
        }
        return new EnumAngles(list);
    }

    public List<EnumAngle> list() {
        return list;
    }

    public TypeIdentifiers typeIdentifiers() {
        return list.stream().map(EnumAngle::typeIdentifier).collect(TypeIdentifiers.collector());
    }
}

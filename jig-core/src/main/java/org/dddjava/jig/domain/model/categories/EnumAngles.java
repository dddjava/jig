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

    public static EnumAngles of(TypeIdentifiers typeIdentifiers, CharacterizedTypes characterizedTypes, TypeDependencies allTypeDependencies, FieldDeclarations allFieldDeclarations, FieldDeclarations allStaticFieldDeclarations) {
        List<EnumAngle> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            list.add(EnumAngle.of(typeIdentifier, characterizedTypes, allTypeDependencies, allFieldDeclarations, allStaticFieldDeclarations));
        }
        return new EnumAngles(list);
    }

    public List<EnumAngle> list() {
        return list;
    }
}

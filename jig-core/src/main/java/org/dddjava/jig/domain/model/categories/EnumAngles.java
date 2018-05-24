package org.dddjava.jig.domain.model.categories;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

public class EnumAngles {

    List<EnumAngle> list;

    public EnumAngles(List<EnumAngle> list) {
        this.list = list;
    }

    public static EnumAngles of(EnumAngleSource enumAngleSource) {
        List<EnumAngle> list = new ArrayList<>();
        for (TypeIdentifier typeIdentifier : enumAngleSource.getTypeIdentifiers().list()) {
            list.add(EnumAngle.of(typeIdentifier, enumAngleSource.getCharacterizedTypes(), enumAngleSource.getAllTypeDependencies(), enumAngleSource.getAllFieldDeclarations(), enumAngleSource.getAllStaticFieldDeclarations()));
        }
        return new EnumAngles(list);
    }

    public List<EnumAngle> list() {
        return list;
    }
}

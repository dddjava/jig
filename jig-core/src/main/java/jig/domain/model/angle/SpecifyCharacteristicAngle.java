package jig.domain.model.angle;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

public class SpecifyCharacteristicAngle {

    Characteristic characteristic;
    TypeIdentifier typeIdentifier;
    TypeIdentifiers userTypeIdentifiers;

    public SpecifyCharacteristicAngle(Characteristic characteristic, TypeIdentifier typeIdentifier, TypeIdentifiers userTypeIdentifiers) {
        this.characteristic = characteristic;
        this.typeIdentifier = typeIdentifier;
        this.userTypeIdentifiers = userTypeIdentifiers;
    }
}

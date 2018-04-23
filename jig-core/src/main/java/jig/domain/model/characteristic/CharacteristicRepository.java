package jig.domain.model.characteristic;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

public interface CharacteristicRepository {

    TypeIdentifiers getTypeIdentifiersOf(Characteristic characteristic);

    void register(TypeCharacteristics typeCharacteristics);

    boolean has(TypeIdentifier typeIdentifier, Characteristic mapper);

    Characteristics findCharacteristics(TypeIdentifier typeIdentifier);
}

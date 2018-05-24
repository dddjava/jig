package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

public interface CharacteristicRepository {

    TypeIdentifiers getTypeIdentifiersOf(Characteristic characteristic);

    void register(TypeCharacteristics typeCharacteristics);

    CharacterizedTypes allCharacterizedTypes();
}

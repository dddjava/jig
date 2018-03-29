package jig.domain.model.characteristic;

import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;

public interface CharacteristicRepository {

    TypeIdentifiers getTypeIdentifiersOf(Characteristic characteristic);

    void register(TypeIdentifier typeIdentifier, Characteristic characteristic);

    boolean has(TypeIdentifier typeIdentifier, Characteristic mapper);

    Characteristics characteristicsOf(TypeIdentifier typeIdentifier);
}

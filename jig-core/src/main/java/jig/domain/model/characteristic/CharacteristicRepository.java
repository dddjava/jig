package jig.domain.model.characteristic;

import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.TypeIdentifiers;

public interface CharacteristicRepository {

    TypeIdentifiers find(Characteristic characteristic);

    void register(TypeIdentifier typeIdentifier, Characteristic characteristic);

    boolean has(TypeIdentifier typeIdentifier, Characteristic mapper);

    Characteristics characteristicsOf(TypeIdentifier typeIdentifier);
}

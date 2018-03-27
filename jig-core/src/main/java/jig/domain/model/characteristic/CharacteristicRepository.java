package jig.domain.model.characteristic;

import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.Identifiers;

public interface CharacteristicRepository {

    Identifiers find(Characteristic characteristic);

    void register(TypeIdentifier typeIdentifier, Characteristic characteristic);

    boolean has(TypeIdentifier typeIdentifier, Characteristic mapper);

    Characteristics characteristicsOf(TypeIdentifier typeIdentifier);
}

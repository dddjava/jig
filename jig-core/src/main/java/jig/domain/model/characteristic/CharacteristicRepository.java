package jig.domain.model.characteristic;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;

public interface CharacteristicRepository {

    Identifiers find(Characteristic characteristic);

    void register(Identifier identifier, Characteristic characteristic);

    boolean has(Identifier identifier, Characteristic mapper);

    Characteristics characteristicsOf(Identifier identifier);
}

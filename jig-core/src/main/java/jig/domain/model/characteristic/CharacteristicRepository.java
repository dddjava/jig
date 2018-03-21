package jig.domain.model.characteristic;

import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;

public interface CharacteristicRepository {

    Identifiers find(Characteristic characteristic);

    void register(Identifier identifier, Characteristic characteristic);

    boolean has(Identifier identifier, Characteristic mapper);

    boolean has(MethodIdentifier identifier, Characteristic mapper);

    Characteristics characteristicsOf(Identifier identifier);
}

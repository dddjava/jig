package jig.domain.model.characteristic;

import jig.domain.model.thing.Identifier;
import jig.domain.model.thing.Identifiers;

public interface CharacteristicRepository {

    Identifiers find(Characteristic characteristic);

    void register(Identifier identifier, Characteristic characteristic);

    boolean has(Identifier identifier, Characteristic mapper);
}

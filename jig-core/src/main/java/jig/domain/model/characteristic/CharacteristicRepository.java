package jig.domain.model.characteristic;

import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;

public interface CharacteristicRepository {

    Names find(Characteristic characteristic);

    void register(Name name, Characteristic characteristic);
}

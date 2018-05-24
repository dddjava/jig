package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;

import java.util.ArrayList;
import java.util.List;

public class CharacterizedTypes {

    List<TypeCharacteristics> list;

    public CharacterizedTypes(List<TypeCharacteristics> list) {
        this.list = list;
    }

    public CharacterizedTypes(Implementations implementations) {
        this(new ArrayList<>());

        for (Implementation implementation : implementations.list()) {
            TypeCharacteristics typeCharacteristics = Characteristic.resolveCharacteristics(implementation);
            list.add(typeCharacteristics);
        }
    }

    public CharacterizedTypeStream stream() {
        return new CharacterizedTypeStream(list.stream());
    }
}

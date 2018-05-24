package org.dddjava.jig.domain.model.characteristic;

import java.util.List;

public class CharacterizedTypes {

    List<TypeCharacteristics> list;

    public CharacterizedTypes(List<TypeCharacteristics> list) {
        this.list = list;
    }

    public CharacterizedTypeStream stream() {
        return new CharacterizedTypeStream(list.stream());
    }
}

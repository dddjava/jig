package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.ByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.ByteCodes;

import java.util.ArrayList;
import java.util.List;

public class CharacterizedTypes {

    List<TypeCharacteristics> list;

    public CharacterizedTypes(List<TypeCharacteristics> list) {
        this.list = list;
    }

    public CharacterizedTypes(ByteCodes byteCodes) {
        this(new ArrayList<>());

        for (ByteCode byteCode : byteCodes.list()) {
            TypeCharacteristics typeCharacteristics = Characteristic.resolveCharacteristics(byteCode);
            list.add(typeCharacteristics);
        }
    }

    public CharacterizedTypeStream stream() {
        return new CharacterizedTypeStream(list.stream());
    }
}

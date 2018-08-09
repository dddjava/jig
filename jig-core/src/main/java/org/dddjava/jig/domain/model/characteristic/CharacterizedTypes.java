package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCode;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;

import java.util.ArrayList;
import java.util.List;

/**
 * 特徴付けられた型一覧
 */
public class CharacterizedTypes {

    List<CharacterizedType> list;

    public CharacterizedTypes(List<CharacterizedType> list) {
        this.list = list;
    }

    public CharacterizedTypes(TypeByteCodes typeByteCodes, CharacterizedTypeFactory characterizedTypeFactory) {
        this(new ArrayList<>());

        for (TypeByteCode typeByteCode : typeByteCodes.list()) {
            CharacterizedType characterizedType = characterizedTypeFactory.create(typeByteCode);
            list.add(characterizedType);
        }
    }

    public CharacterizedTypeStream stream() {
        return new CharacterizedTypeStream(list.stream());
    }
}

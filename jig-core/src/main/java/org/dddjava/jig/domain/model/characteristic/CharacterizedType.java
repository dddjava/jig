package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;

import java.util.Set;

/**
 * 特徴付けられた型
 */
public class CharacterizedType {

    private final TypeIdentifier typeIdentifier;
    private final Set<Characteristic> set;

    public CharacterizedType(TypeIdentifier typeIdentifier, Set<Characteristic> set) {
        this.typeIdentifier = typeIdentifier;
        this.set = set;
    }

    public Satisfaction has(Characteristic characteristic) {
        return Satisfaction.of(set.contains(characteristic));
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public Characteristics characteristics() {
        return new Characteristics(set);
    }
}

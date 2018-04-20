package jig.domain.model.characteristic;

import jig.domain.model.identifier.type.TypeIdentifier;

import java.util.Set;

public class TypeCharacteristics {

    private final TypeIdentifier typeIdentifier;
    private final Set<Characteristic> set;

    public TypeCharacteristics(TypeIdentifier typeIdentifier, Set<Characteristic> set) {
        this.typeIdentifier = typeIdentifier;
        this.set = set;
    }

    public Satisfaction has(Characteristic characteristic) {
        return Satisfaction.of(set.contains(characteristic));
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }
}

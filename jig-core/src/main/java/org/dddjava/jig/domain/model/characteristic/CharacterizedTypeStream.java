package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;

import java.util.Set;
import java.util.stream.Stream;

public class CharacterizedTypeStream {

    Stream<TypeCharacteristics> stream;

    public CharacterizedTypeStream(Stream<TypeCharacteristics> stream) {
        this.stream = stream;
    }

    public Characteristics characteristics() {
        return stream.map(TypeCharacteristics::characteristics).collect(Characteristics.collector());
    }

    public CharacterizedTypeStream filter(TypeIdentifiers typeIdentifiers) {
        Set<TypeIdentifier> set = typeIdentifiers.set();
        return new CharacterizedTypeStream(stream.filter(characterizedType -> set.contains(characterizedType.typeIdentifier())));
    }

    public TypeIdentifiers typeIdentifiers() {
        return stream.map(TypeCharacteristics::typeIdentifier).collect(TypeIdentifiers.collector());
    }

    public CharacterizedTypeStream filter(Characteristic characteristic) {
        return new CharacterizedTypeStream(stream.filter(typeCharacteristics -> typeCharacteristics.has(characteristic).isSatisfy()));
    }
}

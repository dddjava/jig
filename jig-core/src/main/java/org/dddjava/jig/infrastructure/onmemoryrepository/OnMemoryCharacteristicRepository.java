package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Repository
public class OnMemoryCharacteristicRepository implements CharacteristicRepository {

    final EnumMap<Characteristic, List<TypeIdentifier>> map;

    public OnMemoryCharacteristicRepository() {
        map = new EnumMap<>(Characteristic.class);
        for (Characteristic characteristic : Characteristic.values()) {
            map.put(characteristic, new ArrayList<>());
        }
    }

    List<TypeCharacteristics> list = new ArrayList<>();

    @Override
    public void register(TypeCharacteristics typeCharacteristics) {
        map.entrySet().stream()
                .filter(entry -> typeCharacteristics.has(entry.getKey()) == Satisfaction.SATISFY)
                .forEach(entry -> entry.getValue().add(typeCharacteristics.typeIdentifier()));
        list.add(typeCharacteristics);
    }

    @Override
    public TypeIdentifiers getTypeIdentifiersOf(Characteristic characteristic) {
        return new TypeIdentifiers(map.get(characteristic));
    }

    @Override
    public CharacterizedTypes allCharacterizedTypes() {
        return new CharacterizedTypes(list);
    }
}

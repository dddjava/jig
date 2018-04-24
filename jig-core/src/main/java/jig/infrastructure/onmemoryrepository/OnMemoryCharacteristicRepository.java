package jig.infrastructure.onmemoryrepository;

import jig.domain.model.characteristic.*;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import org.springframework.stereotype.Repository;

import java.util.*;

import static java.util.stream.Collectors.toSet;

@Repository
public class OnMemoryCharacteristicRepository implements CharacteristicRepository {

    final EnumMap<Characteristic, List<TypeIdentifier>> map;

    public OnMemoryCharacteristicRepository() {
        map = new EnumMap<>(Characteristic.class);
        for (Characteristic characteristic : Characteristic.values()) {
            map.put(characteristic, new ArrayList<>());
        }
    }

    @Override
    public void register(TypeCharacteristics typeCharacteristics) {
        map.entrySet().stream()
                .filter(entry -> typeCharacteristics.has(entry.getKey()) == Satisfaction.SATISFY)
                .forEach(entry -> entry.getValue().add(typeCharacteristics.typeIdentifier()));
    }

    @Override
    public TypeIdentifiers getTypeIdentifiersOf(Characteristic characteristic) {
        return new TypeIdentifiers(map.get(characteristic));
    }

    @Override
    public Characteristics findCharacteristics(TypeIdentifier typeIdentifier) {
        Set<Characteristic> set = map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(typeIdentifier))
                .map(Map.Entry::getKey)
                .collect(toSet());
        return new Characteristics(set);
    }

    @Override
    public Characteristics findCharacteristics(TypeIdentifiers typeIdentifiers) {
        HashSet<Characteristic> set = new HashSet<>();
        for (TypeIdentifier typeIdentifier : typeIdentifiers.list()) {
            for (Map.Entry<Characteristic, List<TypeIdentifier>> entry : map.entrySet()) {
                if (entry.getValue().contains(typeIdentifier)) {
                    set.add(entry.getKey());
                }
            }
        }
        return new Characteristics(set);
    }
}

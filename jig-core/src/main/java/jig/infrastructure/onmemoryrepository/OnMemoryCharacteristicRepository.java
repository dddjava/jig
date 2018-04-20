package jig.infrastructure.onmemoryrepository;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.TypeCharacteristics;
import jig.domain.model.characteristic.Satisfaction;
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
    public boolean has(TypeIdentifier typeIdentifier, Characteristic characteristic) {
        return map.get(characteristic).contains(typeIdentifier);
    }

    @Override
    public TypeCharacteristics characteristicsOf(TypeIdentifier typeIdentifier) {
        Set<Characteristic> set = map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(typeIdentifier))
                .map(Map.Entry::getKey)
                .collect(toSet());
        return new TypeCharacteristics(typeIdentifier, set);
    }

    @Override
    public TypeIdentifiers getTypeIdentifiersOf(Characteristic characteristic) {
        return new TypeIdentifiers(map.get(characteristic));
    }
}

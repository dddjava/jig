package jig.infrastructure.onmemoryrepository;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.identifier.Identifiers;
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
    public void register(TypeIdentifier typeIdentifier, Characteristic characteristic) {
        map.get(characteristic).add(typeIdentifier);
    }

    @Override
    public boolean has(TypeIdentifier typeIdentifier, Characteristic characteristic) {
        return map.get(characteristic).contains(typeIdentifier);
    }

    @Override
    public Characteristics characteristicsOf(TypeIdentifier typeIdentifier) {
        Set<Characteristic> set = map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(typeIdentifier))
                .map(Map.Entry::getKey)
                .collect(toSet());
        return new Characteristics(set);
    }

    @Override
    public Identifiers find(Characteristic characteristic) {
        return map.get(characteristic)
                .stream()
                .collect(Identifiers.collector());
    }
}

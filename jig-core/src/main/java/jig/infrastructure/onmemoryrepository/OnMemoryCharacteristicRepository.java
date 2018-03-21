package jig.infrastructure.onmemoryrepository;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.identifier.Identifier;
import jig.domain.model.identifier.Identifiers;
import jig.domain.model.identifier.MethodIdentifier;
import org.springframework.stereotype.Repository;

import java.util.*;

import static java.util.stream.Collectors.toSet;

@Repository
public class OnMemoryCharacteristicRepository implements CharacteristicRepository {

    final EnumMap<Characteristic, List<Identifier>> map;

    public OnMemoryCharacteristicRepository() {
        map = new EnumMap<>(Characteristic.class);
        for (Characteristic characteristic : Characteristic.values()) {
            map.put(characteristic, new ArrayList<>());
        }
    }

    @Override
    public void register(Identifier identifier, Characteristic characteristic) {
        map.get(characteristic).add(identifier);
    }

    @Override
    public boolean has(Identifier identifier, Characteristic characteristic) {
        return map.get(characteristic).contains(identifier);
    }

    @Override
    public boolean has(MethodIdentifier identifier, Characteristic characteristic) {
        return has(identifier.toIdentifier(), characteristic);
    }

    @Override
    public Characteristics characteristicsOf(Identifier identifier) {
        Set<Characteristic> set = map.entrySet().stream()
                .filter(entry -> entry.getValue().contains(identifier))
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

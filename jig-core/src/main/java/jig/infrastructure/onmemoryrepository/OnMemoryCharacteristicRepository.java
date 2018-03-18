package jig.infrastructure.onmemoryrepository;

import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.thing.Name;
import jig.domain.model.thing.Names;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

@Repository
public class OnMemoryCharacteristicRepository implements CharacteristicRepository {

    EnumMap<Characteristic, List<Name>> map = new EnumMap<>(Characteristic.class);

    @Override
    public void register(Name name, Characteristic characteristic) {
        map.computeIfAbsent(characteristic, t -> new ArrayList<>());
        map.get(characteristic).add(name);
    }

    @Override
    public Names find(Characteristic characteristic) {
        return map.entrySet().stream()
                .filter(e -> e.getKey().matches(characteristic))
                .flatMap(e -> e.getValue().stream())
                .collect(Names.collector());
    }
}

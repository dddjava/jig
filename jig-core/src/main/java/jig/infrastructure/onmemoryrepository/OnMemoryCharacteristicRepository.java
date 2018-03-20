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

    final EnumMap<Characteristic, List<Name>> map;

    public OnMemoryCharacteristicRepository() {
        map = new EnumMap<>(Characteristic.class);
        for (Characteristic characteristic : Characteristic.values()) {
            map.put(characteristic, new ArrayList<>());
        }
    }

    @Override
    public void register(Name name, Characteristic characteristic) {
        map.get(characteristic).add(name);
    }

    @Override
    public boolean has(Name name, Characteristic characteristic) {
        return map.get(characteristic).contains(name);
    }

    @Override
    public Names find(Characteristic characteristic) {
        return map.get(characteristic)
                .stream()
                .collect(Names.collector());
    }
}

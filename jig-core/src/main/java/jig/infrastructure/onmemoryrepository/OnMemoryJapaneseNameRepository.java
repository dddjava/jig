package jig.infrastructure.onmemoryrepository;

import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.japanasename.JapaneseNames;
import jig.domain.model.thing.Identifier;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryJapaneseNameRepository implements JapaneseNameRepository {

    final Map<Identifier, JapaneseName> map;

    public OnMemoryJapaneseNameRepository() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean exists(Identifier identifier) {
        return map.containsKey(identifier);
    }

    @Override
    public JapaneseName get(Identifier identifier) {
        return map.getOrDefault(identifier, new JapaneseName(""));
    }

    @Override
    public void register(Identifier fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }

    @Override
    public JapaneseNames all() {
        return new JapaneseNames(map);
    }
}

package jig.infrastructure;

import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.japanasename.JapaneseNames;
import jig.domain.model.thing.Name;

import java.util.HashMap;
import java.util.Map;

public class OnMemoryJapanaseNameRepository implements JapaneseNameRepository {

    final Map<Name, JapaneseName> map;

    public OnMemoryJapanaseNameRepository() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean exists(Name name) {
        return map.containsKey(name);
    }

    @Override
    public JapaneseName get(Name name) {
        return map.getOrDefault(name, new JapaneseName(""));
    }

    @Override
    public void register(Name fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }

    @Override
    public JapaneseNames all() {
        return new JapaneseNames(map);
    }
}

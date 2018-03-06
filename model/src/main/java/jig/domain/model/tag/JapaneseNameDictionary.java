package jig.domain.model.tag;

import jig.domain.model.thing.Name;

import java.util.HashMap;
import java.util.Map;

public class JapaneseNameDictionary {

    final Map<Name, JapaneseName> map;

    public JapaneseNameDictionary() {
        this.map = new HashMap<>();
    }

    public boolean exists(Name name) {
        return map.containsKey(name);
    }

    public JapaneseName get(Name name) {
        return map.getOrDefault(name, new JapaneseName(""));
    }

    public void register(Name fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }

    public void merge(JapaneseNameDictionary other) {
        map.putAll(other.map);
    }
}

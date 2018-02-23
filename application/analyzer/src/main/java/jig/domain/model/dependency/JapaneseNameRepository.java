package jig.domain.model.dependency;

import java.util.HashMap;
import java.util.Map;

public class JapaneseNameRepository {

    final Map<FullQualifiedName, JapaneseName> map;

    public JapaneseNameRepository() {
        this.map = new HashMap<>();
    }

    public boolean exists(FullQualifiedName fullQualifiedName) {
        return map.containsKey(fullQualifiedName);
    }

    public JapaneseName get(FullQualifiedName fullQualifiedName) {
        return map.getOrDefault(fullQualifiedName, new JapaneseName(""));
    }

    public void register(FullQualifiedName fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }
}

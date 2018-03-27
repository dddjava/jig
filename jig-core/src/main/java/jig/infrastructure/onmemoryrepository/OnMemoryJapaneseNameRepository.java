package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import jig.domain.model.japanasename.JapaneseNames;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryJapaneseNameRepository implements JapaneseNameRepository {

    final Map<TypeIdentifier, JapaneseName> map;

    public OnMemoryJapaneseNameRepository() {
        this.map = new HashMap<>();
    }

    @Override
    public boolean exists(TypeIdentifier typeIdentifier) {
        return map.containsKey(typeIdentifier);
    }

    @Override
    public JapaneseName get(TypeIdentifier typeIdentifier) {
        return map.getOrDefault(typeIdentifier, new JapaneseName(""));
    }

    @Override
    public void register(TypeIdentifier fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }

    @Override
    public JapaneseNames all() {
        return new JapaneseNames(map);
    }
}

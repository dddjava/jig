package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.PackageIdentifier;
import jig.domain.model.identifier.TypeIdentifier;
import jig.domain.model.japanasename.JapaneseName;
import jig.domain.model.japanasename.JapaneseNameRepository;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryJapaneseNameRepository implements JapaneseNameRepository {

    final Map<TypeIdentifier, JapaneseName> map;
    final Map<PackageIdentifier, JapaneseName> packageMap;

    public OnMemoryJapaneseNameRepository() {
        this.map = new HashMap<>();
        this.packageMap = new HashMap<>();
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
    public boolean exists(PackageIdentifier packageIdentifier) {
        return packageMap.containsKey(packageIdentifier);
    }

    @Override
    public JapaneseName get(PackageIdentifier packageIdentifier) {
        return packageMap.get(packageIdentifier);
    }

    @Override
    public void register(PackageIdentifier packageIdentifier, JapaneseName japaneseName) {
        packageMap.put(packageIdentifier, japaneseName);
    }

    @Override
    public void register(TypeIdentifier fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }
}

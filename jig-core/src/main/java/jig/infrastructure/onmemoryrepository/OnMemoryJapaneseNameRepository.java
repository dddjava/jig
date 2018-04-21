package jig.infrastructure.onmemoryrepository;

import jig.domain.model.identifier.namespace.PackageIdentifier;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.japanese.JapaneseName;
import jig.domain.model.japanese.JapaneseNameRepository;
import jig.domain.model.japanese.PackageJapaneseName;
import jig.domain.model.japanese.TypeJapaneseName;
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
    public void register(TypeJapaneseName typeJapaneseName) {
        register(typeJapaneseName.typeIdentifier(), typeJapaneseName.japaneseName());
    }

    @Override
    public void register(PackageJapaneseName packageJapaneseName) {
        register(packageJapaneseName.packageIdentifier(), packageJapaneseName.japaneseName());
    }

    @Override
    public void register(TypeIdentifier fqn, JapaneseName japaneseName) {
        map.put(fqn, japaneseName);
    }
}

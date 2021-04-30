package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.parts.alias.AliasRepository;
import org.dddjava.jig.domain.model.parts.alias.PackageAlias;
import org.dddjava.jig.domain.model.parts.alias.TypeAlias;
import org.dddjava.jig.domain.model.parts.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryAliasRepository implements AliasRepository {

    final Map<TypeIdentifier, TypeAlias> map = new HashMap<>();
    final Map<PackageIdentifier, PackageAlias> packageMap = new HashMap<>();

    @Override
    public TypeAlias get(TypeIdentifier typeIdentifier) {
        return map.getOrDefault(typeIdentifier, TypeAlias.empty(typeIdentifier));
    }

    @Override
    public boolean exists(PackageIdentifier packageIdentifier) {
        return packageMap.containsKey(packageIdentifier);
    }

    @Override
    public PackageAlias get(PackageIdentifier packageIdentifier) {
        return packageMap.getOrDefault(packageIdentifier, PackageAlias.empty(packageIdentifier));
    }

    @Override
    public void register(TypeAlias typeAlias) {
        map.put(typeAlias.typeIdentifier(), typeAlias);
    }

    @Override
    public void register(PackageAlias packageAlias) {
        packageMap.put(packageAlias.packageIdentifier(), packageAlias);
    }
}

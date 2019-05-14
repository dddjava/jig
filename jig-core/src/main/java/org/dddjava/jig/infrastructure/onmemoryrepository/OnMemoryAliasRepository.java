package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.implementation.analyzed.alias.*;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.implementation.analyzed.declaration.type.TypeIdentifier;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OnMemoryAliasRepository implements AliasRepository {

    final Map<TypeIdentifier, Alias> map = new HashMap<>();
    final Map<PackageIdentifier, Alias> packageMap = new HashMap<>();
    final List<MethodAlias> methodList = new ArrayList<>();

    @Override
    public Alias get(TypeIdentifier typeIdentifier) {
        return map.getOrDefault(typeIdentifier, new Alias(""));
    }

    @Override
    public boolean exists(PackageIdentifier packageIdentifier) {
        return packageMap.containsKey(packageIdentifier);
    }

    @Override
    public Alias get(PackageIdentifier packageIdentifier) {
        return packageMap.get(packageIdentifier);
    }

    @Override
    public void register(TypeAlias typeAlias) {
        map.put(typeAlias.typeIdentifier(), typeAlias.japaneseName());
    }

    @Override
    public void register(PackageAlias packageAlias) {
        packageMap.put(packageAlias.packageIdentifier(), packageAlias.japaneseName());
    }

    @Override
    public Alias get(MethodIdentifier methodIdentifier) {
        for (MethodAlias methodAlias : methodList) {
            if (methodAlias.methodIdentifier().matchesIgnoreOverload(methodIdentifier)) {
                return methodAlias.japaneseName();
            }
        }
        return new Alias("");
    }

    @Override
    public void register(MethodAlias methodAlias) {
        methodList.add(methodAlias);
    }
}

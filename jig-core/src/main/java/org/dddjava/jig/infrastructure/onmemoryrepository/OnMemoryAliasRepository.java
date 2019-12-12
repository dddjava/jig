package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasRepository;
import org.dddjava.jig.domain.model.jigloaded.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigloaded.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigloaded.alias.TypeAlias;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OnMemoryAliasRepository implements AliasRepository {

    final Map<TypeIdentifier, TypeAlias> map = new HashMap<>();
    final Map<PackageIdentifier, PackageAlias> packageMap = new HashMap<>();
    final List<MethodAlias> methodList = new ArrayList<>();

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

    @Override
    public MethodAlias get(MethodIdentifier methodIdentifier) {
        for (MethodAlias methodAlias : methodList) {
            if (methodAlias.methodIdentifier().matchesIgnoreOverload(methodIdentifier)) {
                return methodAlias;
            }
        }
        return MethodAlias.empty(methodIdentifier);
    }

    @Override
    public void register(MethodAlias methodAlias) {
        methodList.add(methodAlias);
    }
}

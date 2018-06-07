package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.*;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OnMemoryJapaneseNameRepository implements JapaneseNameRepository {

    final Map<TypeIdentifier, JapaneseName> map = new HashMap<>();
    final Map<PackageIdentifier, JapaneseName> packageMap = new HashMap<>();
    final List<MethodJapaneseName> methodList = new ArrayList<>();

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
    public void register(TypeJapaneseName typeJapaneseName) {
        map.put(typeJapaneseName.typeIdentifier(), typeJapaneseName.japaneseName());
    }

    @Override
    public void register(PackageJapaneseName packageJapaneseName) {
        packageMap.put(packageJapaneseName.packageIdentifier(), packageJapaneseName.japaneseName());
    }

    @Override
    public JapaneseName get(MethodIdentifier methodIdentifier) {
        for (MethodJapaneseName methodJapaneseName : methodList) {
            if (methodJapaneseName.methodIdentifier().matchesIgnoreOverload(methodIdentifier)) {
                return methodJapaneseName.japaneseName();
            }
        }
        return new JapaneseName("");
    }

    @Override
    public void register(MethodJapaneseName methodJapaneseName) {
        methodList.add(methodJapaneseName);
    }
}

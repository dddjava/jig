package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.namespace.PackageIdentifier;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.japanese.*;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class OnMemoryJapaneseNameRepository implements JapaneseNameRepository {

    final Map<TypeIdentifier, JapaneseName> map = new HashMap<>();
    final Map<PackageIdentifier, JapaneseName> packageMap = new HashMap<>();
    final Map<String, JapaneseName> methodMap = new HashMap<>();

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
    public JapaneseName get(MethodDeclaration methodDeclaration) {
        // TODO 引数と戻り値の型解決に制限があるため名前だけで引き当てる
        String methodName = methodDeclaration.methodSignature().methodName();
        String key = methodDeclaration.declaringType().fullQualifiedName() + methodName;
        return methodMap.getOrDefault(key, new JapaneseName(""));
    }

    @Override
    public void register(MethodJapaneseName methodJapaneseName) {
        MethodDeclaration methodDeclaration = methodJapaneseName.methodDeclaration();
        String methodName = methodDeclaration.methodSignature().methodName();
        String key = methodDeclaration.declaringType().fullQualifiedName() + methodName;
        methodMap.put(key, methodJapaneseName.japaneseName());
    }
}

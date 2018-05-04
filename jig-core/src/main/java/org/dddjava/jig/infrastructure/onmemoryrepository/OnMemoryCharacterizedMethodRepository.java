package org.dddjava.jig.infrastructure.onmemoryrepository;

import org.dddjava.jig.domain.model.characteristic.CharacterizedMethod;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethodRepository;
import org.dddjava.jig.domain.model.characteristic.MethodCharacteristic;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

@Repository
public class OnMemoryCharacterizedMethodRepository implements CharacterizedMethodRepository {

    HashMap<MethodCharacteristic, List<MethodDeclaration>> map = new HashMap<>();

    @Override
    public CharacterizedMethod get(MethodDeclaration methodDeclaration) {
        return null;
    }

    @Override
    public MethodDeclarations getCharacterizedMethods(MethodCharacteristic methodCharacteristic) {
        List<MethodDeclaration> list = map.getOrDefault(methodCharacteristic, Collections.emptyList());
        return new MethodDeclarations(list);
    }

    @Override
    public void register(MethodCharacteristic methodCharacteristic, MethodDeclaration methodDeclaration) {
        map.computeIfAbsent(methodCharacteristic, dummy -> new ArrayList<>());

        map.get(methodCharacteristic).add(methodDeclaration);
    }
}

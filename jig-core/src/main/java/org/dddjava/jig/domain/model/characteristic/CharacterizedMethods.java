package org.dddjava.jig.domain.model.characteristic;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;

import java.util.ArrayList;
import java.util.List;

public class CharacterizedMethods {

    List<CharacterizedMethod> list;

    public CharacterizedMethods(List<MethodImplementation> methodImplementations) {
        list = new ArrayList<>();
        for (MethodImplementation methodImplementation : methodImplementations) {
            list.add(new CharacterizedMethod(methodImplementation));
        }
    }

    public MethodDeclarations serviceMethods(CharacterizedTypes characterizedTypes) {
        return methodsOf(MethodCharacteristic.SERVICE_METHOD, characterizedTypes);
    }

    public MethodDeclarations repositoryMethods(CharacterizedTypes characterizedTypes) {
        return methodsOf(MethodCharacteristic.REPOSITORY_METHOD, characterizedTypes);
    }

    public MethodDeclarations mapperMethods(CharacterizedTypes characterizedTypes) {
        return methodsOf(MethodCharacteristic.MAPPER_METHOD, characterizedTypes);
    }

    private MethodDeclarations methodsOf(MethodCharacteristic methodCharacteristic, CharacterizedTypes characterizedTypes) {
        return list.stream()
                .filter(characterizedMethod -> {
                    CharacterizedType characterizedType = characterizedTypes.stream()
                            .pickup(characterizedMethod.methodDeclaration().declaringType());
                    return characterizedMethod.has(methodCharacteristic, characterizedType);
                })
                .map(CharacterizedMethod::methodDeclaration)
                .collect(MethodDeclarations.collector());
    }

    public MethodDeclarations decisionMethods() {
        return list.stream()
                .filter(CharacterizedMethod::hasDecision)
                .map(CharacterizedMethod::methodDeclaration)
                .collect(MethodDeclarations.collector());
    }
}

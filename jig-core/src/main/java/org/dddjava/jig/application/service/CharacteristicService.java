package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementation;
import org.dddjava.jig.domain.model.implementation.bytecode.Implementations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodImplementation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 特徴サービス
 */
@Service
public class CharacteristicService {

    CharacteristicRepository characteristicRepository;
    CharacterizedMethodRepository characterizedMethodRepository;

    public CharacteristicService(CharacteristicRepository characteristicRepository, CharacterizedMethodRepository characterizedMethodRepository) {
        this.characteristicRepository = characteristicRepository;
        this.characterizedMethodRepository = characterizedMethodRepository;
    }

    /**
     * モデルの実装リストを元に特徴を登録する。
     */
    public void registerCharacteristic(Implementations implementations) {
        for (Implementation implementation : implementations.list()) {
            registerCharacteristic(implementation);
        }
    }

    /**
     * モデルの実装を元に特徴を登録する。
     */
    public void registerCharacteristic(Implementation implementation) {
        TypeCharacteristics typeCharacteristics = Characteristic.resolveCharacteristics(implementation);
        characteristicRepository.register(typeCharacteristics);

        List<MethodImplementation> methodImplementations = implementation.instanceMethodSpecifications();
        for (MethodImplementation methodImplementation : methodImplementations) {
            if (methodImplementation.hasDecision()) {
                characterizedMethodRepository.register(MethodCharacteristic.HAS_DECISION, methodImplementation.methodDeclaration);
            }

            if (typeCharacteristics.has(Characteristic.SERVICE).isSatisfy()) {
                characterizedMethodRepository.register(MethodCharacteristic.SERVICE_METHOD, methodImplementation.methodDeclaration);
            }
            if (typeCharacteristics.has(Characteristic.REPOSITORY).isSatisfy()) {
                characterizedMethodRepository.register(MethodCharacteristic.REPOSITORY_METHOD, methodImplementation.methodDeclaration);
            }
            if (typeCharacteristics.has(Characteristic.MAPPER).isSatisfy()) {
                characterizedMethodRepository.register(MethodCharacteristic.MAPPER_METHOD, methodImplementation.methodDeclaration);
            }
        }
    }

    public MethodDeclarations getServiceMethods() {
        return characterizedMethodRepository.getCharacterizedMethods(MethodCharacteristic.SERVICE_METHOD);
    }

    public MethodDeclarations getRepositoryMethods() {
        return characterizedMethodRepository.getCharacterizedMethods(MethodCharacteristic.REPOSITORY_METHOD);
    }

    public MethodDeclarations getDecisionMethods() {
        return characterizedMethodRepository.getCharacterizedMethods(MethodCharacteristic.HAS_DECISION);
    }

    public MethodDeclarations getMapperMethods() {
        return characterizedMethodRepository.getCharacterizedMethods(MethodCharacteristic.MAPPER_METHOD);
    }

    public CharacterizedTypes allCharacterizedTypes() {
        return characteristicRepository.allCharacterizedTypes();
    }
}

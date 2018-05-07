package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.characteristic.*;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.specification.MethodSpecification;
import org.dddjava.jig.domain.model.specification.Specification;
import org.dddjava.jig.domain.model.specification.Specifications;
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

    public void registerCharacteristic(Specifications specifications) {
        for (Specification specification : specifications.list()) {
            registerCharacteristic(specification);
        }
    }

    public void registerCharacteristic(Specification specification) {
        TypeCharacteristics typeCharacteristics = Characteristic.resolveCharacteristics(specification);
        characteristicRepository.register(typeCharacteristics);

        List<MethodSpecification> methodSpecifications = specification.instanceMethodSpecifications();
        for (MethodSpecification methodSpecification : methodSpecifications) {
            if (methodSpecification.hasDecision()) {
                characterizedMethodRepository.register(MethodCharacteristic.HAS_DECISION, methodSpecification.methodDeclaration);
            }

            if (typeCharacteristics.has(Characteristic.SERVICE).isSatisfy()) {
                characterizedMethodRepository.register(MethodCharacteristic.SERVICE_METHOD, methodSpecification.methodDeclaration);
            }
            if (typeCharacteristics.has(Characteristic.REPOSITORY).isSatisfy()) {
                characterizedMethodRepository.register(MethodCharacteristic.REPOSITORY_METHOD, methodSpecification.methodDeclaration);
            }
            if (typeCharacteristics.has(Characteristic.MAPPER).isSatisfy()) {
                characterizedMethodRepository.register(MethodCharacteristic.MAPPER_METHOD, methodSpecification.methodDeclaration);
            }
        }
    }

    public Characteristics findCharacteristics(TypeIdentifiers typeIdentifiers) {
        return characteristicRepository.findCharacteristics(typeIdentifiers);
    }

    public Characteristics findCharacteristics(TypeIdentifier typeIdentifier) {
        return characteristicRepository.findCharacteristics(typeIdentifier);
    }

    public TypeIdentifiers getEnums() {
        return characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM);
    }

    public TypeIdentifiers getTypeIdentifiersOf(Characteristic characteristic) {
        return characteristicRepository.getTypeIdentifiersOf(characteristic);
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
}

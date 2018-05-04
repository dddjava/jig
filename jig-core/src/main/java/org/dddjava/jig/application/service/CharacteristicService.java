package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacteristicRepository;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethodRepository;
import org.dddjava.jig.domain.model.characteristic.MethodCharacteristic;
import org.dddjava.jig.domain.model.specification.MethodSpecification;
import org.dddjava.jig.domain.model.specification.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CharacteristicService {

    CharacteristicRepository characteristicRepository;
    CharacterizedMethodRepository characterizedMethodRepository;

    public CharacteristicService(CharacteristicRepository characteristicRepository, CharacterizedMethodRepository characterizedMethodRepository) {
        this.characteristicRepository = characteristicRepository;
        this.characterizedMethodRepository = characterizedMethodRepository;
    }

    public void registerCharacteristic(Specification specification){
        characteristicRepository.register(Characteristic.resolveCharacteristics(specification));

        List<MethodSpecification> methodSpecifications = specification.instanceMethodSpecifications();
        for (MethodSpecification methodSpecification : methodSpecifications) {
            if (methodSpecification.hasDecision()) {
                characterizedMethodRepository.register(MethodCharacteristic.HAS_DECISION, methodSpecification.methodDeclaration);
            }
        }
    }
}

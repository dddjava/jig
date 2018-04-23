package jig.application.service;

import jig.domain.model.angle.EnumAngle;
import jig.domain.model.angle.EnumAngles;
import jig.domain.model.angle.GenericModelAngle;
import jig.domain.model.angle.GenericModelAngles;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;

import java.util.List;

import static java.util.stream.Collectors.toList;

public class AngleService {

    private final CharacteristicRepository characteristicRepository;
    private final RelationRepository relationRepository;

    public AngleService(CharacteristicRepository characteristicRepository, RelationRepository relationRepository) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
    }

    public EnumAngles enumAngles() {
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(Characteristic.ENUM);
        List<EnumAngle> list = typeIdentifiers.list().stream().map(typeIdentifier -> {
            Characteristics characteristics = characteristicRepository.findCharacteristics(typeIdentifier);
            TypeIdentifiers userTypeIdentifiers = relationRepository.findUserTypes(typeIdentifier);
            FieldDeclarations fieldDeclarations = relationRepository.findFieldsOf(typeIdentifier);
            FieldDeclarations constantsDeclarations = relationRepository.findConstants(typeIdentifier);
            return new EnumAngle(characteristics, typeIdentifier, userTypeIdentifiers, constantsDeclarations, fieldDeclarations);
        }).collect(toList());
        return new EnumAngles(list);
    }

    public GenericModelAngles specifyCharacteristicAngles(Characteristic characteristic) {
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(characteristic);
        List<GenericModelAngle> list = typeIdentifiers.list().stream().map(typeIdentifier -> {
            TypeIdentifiers userTypeIdentifiers = relationRepository.findUserTypes(typeIdentifier);
            return new GenericModelAngle(characteristic, typeIdentifier, userTypeIdentifiers);
        }).collect(toList());
        return new GenericModelAngles(list);
    }
}

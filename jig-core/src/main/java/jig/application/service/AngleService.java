package jig.application.service;

import jig.domain.model.angle.*;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.declaration.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AngleService {

    private final CharacteristicRepository characteristicRepository;
    private final RelationRepository relationRepository;

    public AngleService(CharacteristicRepository characteristicRepository, RelationRepository relationRepository) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
    }

    public ServiceAngles serviceAngles() {
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(Characteristic.SERVICE);
        List<ServiceAngle> list = typeIdentifiers.list().stream().flatMap(typeIdentifier ->
                relationRepository.methodsOf(typeIdentifier).list().stream().map(methodDeclaration -> {
                    TypeIdentifier returnTypeIdentifier = relationRepository.getReturnTypeOf(methodDeclaration);
                    TypeIdentifiers userTypes = relationRepository.findUserTypes(methodDeclaration);
                    Characteristics userCharacteristics = characteristicRepository.findCharacteristics(userTypes);
                    TypeIdentifiers usingFieldTypeIdentifiers = relationRepository.findUseFields(methodDeclaration).toTypeIdentifies();
                    MethodDeclarations usingRepositoryMethods = relationRepository.findUseMethod(methodDeclaration)
                            .filter(m -> characteristicRepository.findCharacteristics(m.declaringType()).has(Characteristic.REPOSITORY).isSatisfy());
                    return new ServiceAngle(methodDeclaration, returnTypeIdentifier, userCharacteristics, usingFieldTypeIdentifiers, usingRepositoryMethods);
                })).collect(toList());
        return new ServiceAngles(list);
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

    public GenericModelAngles genericModelAngles(Characteristic characteristic) {
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(characteristic);
        List<GenericModelAngle> list = typeIdentifiers.list().stream().map(typeIdentifier -> {
            TypeIdentifiers userTypeIdentifiers = relationRepository.findUserTypes(typeIdentifier);
            return new GenericModelAngle(characteristic, typeIdentifier, userTypeIdentifiers);
        }).collect(toList());
        return new GenericModelAngles(list);
    }

    /**
     * 文字列比較を行なっているメソッドを見つける。
     *
     * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
     * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
     */
    public DesignSmellAngle stringComparing() {
        // String#equals(Object)
        MethodDeclaration equalsMethod = new MethodDeclaration(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        Collections.singletonList(new TypeIdentifier(Object.class))));

        MethodDeclarations userMethods = relationRepository.findUserMethods(equalsMethod);
        return new DesignSmellAngle(userMethods);
    }
}

package jig.application.service;

import jig.domain.model.angle.*;
import jig.domain.model.characteristic.Characteristic;
import jig.domain.model.characteristic.CharacteristicRepository;
import jig.domain.model.characteristic.Characteristics;
import jig.domain.model.datasource.Sql;
import jig.domain.model.datasource.SqlRepository;
import jig.domain.model.datasource.Sqls;
import jig.domain.model.declaration.field.FieldDeclarations;
import jig.domain.model.declaration.method.MethodDeclaration;
import jig.domain.model.declaration.method.MethodDeclarations;
import jig.domain.model.declaration.method.MethodSignature;
import jig.domain.model.identifier.type.TypeIdentifier;
import jig.domain.model.identifier.type.TypeIdentifiers;
import jig.domain.model.relation.RelationRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class AngleService {

    CharacteristicRepository characteristicRepository;
    RelationRepository relationRepository;
    SqlRepository sqlRepository;

    public AngleService(CharacteristicRepository characteristicRepository, RelationRepository relationRepository, SqlRepository sqlRepository) {
        this.characteristicRepository = characteristicRepository;
        this.relationRepository = relationRepository;
        this.sqlRepository = sqlRepository;
    }

    public ServiceAngles serviceAngles() {
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(Characteristic.SERVICE);
        List<ServiceAngle> list = typeIdentifiers.list().stream().flatMap(typeIdentifier ->
                relationRepository.methodsOf(typeIdentifier).list().stream().map(methodDeclaration -> {
                    TypeIdentifier returnTypeIdentifier = relationRepository.getReturnTypeOf(methodDeclaration);

                    TypeIdentifiers userTypes = relationRepository.findUserTypes(methodDeclaration);
                    Characteristics userCharacteristics = characteristicRepository.findCharacteristics(userTypes);

                    MethodDeclarations userServiceMethods = relationRepository.findUserMethods(methodDeclaration)
                            .filter(userMethod -> characteristicRepository
                                    .findCharacteristics(userMethod.declaringType())
                                    .has(Characteristic.SERVICE).isSatisfy());

                    TypeIdentifiers usingFieldTypeIdentifiers = relationRepository.findUseFields(methodDeclaration).toTypeIdentifies();

                    MethodDeclarations usingRepositoryMethods = relationRepository.findUseMethod(methodDeclaration)
                            .filter(m -> characteristicRepository.findCharacteristics(m.declaringType()).has(Characteristic.REPOSITORY).isSatisfy());
                    return new ServiceAngle(methodDeclaration, returnTypeIdentifier, userCharacteristics, userServiceMethods, usingFieldTypeIdentifiers, usingRepositoryMethods);
                })).collect(toList());
        return new ServiceAngles(list);
    }

    public DatasourceAngles datasourceAngles() {
        TypeIdentifiers typeIdentifiers = characteristicRepository.getTypeIdentifiersOf(Characteristic.REPOSITORY);
        List<DatasourceAngle> list = typeIdentifiers.list().stream().flatMap(typeIdentifier ->
                relationRepository.methodsOf(typeIdentifier).list().stream().map(methodDeclaration -> {
                    TypeIdentifier returnTypeIdentifier = relationRepository.getReturnTypeOf(methodDeclaration);

                    MethodDeclarations mapperMethods = relationRepository.findConcrete(methodDeclaration)
                            .map(relationRepository::findUseMethod)
                            .filter(methodIdentifier -> characteristicRepository.findCharacteristics(methodIdentifier.declaringType()).has(Characteristic.MAPPER).isSatisfy());
                    List<Sql> sqls = new ArrayList<>();
                    for (MethodDeclaration identifier : mapperMethods.list()) {
                        sqlRepository.find(identifier).ifPresent(sqls::add);
                    }
                    return new DatasourceAngle(methodDeclaration, returnTypeIdentifier, new Sqls(sqls));
                })).collect(toList());
        return new DatasourceAngles(list);
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

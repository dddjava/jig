package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.categories.EnumAngle;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.datasources.DatasourceAngle;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngle;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.values.ValueAngle;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 分析の切り口サービス
 */
@Service
public class AngleService {

    CharacteristicService characteristicService;
    RelationRepository relationRepository;
    DatasourceService datasourceService;

    public AngleService(CharacteristicService characteristicService, RelationRepository relationRepository, DatasourceService datasourceService) {
        this.characteristicService = characteristicService;
        this.relationRepository = relationRepository;
        this.datasourceService = datasourceService;
    }

    public ServiceAngles serviceAngles() {
        MethodDeclarations serviceMethods = characteristicService.getServiceMethods();

        MethodRelations methodRelations = relationRepository.allMethodRelations();
        CharacterizedTypes characterizedTypes = characteristicService.allCharacterizedTypes();
        MethodUsingFields methodUsingFields = relationRepository.allMethodUsingFields();

        return ServiceAngles.of(serviceMethods, methodRelations, characterizedTypes, methodUsingFields);
    }

    public DatasourceAngles datasourceAngles() {
        MethodDeclarations mapperMethods = characteristicService.getMapperMethods();

        List<DatasourceAngle> list = characteristicService.getRepositoryMethods().list().stream()
                .map(methodDeclaration -> {
                    // Repositoryを実装している具象メソッド
                    MethodDeclarations datasourceMethods = relationRepository.findConcrete(methodDeclaration);

                    // 使用しているMapperメソッド
                    MethodDeclarations usingMethods = new MethodDeclarations(Collections.emptyList());
                    for (MethodDeclaration datasourceMethod : datasourceMethods.list()) {
                        usingMethods = usingMethods.union(relationRepository.findUseMethods(datasourceMethod));
                    }
                    MethodDeclarations usingMapperMethods = usingMethods.intersection(mapperMethods);

                    Sqls sqls = datasourceService.findSqls(usingMapperMethods);
                    return new DatasourceAngle(methodDeclaration, sqls);
                }).collect(toList());
        return new DatasourceAngles(list);
    }

    public EnumAngles enumAngles() {
        TypeIdentifiers typeIdentifiers = characteristicService.getEnums();
        List<EnumAngle> list = typeIdentifiers.list().stream().map(typeIdentifier -> {
            Characteristics characteristics = characteristicService.findCharacteristics(typeIdentifier);
            TypeIdentifiers userTypeIdentifiers = relationRepository.findUserTypes(typeIdentifier);
            FieldDeclarations fieldDeclarations = relationRepository.findFieldsOf(typeIdentifier);
            FieldDeclarations constantsDeclarations = relationRepository.findConstants(typeIdentifier);
            return new EnumAngle(characteristics, typeIdentifier, userTypeIdentifiers, constantsDeclarations, fieldDeclarations);
        }).collect(toList());
        return new EnumAngles(list);
    }

    public ValueAngles genericModelAngles(ValueKind valueKind) {
        TypeIdentifiers typeIdentifiers = characteristicService.getTypeIdentifiersOf(valueKind);
        List<ValueAngle> list = typeIdentifiers.list().stream().map(typeIdentifier -> {
            TypeIdentifiers userTypeIdentifiers = relationRepository.findUserTypes(typeIdentifier);
            return new ValueAngle(valueKind, typeIdentifier, userTypeIdentifiers);
        }).collect(toList());
        return new ValueAngles(list);
    }

    /**
     * 文字列比較を行なっているメソッドを見つける。
     *
     * 文字列比較を行なっているメソッドはビジネスルールの分類判定を行なっている可能性が高い。
     * サービスなどに登場した場合はかなり拙いし、そうでなくても列挙を使用するなど改善の余地がある。
     */
    public StringComparingAngle stringComparing() {
        // String#equals(Object)
        MethodDeclaration equalsMethod = new MethodDeclaration(
                new TypeIdentifier(String.class),
                new MethodSignature(
                        "equals",
                        Collections.singletonList(new TypeIdentifier(Object.class))),
                new TypeIdentifier("boolean"));

        MethodDeclarations userMethods = relationRepository.findUserMethods(equalsMethod);
        return new StringComparingAngle(userMethods);
    }

    public DecisionAngles decision() {
        MethodDeclarations methods = characteristicService.getDecisionMethods();
        List<DecisionAngle> list = methods.list().stream()
                .map(methodDeclaration -> {
                    TypeIdentifier typeIdentifier = methodDeclaration.declaringType();
                    Characteristics characteristics = characteristicService.findCharacteristics(typeIdentifier);
                    return new DecisionAngle(methodDeclaration, characteristics);
                }).collect(toList());
        return new DecisionAngles(list);
    }
}

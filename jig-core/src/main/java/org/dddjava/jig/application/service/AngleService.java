package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.relation.ImplementationMethods;
import org.dddjava.jig.domain.model.implementation.relation.MethodRelations;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.networks.DependencyRepository;
import org.dddjava.jig.domain.model.networks.TypeDependencies;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.springframework.stereotype.Service;

/**
 * 分析の切り口サービス
 */
@Service
public class AngleService {

    CharacteristicService characteristicService;
    RelationRepository relationRepository;
    DatasourceService datasourceService;
    DependencyRepository dependencyRepository;

    public AngleService(CharacteristicService characteristicService, RelationRepository relationRepository, DependencyRepository dependencyRepository, DatasourceService datasourceService) {
        this.characteristicService = characteristicService;
        this.relationRepository = relationRepository;
        this.dependencyRepository = dependencyRepository;
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
        ImplementationMethods implementationMethods = relationRepository.allImplementationMethods();
        MethodRelations methodRelations = relationRepository.allMethodRelations();
        Sqls allSqls = datasourceService.allSqls();

        MethodDeclarations repositoryMethods = characteristicService.getRepositoryMethods();

        return DatasourceAngles.of(repositoryMethods, mapperMethods, implementationMethods, methodRelations, allSqls);
    }

    public EnumAngles enumAngles() {
        TypeIdentifiers typeIdentifiers = characteristicService.getEnums();
        CharacterizedTypes characterizedTypes = characteristicService.allCharacterizedTypes();
        TypeDependencies allTypeDependencies = dependencyRepository.findAllTypeDependency();
        FieldDeclarations allFieldDeclarations = relationRepository.allFieldDeclarations();
        FieldDeclarations allStaticFieldDeclarations = relationRepository.allStaticFieldDeclarations();

        return EnumAngles.of(typeIdentifiers, characterizedTypes, allTypeDependencies, allFieldDeclarations, allStaticFieldDeclarations);
    }

    public ValueAngles valueAngles(ValueKind valueKind) {
        TypeIdentifiers typeIdentifiers = characteristicService.getTypeIdentifiersOf(valueKind);
        TypeDependencies allTypeDependencies = dependencyRepository.findAllTypeDependency();

        return ValueAngles.of(valueKind, typeIdentifiers, allTypeDependencies);
    }

    public StringComparingAngle stringComparing() {
        MethodRelations methodRelations = relationRepository.allMethodRelations();
        return StringComparingAngle.of(methodRelations);
    }

    public DecisionAngles decision() {
        MethodDeclarations methods = characteristicService.getDecisionMethods();
        CharacterizedTypes characterizedTypes = characteristicService.allCharacterizedTypes();

        return DecisionAngles.of(methods, characterizedTypes);
    }
}

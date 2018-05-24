package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.categories.EnumAngleSource;
import org.dddjava.jig.domain.model.categories.EnumAngles;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.datasources.DatasourceAngleSource;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngle;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.relation.RelationRepository;
import org.dddjava.jig.domain.model.networks.DependencyRepository;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.values.ValueAngleSource;
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

    public ServiceAngles serviceAngles(ProjectData projectData) {
        MethodDeclarations serviceMethods = characteristicService.getServiceMethods();

        return ServiceAngles.of(serviceMethods,
                projectData.methodRelations(),
                projectData.characterizedTypes(),
                projectData.methodUsingFields());
    }

    public DatasourceAngles datasourceAngles(ProjectData projectData) {
        MethodDeclarations repositoryMethods = characteristicService.getRepositoryMethods();

        return DatasourceAngles.of(new DatasourceAngleSource(
                repositoryMethods,
                projectData.mapperMethods(),
                projectData.implementationMethods(),
                projectData.methodRelations(),
                projectData.sqls()));
    }

    public EnumAngles enumAngles(ProjectData projectData) {
        TypeIdentifiers enumTypeIdentifies = projectData.characterizedTypes().stream()
                .filter(Characteristic.ENUM)
                .typeIdentifiers();

        return EnumAngles.of(new EnumAngleSource(
                enumTypeIdentifies,
                projectData.characterizedTypes(),
                projectData.typeDependencies(),
                projectData.fieldDeclarations(),
                projectData.staticFieldDeclarations()));
    }

    public ValueAngles valueAngles(ValueKind valueKind, ProjectData projectData) {
        return ValueAngles.of(valueKind, new ValueAngleSource(
                projectData.valueTypes(), projectData.typeDependencies()));
    }

    public StringComparingAngle stringComparing(ProjectData projectData) {
        return StringComparingAngle.of(projectData.methodRelations());
    }

    public DecisionAngles decision(ProjectData projectData) {
        MethodDeclarations methods = characteristicService.getDecisionMethods();

        CharacterizedTypes characterizedTypes = projectData.characterizedTypes();
        return DecisionAngles.of(methods, characterizedTypes);
    }
}

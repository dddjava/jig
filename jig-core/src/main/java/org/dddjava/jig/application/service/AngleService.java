package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.basic.Warning;
import org.dddjava.jig.domain.model.booleans.model.BoolQueryAngles;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.characteristic.Characteristic;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.Methods;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 分析の切り口サービス
 */
@Service
public class AngleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AngleService.class);

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(ProjectData projectData) {
        MethodDeclarations serviceMethods = projectData.characterizedMethods().serviceMethods();

        if (serviceMethods.empty()) {
            LOGGER.warn(Warning.サービス検出異常.text());
        }

        return ServiceAngles.of(serviceMethods,
                projectData.methodRelations(),
                projectData.characterizedTypes(),
                projectData.methodUsingFields(),
                projectData.characterizedMethods());
    }

    /**
     * データソースを分析する
     */
    public DatasourceAngles datasourceAngles(ProjectData projectData) {
        CharacterizedMethods characterizedMethods = projectData.characterizedMethods();

        MethodDeclarations mapperMethodDeclarations = characterizedMethods.mapperMethods();
        if (mapperMethodDeclarations.empty()) {
            LOGGER.warn(Warning.Mapperメソッド検出異常.text());
        }

        return DatasourceAngles.of(
                characterizedMethods.repositoryMethods(),
                mapperMethodDeclarations,
                projectData.implementationMethods(),
                projectData.methodRelations(),
                projectData.sqls());
    }

    /**
     * enumを分析する
     */
    public CategoryAngles enumAngles(ProjectData projectData) {
        TypeIdentifiers enumTypeIdentifies = projectData.characterizedTypes().stream()
                .filter(Characteristic.ENUM)
                .typeIdentifiers();

        return CategoryAngles.of(enumTypeIdentifies,
                projectData.characterizedTypes(),
                projectData.typeDependencies(),
                projectData.fieldDeclarations(),
                projectData.staticFieldDeclarations());
    }

    /**
     * 値を分析する
     */
    public ValueAngles valueAngles(ValueKind valueKind, ProjectData projectData) {
        return ValueAngles.of(valueKind, projectData.valueTypes(), projectData.typeDependencies());
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingAngles stringComparing(ProjectData projectData) {
        return StringComparingAngles.of(projectData.methodRelations());
    }

    /**
     * 分岐箇所を分析する
     */
    public DecisionAngles decision(ProjectData projectData) {
        Methods methods = projectData.methods().filterHasDecision();

        CharacterizedTypes characterizedTypes = projectData.characterizedTypes();
        return new DecisionAngles(methods, characterizedTypes);
    }

    /**
     * 真偽値を返すモデルのメソッドを分析する
     */
    public BoolQueryAngles boolQueryModelMethodAngle(ProjectData projectData) {
        CharacterizedMethods methods = projectData.characterizedMethods();
        MethodRelations relations = projectData.methodRelations();
        return BoolQueryAngles.of(methods, relations);
    }

    /**
     * コレクションを分析する
     */
    public CollectionAngles collectionAngles(ProjectData projectData) {
        TypeIdentifiers collectionTypeIdentifiers = projectData.valueTypes().extract(ValueKind.COLLECTION);
        return new CollectionAngles(collectionTypeIdentifiers,
                projectData.fieldDeclarations(),
                projectData.methods(),
                projectData.typeDependencies());
    }
}

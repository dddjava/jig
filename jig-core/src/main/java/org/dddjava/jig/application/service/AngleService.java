package org.dddjava.jig.application.service;

import org.dddjava.jig.annotation.incubation.Progress;
import org.dddjava.jig.domain.basic.Warning;
import org.dddjava.jig.domain.model.booleans.model.BoolQueryAngles;
import org.dddjava.jig.domain.model.categories.CategoryTypes;
import org.dddjava.jig.domain.model.categories.CategoryAngles;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.collections.CollectionAngles;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.method.Methods;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.progress.ProgressAngles;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.values.ValueAngles;
import org.dddjava.jig.domain.model.values.ValueKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 分析の切り口サービス
 */
@Progress("解体したい")
@Service
public class AngleService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AngleService.class);

    /**
     * コントローラーを分析する
     */
    public ControllerAngles controllerAngles(ProjectData projectData) {
        ProgressAngles progressAngles = progressAngles(projectData);
        return new ControllerAngles(projectData.controllerMethods(), projectData.typeAnnotations(), progressAngles);
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(ProjectData projectData) {
        MethodDeclarations serviceMethods = projectData.characterizedMethods().serviceMethods();
        ProgressAngles progressAngles = progressAngles(projectData);

        if (serviceMethods.empty()) {
            LOGGER.warn(Warning.サービス検出異常.text());
        }

        return new ServiceAngles(
                serviceMethods,
                projectData.methodRelations(),
                projectData.characterizedTypes(),
                projectData.methodUsingFields(),
                projectData.characterizedMethods(),
                progressAngles
        );
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

        return new DatasourceAngles(
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
        CategoryTypes categoryTypes = projectData.categories();

        return new CategoryAngles(categoryTypes,
                projectData.typeDependencies(),
                projectData.fieldDeclarations(),
                projectData.staticFieldDeclarations());
    }

    /**
     * 値を分析する
     */
    public ValueAngles valueAngles(ValueKind valueKind, ProjectData projectData) {
        return new ValueAngles(valueKind, projectData.valueTypes(), projectData.typeDependencies());
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingAngles stringComparing(ProjectData projectData) {
        return new StringComparingAngles(projectData.methodRelations());
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
        return new BoolQueryAngles(methods, relations);
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

    /**
     * 進捗を分析する
     */
    @Progress("実験的機能")
    public ProgressAngles progressAngles(ProjectData projectData) {
        MethodAnnotations methodAnnotations = projectData.methodAnnotations();
        TypeAnnotations typeAnnotations = projectData.typeAnnotations();
        MethodDeclarations declarations = projectData.methods().declarations();
        return new ProgressAngles(declarations, typeAnnotations, methodAnnotations);
    }
}

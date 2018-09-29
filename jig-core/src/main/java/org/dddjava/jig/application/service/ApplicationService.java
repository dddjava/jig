package org.dddjava.jig.application.service;

import org.dddjava.jig.annotation.Progress;
import org.dddjava.jig.domain.basic.Warning;
import org.dddjava.jig.domain.model.characteristic.CharacterizedMethods;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.unit.method.Methods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 機能の分析サービス
 */
@Progress("安定")
@Service
public class ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    /**
     * コントローラーを分析する
     */
    public ControllerAngles controllerAngles(ProjectData projectData) {
        return new ControllerAngles(projectData.controllerMethods(), projectData.typeAnnotations());
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(ProjectData projectData) {
        MethodDeclarations serviceMethods = projectData.characterizedMethods().serviceMethods();

        if (serviceMethods.empty()) {
            LOGGER.warn(Warning.サービス検出異常.text());
        }

        return new ServiceAngles(
                serviceMethods,
                projectData.methodRelations(),
                projectData.characterizedTypes(),
                projectData.methodUsingFields(),
                projectData.characterizedMethods()
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

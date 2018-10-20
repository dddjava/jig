package org.dddjava.jig.application.service;

import org.dddjava.jig.annotation.Progress;
import org.dddjava.jig.domain.basic.Warning;
import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceMethods;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.ProjectData;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.MethodUsingFields;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.services.ServiceMethods;
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

    Architecture architecture;

    public ApplicationService(Architecture architecture) {
        this.architecture = architecture;
    }

    /**
     * コントローラーを分析する
     */
    public ControllerAngles controllerAngles(ProjectData projectData) {
        TypeByteCodes typeByteCodes = projectData.typeByteCodes();
        ControllerMethods controllerMethods = new ControllerMethods(typeByteCodes, architecture);

        if (controllerMethods.empty()) {
            LOGGER.warn(Warning.コントローラーなし.text());
        }

        return new ControllerAngles(
                controllerMethods,
                typeByteCodes.typeAnnotations(),
                new MethodUsingFields(typeByteCodes));
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(ProjectData projectData) {
        TypeByteCodes typeByteCodes = projectData.typeByteCodes();
        ServiceMethods serviceMethods = new ServiceMethods(typeByteCodes, architecture);

        if (serviceMethods.empty()) {
            LOGGER.warn(Warning.サービスなし.text());
        }

        ControllerMethods controllerMethods = new ControllerMethods(typeByteCodes, architecture);
        DatasourceMethods datasourceMethods = new DatasourceMethods(typeByteCodes, architecture);

        return new ServiceAngles(
                serviceMethods,
                new MethodRelations(typeByteCodes),
                controllerMethods,
                datasourceMethods);
    }

    /**
     * データソースを分析する
     */
    public DatasourceAngles datasourceAngles(ProjectData projectData, Sqls sqls) {
        TypeByteCodes typeByteCodes = projectData.typeByteCodes();

        DatasourceMethods datasourceMethods = new DatasourceMethods(typeByteCodes, architecture);

        if (datasourceMethods.empty()) {
            LOGGER.warn(Warning.リポジトリなし.text());
        } else if (sqls.empty()) {
            LOGGER.warn(Warning.SQLなし.text());
        }

        return new DatasourceAngles(datasourceMethods, sqls);
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingAngles stringComparing(ProjectData projectData) {

        return new StringComparingAngles(new MethodRelations(projectData.typeByteCodes()));
    }

    /**
     * 分岐箇所を分析する
     */
    public DecisionAngles decision(ProjectData projectData) {
        return new DecisionAngles(projectData.typeByteCodes(), architecture);
    }

    /**
     * 進捗を分析する
     */
    @Progress("実験的機能")
    public ProgressAngles progressAngles(ProjectData projectData) {
        MethodAnnotations methodAnnotations = projectData.typeByteCodes().annotatedMethods();
        TypeAnnotations typeAnnotations = projectData.typeByteCodes().typeAnnotations();
        MethodDeclarations declarations = new Methods(projectData.typeByteCodes()).declarations();
        return new ProgressAngles(declarations, typeAnnotations, methodAnnotations);
    }
}

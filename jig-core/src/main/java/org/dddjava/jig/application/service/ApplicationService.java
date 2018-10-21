package org.dddjava.jig.application.service;

import org.dddjava.jig.annotation.Progress;
import org.dddjava.jig.domain.basic.Warning;
import org.dddjava.jig.domain.model.angle.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.angle.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.angle.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.angle.unit.method.Methods;
import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.networks.method.MethodRelations;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.threelayer.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.threelayer.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.threelayer.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.threelayer.datasources.DatasourceMethods;
import org.dddjava.jig.domain.model.threelayer.services.ServiceAngles;
import org.dddjava.jig.domain.model.threelayer.services.ServiceMethods;
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
    public ControllerAngles controllerAngles(TypeByteCodes typeByteCodes) {
        ControllerMethods controllerMethods = new ControllerMethods(typeByteCodes, architecture);

        if (controllerMethods.empty()) {
            LOGGER.warn(Warning.コントローラーなし.text());
        }

        return new ControllerAngles(controllerMethods, typeByteCodes.typeAnnotations());
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(TypeByteCodes typeByteCodes) {
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
    public DatasourceAngles datasourceAngles(TypeByteCodes typeByteCodes, Sqls sqls) {
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
    public StringComparingAngles stringComparing(TypeByteCodes typeByteCodes) {

        return new StringComparingAngles(new MethodRelations(typeByteCodes));
    }

    /**
     * 分岐箇所を分析する
     */
    public DecisionAngles decision(TypeByteCodes typeByteCodes) {
        return new DecisionAngles(typeByteCodes, architecture);
    }

    /**
     * 進捗を分析する
     */
    @Progress("実験的機能")
    public ProgressAngles progressAngles(TypeByteCodes typeByteCodes) {
        MethodAnnotations methodAnnotations = typeByteCodes.annotatedMethods();
        TypeAnnotations typeAnnotations = typeByteCodes.typeAnnotations();
        MethodDeclarations declarations = new Methods(typeByteCodes).declarations();
        return new ProgressAngles(declarations, typeAnnotations, methodAnnotations);
    }
}

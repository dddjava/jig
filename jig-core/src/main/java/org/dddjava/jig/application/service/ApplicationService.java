package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.controllers.ControllerAngles;
import org.dddjava.jig.domain.model.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.datasources.DatasourceAngles;
import org.dddjava.jig.domain.model.datasources.DatasourceMethods;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingAngles;
import org.dddjava.jig.domain.model.implementation.architecture.Architecture;
import org.dddjava.jig.domain.model.implementation.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.implementation.datasource.Sqls;
import org.dddjava.jig.domain.model.implementation.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.implementation.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.implementation.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.implementation.networks.method.MethodRelations;
import org.dddjava.jig.domain.model.implementation.unit.method.Methods;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.services.ServiceMethods;
import org.dddjava.jig.domain.type.Warning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 機能の分析サービス
 */
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
            LOGGER.warn(Warning.ハンドラメソッドが見つからないので出力されない通知.text());
        }

        return new ControllerAngles(controllerMethods, typeByteCodes.typeAnnotations());
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(TypeByteCodes typeByteCodes) {
        ServiceMethods serviceMethods = new ServiceMethods(typeByteCodes, architecture);

        if (serviceMethods.empty()) {
            LOGGER.warn(Warning.サービスメソッドが見つからないので出力されない通知.text());
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
            LOGGER.warn(Warning.リポジトリが見つからないので出力されない通知.text());
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
    public ProgressAngles progressAngles(TypeByteCodes typeByteCodes) {
        MethodAnnotations methodAnnotations = typeByteCodes.annotatedMethods();
        TypeAnnotations typeAnnotations = typeByteCodes.typeAnnotations();
        MethodDeclarations declarations = new Methods(typeByteCodes).declarations();
        return new ProgressAngles(declarations, typeAnnotations, methodAnnotations);
    }
}

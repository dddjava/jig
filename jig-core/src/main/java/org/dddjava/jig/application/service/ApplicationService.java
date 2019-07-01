package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.architecture.Architecture;
import org.dddjava.jig.domain.model.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.decisions.DecisionAngles;
import org.dddjava.jig.domain.model.decisions.StringComparingCallerMethods;
import org.dddjava.jig.domain.model.declaration.annotation.MethodAnnotations;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.fact.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.fact.bytecode.TypeByteCodes;
import org.dddjava.jig.domain.model.fact.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.notice.Warning;
import org.dddjava.jig.domain.model.progresses.ProgressAngles;
import org.dddjava.jig.domain.model.repositories.DatasourceAngles;
import org.dddjava.jig.domain.model.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.richmethod.Methods;
import org.dddjava.jig.domain.model.services.ServiceAngles;
import org.dddjava.jig.domain.model.services.ServiceMethods;
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
    public ControllerMethods controllerAngles(AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ControllerMethods controllerMethods = new ControllerMethods(typeByteCodes, architecture);

        if (controllerMethods.empty()) {
            LOGGER.warn(Warning.ハンドラメソッドが見つからないので出力されない通知.text());
        }

        return controllerMethods;
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
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
    public DatasourceAngles datasourceAngles(AnalyzedImplementation analyzedImplementation) {
        DatasourceMethods datasourceMethods = new DatasourceMethods(analyzedImplementation.typeByteCodes(), architecture);

        if (datasourceMethods.empty()) {
            LOGGER.warn(Warning.リポジトリが見つからないので出力されない通知.text());
        }

        return new DatasourceAngles(datasourceMethods, analyzedImplementation.sqls());
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingCallerMethods stringComparing(AnalyzedImplementation analyzedImplementation) {
        return new StringComparingCallerMethods(new MethodRelations(analyzedImplementation.typeByteCodes()));
    }

    /**
     * 分岐箇所を分析する
     */
    public DecisionAngles decision(AnalyzedImplementation analyzedImplementation) {
        return new DecisionAngles(analyzedImplementation.typeByteCodes(), architecture);
    }

    /**
     * 進捗を分析する
     */
    public ProgressAngles progressAngles(AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        MethodAnnotations methodAnnotations = typeByteCodes.annotatedMethods();
        TypeAnnotations typeAnnotations = typeByteCodes.typeAnnotations();
        MethodDeclarations declarations = new Methods(typeByteCodes).declarations();
        return new ProgressAngles(declarations, typeAnnotations, methodAnnotations);
    }
}

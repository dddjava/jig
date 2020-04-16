package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocument.JigLogger;
import org.dddjava.jig.domain.model.jigdocument.Warning;
import org.dddjava.jig.domain.model.jigloaded.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigmodel.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigmodel.applications.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.repositories.DatasourceAngles;
import org.dddjava.jig.domain.model.jigmodel.applications.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.architecture.Architecture;
import org.dddjava.jig.domain.model.jigmodel.smells.StringComparingCallerMethods;
import org.dddjava.jig.domain.model.jigpresentation.servicecall.ServiceMethodCallHierarchy;
import org.dddjava.jig.domain.model.jigsource.bytecode.TypeByteCodes;
import org.springframework.stereotype.Service;

/**
 * 機能の分析サービス
 */
@Service
public class ApplicationService {

    Architecture architecture;
    JigLogger jigLogger;

    public ApplicationService(Architecture architecture, JigLogger jigLogger) {
        this.architecture = architecture;
        this.jigLogger = jigLogger;
    }

    /**
     * コントローラーを分析する
     */
    public ControllerMethods controllerAngles(AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ControllerMethods controllerMethods = new ControllerMethods(typeByteCodes, architecture);

        if (controllerMethods.empty()) {
            jigLogger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知);
        }

        return controllerMethods;
    }

    public ServiceMethodCallHierarchy serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = serviceAngles(implementations);
        return new ServiceMethodCallHierarchy(serviceAngles.list());
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ServiceMethods serviceMethods = new ServiceMethods(typeByteCodes, architecture);

        if (serviceMethods.empty()) {
            jigLogger.warn(Warning.サービスメソッドが見つからないので出力されない通知);
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
            jigLogger.warn(Warning.リポジトリが見つからないので出力されない通知);
        }

        return new DatasourceAngles(datasourceMethods, analyzedImplementation.sqls());
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingCallerMethods stringComparing(AnalyzedImplementation analyzedImplementation) {
        return StringComparingCallerMethods.from(analyzedImplementation, architecture);
    }
}

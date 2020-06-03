package org.dddjava.jig.application.service;

import org.dddjava.jig.domain.model.jigdocumenter.diagram.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.jigdocumenter.stationery.JigLogger;
import org.dddjava.jig.domain.model.jigdocumenter.stationery.Warning;
import org.dddjava.jig.domain.model.jigmodel.architectures.RoundingPackageRelations;
import org.dddjava.jig.domain.model.jigmodel.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceAngles;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.services.ServiceMethods;
import org.dddjava.jig.domain.model.jigmodel.smells.StringComparingCallerMethods;
import org.dddjava.jig.domain.model.jigsource.file.binary.TypeByteCodes;
import org.dddjava.jig.domain.model.jigsource.jigloader.MethodFactory;
import org.dddjava.jig.domain.model.jigsource.jigloader.RelationsFactory;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.AnalyzedImplementation;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.Architecture;
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
        ControllerMethods controllerMethods = MethodFactory.createControllerMethods(typeByteCodes, architecture);

        if (controllerMethods.empty()) {
            jigLogger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知);
        }

        return controllerMethods;
    }

    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy(AnalyzedImplementation implementations) {
        ServiceAngles serviceAngles = serviceAngles(implementations);
        return new ServiceMethodCallHierarchyDiagram(serviceAngles.list());
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(AnalyzedImplementation analyzedImplementation) {
        TypeByteCodes typeByteCodes = analyzedImplementation.typeByteCodes();
        ServiceMethods serviceMethods = MethodFactory.createServiceMethods(typeByteCodes, architecture);

        if (serviceMethods.empty()) {
            jigLogger.warn(Warning.サービスメソッドが見つからないので出力されない通知);
        }

        ControllerMethods controllerMethods = MethodFactory.createControllerMethods(typeByteCodes, architecture);
        DatasourceMethods datasourceMethods = MethodFactory.createDatasourceMethods(typeByteCodes, architecture);

        return new ServiceAngles(
                serviceMethods,
                RelationsFactory.createMethodRelations(typeByteCodes),
                controllerMethods,
                datasourceMethods);
    }

    /**
     * データソースを分析する
     */
    public DatasourceAngles datasourceAngles(AnalyzedImplementation analyzedImplementation) {
        DatasourceMethods datasourceMethods = MethodFactory.createDatasourceMethods(analyzedImplementation.typeByteCodes(), architecture);

        if (datasourceMethods.empty()) {
            jigLogger.warn(Warning.リポジトリが見つからないので出力されない通知);
        }

        return new DatasourceAngles(datasourceMethods, analyzedImplementation.sqls());
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingCallerMethods stringComparing(AnalyzedImplementation analyzedImplementation) {
        return MethodFactory.from(analyzedImplementation, architecture);
    }

    public RoundingPackageRelations buildingBlockRelations(AnalyzedImplementation implementations) {
        ClassRelations classRelations = RelationsFactory.createClassRelations(implementations.typeByteCodes());

        return RoundingPackageRelations.getRoundingPackageRelations(classRelations);
    }
}

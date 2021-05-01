package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.jigdocument.implementation.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.jigdocument.implementation.StringComparingMethodList;
import org.dddjava.jig.domain.model.jigdocument.specification.ArchitectureDiagram;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigLogger;
import org.dddjava.jig.domain.model.jigdocument.stationery.Warning;
import org.dddjava.jig.domain.model.jigmodel.architectures.ArchitectureComponents;
import org.dddjava.jig.domain.model.jigmodel.presentations.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.jigobject.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.infrastructures.DatasourceAngles;
import org.dddjava.jig.domain.model.jigmodel.infrastructures.DatasourceMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.ServiceAngles;
import org.dddjava.jig.domain.model.jigmodel.applications.ServiceMethods;
import org.dddjava.jig.domain.model.jigsource.jigfactory.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigfactory.TypeFacts;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 機能の分析サービス
 */
@Service
public class ApplicationService {

    final Architecture architecture;
    final JigLogger jigLogger;
    final JigSourceRepository jigSourceRepository;

    public ApplicationService(Architecture architecture, JigLogger jigLogger, JigSourceRepository jigSourceRepository) {
        this.architecture = architecture;
        this.jigLogger = jigLogger;
        this.jigSourceRepository = jigSourceRepository;
    }

    /**
     * コントローラーを分析する
     */
    public ControllerMethods controllerAngles() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        ControllerMethods controllerMethods = typeFacts.createControllerMethods(architecture);

        if (controllerMethods.empty()) {
            jigLogger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知);
        }

        return controllerMethods;
    }

    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy() {
        ServiceAngles serviceAngles = serviceAngles();
        return new ServiceMethodCallHierarchyDiagram(serviceAngles.list());
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        ServiceMethods serviceMethods = new ServiceMethods(typeFacts.applicationMethodsOf(architecture));

        if (serviceMethods.empty()) {
            jigLogger.warn(Warning.サービスメソッドが見つからないので出力されない通知);
        }

        ControllerMethods controllerMethods = typeFacts.createControllerMethods(architecture);
        DatasourceMethods datasourceMethods = typeFacts.createDatasourceMethods(architecture);

        return new ServiceAngles(
                serviceMethods,
                typeFacts.toMethodRelations(),
                controllerMethods,
                datasourceMethods);
    }

    /**
     * データソースを分析する
     */
    public DatasourceAngles datasourceAngles() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        DatasourceMethods datasourceMethods = typeFacts.createDatasourceMethods(architecture);

        if (datasourceMethods.empty()) {
            jigLogger.warn(Warning.リポジトリが見つからないので出力されない通知);
        }

        MethodRelations methodRelations = typeFacts.toMethodRelations();
        return new DatasourceAngles(datasourceMethods, jigSourceRepository.sqls(), methodRelations);
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingMethodList stringComparing() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        ControllerMethods controllerMethods = typeFacts.createControllerMethods(architecture);
        ServiceMethods serviceMethods = new ServiceMethods(typeFacts.applicationMethodsOf(architecture));

        return StringComparingMethodList.createFrom(controllerMethods, serviceMethods);
    }

    public ArchitectureDiagram architectureDiagram() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        ArchitectureComponents architectureComponents = typeFacts.getArchitectureComponents();
        ClassRelations classRelations = typeFacts.toClassRelations();
        return new ArchitectureDiagram(architectureComponents, classRelations);
    }

    public Map<PackageIdentifier, List<JigType>> serviceTypes() {
        return jigSourceRepository.allTypeFacts().applicationTypes(architecture);
    }
}

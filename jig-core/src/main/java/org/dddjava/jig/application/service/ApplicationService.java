package org.dddjava.jig.application.service;

import org.dddjava.jig.application.repository.JigSourceRepository;
import org.dddjava.jig.domain.model.documents.diagrams.ArchitectureDiagram;
import org.dddjava.jig.domain.model.documents.diagrams.ServiceMethodCallHierarchyDiagram;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.models.applications.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.ServiceMethods;
import org.dddjava.jig.domain.model.models.applications.StringComparingMethodList;
import org.dddjava.jig.domain.model.models.architectures.PackageBasedArchitecture;
import org.dddjava.jig.domain.model.models.backends.DatasourceAngles;
import org.dddjava.jig.domain.model.models.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 機能の分析サービス
 */
@Service
public class ApplicationService {

    static Logger logger = LoggerFactory.getLogger(ApplicationService.class);
    final JigSourceRepository jigSourceRepository;

    public ApplicationService(JigSourceRepository jigSourceRepository) {
        this.jigSourceRepository = jigSourceRepository;
    }

    /**
     * コントローラーを分析する
     */
    public HandlerMethods controllerAngles() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        HandlerMethods handlerMethods = HandlerMethods.from(typeFacts.jigTypes());

        if (handlerMethods.empty()) {
            logger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知.localizedMessage());
        }

        return handlerMethods;
    }

    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy() {
        ServiceAngles serviceAngles = serviceAngles();
        return new ServiceMethodCallHierarchyDiagram(serviceAngles);
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        JigTypes jigTypes = typeFacts.jigTypes();
        ServiceMethods serviceMethods = ServiceMethods.from(jigTypes);

        if (serviceMethods.empty()) {
            logger.warn(Warning.サービスメソッドが見つからないので出力されない通知.localizedMessage());
        }

        HandlerMethods handlerMethods = HandlerMethods.from(jigTypes);
        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes);

        return new ServiceAngles(
                serviceMethods,
                typeFacts.toMethodRelations(),
                handlerMethods,
                datasourceMethods);
    }

    /**
     * データソースを分析する
     */
    public DatasourceAngles datasourceAngles() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        DatasourceMethods datasourceMethods = DatasourceMethods.from(typeFacts.jigTypes());

        if (datasourceMethods.empty()) {
            logger.warn(Warning.リポジトリが見つからないので出力されない通知.localizedMessage());
        }

        MethodRelations methodRelations = typeFacts.toMethodRelations();
        return new DatasourceAngles(datasourceMethods, jigSourceRepository.sqls(), methodRelations);
    }

    /**
     * 文字列比較を分析する
     */
    public StringComparingMethodList stringComparing() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        HandlerMethods handlerMethods = HandlerMethods.from(typeFacts.jigTypes());
        ServiceMethods serviceMethods = ServiceMethods.from(typeFacts.jigTypes());

        return StringComparingMethodList.createFrom(handlerMethods, serviceMethods);
    }

    public ArchitectureDiagram architectureDiagram() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        // TODO packageBasedArchitectureがjigTypeを受け取っているのでclassRelationを別に受け取らなくてもいけるはず
        PackageBasedArchitecture packageBasedArchitecture = PackageBasedArchitecture.from(typeFacts.jigTypes());
        ClassRelations classRelations = typeFacts.toClassRelations();
        return new ArchitectureDiagram(packageBasedArchitecture, classRelations);
    }

    public ServiceMethods serviceMethods() {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return ServiceMethods.from(typeFacts.jigTypes());
    }
}

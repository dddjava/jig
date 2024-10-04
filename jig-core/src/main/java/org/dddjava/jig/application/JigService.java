package org.dddjava.jig.application;

import org.dddjava.jig.domain.model.documents.diagrams.*;
import org.dddjava.jig.domain.model.documents.stationery.Warning;
import org.dddjava.jig.domain.model.models.applications.backends.DatasourceAngles;
import org.dddjava.jig.domain.model.models.applications.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.applications.frontends.Entrypoint;
import org.dddjava.jig.domain.model.models.applications.frontends.HandlerMethods;
import org.dddjava.jig.domain.model.models.applications.services.ServiceAngles;
import org.dddjava.jig.domain.model.models.applications.services.ServiceMethods;
import org.dddjava.jig.domain.model.models.applications.services.StringComparingMethodList;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.domains.businessrules.MethodSmellList;
import org.dddjava.jig.domain.model.models.domains.categories.CategoryTypes;
import org.dddjava.jig.domain.model.models.domains.categories.enums.EnumModels;
import org.dddjava.jig.domain.model.models.domains.collections.JigCollectionTypes;
import org.dddjava.jig.domain.model.models.jigobject.architectures.Architecture;
import org.dddjava.jig.domain.model.models.jigobject.architectures.PackageBasedArchitecture;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelations;
import org.dddjava.jig.domain.model.parts.term.Terms;
import org.dddjava.jig.domain.model.sources.jigfactory.TypeFacts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 機能の分析サービス
 */
@Service
public class JigService {

    static Logger logger = LoggerFactory.getLogger(JigService.class);
    private final Architecture architecture;
    private final JigSourceRepository jigSourceRepository;

    public JigService(Architecture architecture, JigSourceRepository jigSourceRepository) {
        this.architecture = architecture;
        this.jigSourceRepository = jigSourceRepository;
    }

    /**
     * ビジネスルール一覧を取得する
     */
    public BusinessRules businessRules(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return BusinessRules.from(architecture, typeFacts.toClassRelations(), typeFacts.jigTypes());
    }

    /**
     * メソッドの不吉なにおい一覧を取得する
     */
    public MethodSmellList methodSmells(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        MethodRelations methodRelations = typeFacts.toMethodRelations();
        return new MethodSmellList(businessRules(jigSource), methodRelations);
    }

    /**
     * 区分一覧を取得する
     */
    public CategoryDiagram categories(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        CategoryTypes categoryTypes = categoryTypes(jigSource);
        return CategoryDiagram.create(categoryTypes, typeFacts.toClassRelations());
    }

    public CategoryTypes categoryTypes(JigSource jigSource) {
        return CategoryTypes.from(businessRules(jigSource).jigTypes());
    }

    /**
     * コレクションを分析する
     */
    public JigCollectionTypes collections(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();

        return new JigCollectionTypes(businessRules(jigSource).jigTypes(), typeFacts.toClassRelations());
    }

    /**
     * 区分使用図
     */
    public CategoryUsageDiagram categoryUsages(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        JigTypes jigTypes = typeFacts.jigTypes();

        BusinessRules businessRules = BusinessRules.from(architecture, typeFacts.toClassRelations(), jigTypes);
        ServiceMethods serviceMethods = ServiceMethods.from(jigTypes, typeFacts.toMethodRelations());

        return new CategoryUsageDiagram(serviceMethods, businessRules);
    }

    public JigTypes jigTypes(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return typeFacts.jigTypes();
    }

    public Terms terms(JigSource jigSource) {
        return jigSourceRepository.terms();
    }

    public EnumModels enumModels(JigSource jigSource) {
        return jigSourceRepository.enumModels();
    }

    /**
     * コントローラーを分析する
     */
    public HandlerMethods controllerAngles(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        HandlerMethods handlerMethods = HandlerMethods.from(typeFacts.jigTypes());

        if (handlerMethods.empty()) {
            logger.warn(Warning.ハンドラメソッドが見つからないので出力されない通知.localizedMessage());
        }

        return handlerMethods;
    }

    public ServiceMethodCallHierarchyDiagram serviceMethodCallHierarchy(JigSource jigSource) {
        ServiceAngles serviceAngles = serviceAngles(jigSource);
        return new ServiceMethodCallHierarchyDiagram(serviceAngles);
    }

    /**
     * サービスを分析する
     */
    public ServiceAngles serviceAngles(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        JigTypes jigTypes = typeFacts.jigTypes();
        ServiceMethods serviceMethods = ServiceMethods.from(jigTypes, typeFacts.toMethodRelations());

        if (serviceMethods.empty()) {
            logger.warn(Warning.サービスメソッドが見つからないので出力されない通知.localizedMessage());
        }

        HandlerMethods handlerMethods = HandlerMethods.from(jigTypes);
        DatasourceMethods datasourceMethods = DatasourceMethods.from(jigTypes);

        return ServiceAngles.from(serviceMethods, handlerMethods, datasourceMethods);
    }

    /**
     * データソースを分析する
     */
    public DatasourceAngles datasourceAngles(JigSource jigSource) {
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
    public StringComparingMethodList stringComparing(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        HandlerMethods handlerMethods = HandlerMethods.from(typeFacts.jigTypes());
        ServiceMethods serviceMethods = ServiceMethods.from(typeFacts.jigTypes(), typeFacts.toMethodRelations());

        return StringComparingMethodList.createFrom(handlerMethods, serviceMethods);
    }

    public ArchitectureDiagram architectureDiagram(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        // TODO packageBasedArchitectureがjigTypeを受け取っているのでclassRelationを別に受け取らなくてもいけるはず
        PackageBasedArchitecture packageBasedArchitecture = PackageBasedArchitecture.from(typeFacts.jigTypes());
        ClassRelations classRelations = typeFacts.toClassRelations();
        return new ArchitectureDiagram(packageBasedArchitecture, classRelations);
    }

    public ServiceMethods serviceMethods(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return ServiceMethods.from(typeFacts.jigTypes(), typeFacts.toMethodRelations());
    }

    public Entrypoint entrypoint(JigSource jigSource) {
        TypeFacts typeFacts = jigSourceRepository.allTypeFacts();
        return new Entrypoint(
                typeFacts.jigTypes(),
                ServiceMethods.from(typeFacts.jigTypes(), typeFacts.toMethodRelations()));
    }

    /**
     * パッケージの関連を取得する
     */
    public PackageRelationDiagram packageDependencies(JigSource jigSource) {
        BusinessRules businessRules = businessRules(jigSource);

        if (businessRules.empty()) {
            logger.warn(Warning.ビジネスルールが見つからないので出力されない通知.localizedMessage());
            return PackageRelationDiagram.empty();
        }

        return new PackageRelationDiagram(businessRules.identifiers().packageIdentifiers(), businessRules.classRelations());
    }
}

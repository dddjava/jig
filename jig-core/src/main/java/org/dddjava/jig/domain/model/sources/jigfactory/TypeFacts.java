package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.backends.DatasourceMethod;
import org.dddjava.jig.domain.model.models.backends.DatasourceMethods;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.models.domains.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.models.jigobject.class_.JigTypes;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.classes.type.ClassComment;
import org.dddjava.jig.domain.model.parts.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.packages.PackageComment;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelation;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelations;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること一覧
 */
public class TypeFacts {
    private final List<TypeFact> list;

    public TypeFacts(List<TypeFact> list) {
        this.list = list;
    }

    private ClassRelations classRelations;
    private MethodRelations methodRelations;

    private JigTypes jigTypes;

    public JigTypes jigTypes() {
        if (jigTypes != null) return jigTypes;
        jigTypes = new JigTypes(list.stream().map(TypeFact::jigType).collect(toList()));
        return jigTypes;
    }

    public BusinessRules toBusinessRules(Architecture architecture) {
        List<BusinessRule> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            if (architecture.isBusinessRule(typeFact)) {
                list.add(new BusinessRule(typeFact.jigType()));
            }
        }
        return new BusinessRules(list, toClassRelations());
    }

    public DatasourceMethods createDatasourceMethods(Architecture architecture) {
        List<DatasourceMethod> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            if (architecture.isRepositoryImplementation(typeFact)) {
                for (ParameterizedType interfaceType : typeFact.interfaceTypes()) {
                    TypeIdentifier interfaceTypeIdentifier = interfaceType.typeIdentifier();
                    selectByTypeIdentifier(interfaceTypeIdentifier).ifPresent(interfaceTypeFact -> {
                        for (MethodFact interfaceMethodFact : interfaceTypeFact.instanceMethodFacts()) {
                            typeFact.instanceMethodFacts().stream()
                                    .filter(datasourceMethodByteCode -> interfaceMethodFact.sameSignature(datasourceMethodByteCode))
                                    // 0 or 1
                                    .forEach(concreteMethodByteCode -> list.add(new DatasourceMethod(
                                            interfaceMethodFact.createMethod(),
                                            concreteMethodByteCode.createMethod(),
                                            concreteMethodByteCode.methodDepend().usingMethods().methodDeclarations()))
                                    );
                        }
                    });
                }
            }
        }
        return new DatasourceMethods(list);
    }

    public synchronized MethodRelations toMethodRelations() {
        if (methodRelations != null) {
            return methodRelations;
        }
        List<MethodRelation> collector = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            for (MethodFact methodFact : typeFact.allMethodFacts()) {
                methodFact.collectUsingMethodRelations(collector);
            }
        }
        return methodRelations = new MethodRelations(collector);
    }

    public synchronized ClassRelations toClassRelations() {
        if (classRelations != null) {
            return classRelations;
        }
        List<ClassRelation> collector = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            typeFact.collectClassRelations(collector);
        }
        return classRelations = new ClassRelations(collector);
    }

    public List<TypeFact> list() {
        return list;
    }

    public List<MethodFact> instanceMethodFacts() {
        return list.stream()
                .map(TypeFact::instanceMethodFacts)
                .flatMap(List::stream)
                .collect(toList());
    }

    public Optional<TypeFact> selectByTypeIdentifier(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(typeFact -> typeIdentifier.equals(typeFact.typeIdentifier()))
                .findAny();
    }

    public void registerPackageAlias(PackageComment packageComment) {
        // TODO Packageを取得した際にくっつけて返せるようにする
    }

    public AliasRegisterResult registerTypeAlias(ClassComment classComment) {
        for (TypeFact typeFact : list) {
            if (typeFact.typeIdentifier().equals(classComment.typeIdentifier())) {
                typeFact.registerTypeAlias(classComment);
                return AliasRegisterResult.成功;
            }
        }

        return AliasRegisterResult.紐付け対象なし;
    }

    public AliasRegisterResult registerMethodAlias(MethodComment methodComment) {
        for (TypeFact typeFact : list) {
            MethodIdentifier methodIdentifier = methodComment.methodIdentifier();
            if (typeFact.typeIdentifier().equals(methodIdentifier.declaringType())) {
                return typeFact.registerMethodAlias(methodComment);
            }
        }

        return AliasRegisterResult.紐付け対象なし;
    }
}

package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.PackageAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.*;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.ParameterizedType;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Method;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.RequestHandlerMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.ApplicationLayer;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.Architecture;
import org.dddjava.jig.domain.model.jigsource.jigloader.architecture.BuildingBlock;

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

    public BusinessRules from(Architecture architecture) {
        List<BusinessRule> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            if (BuildingBlock.BUSINESS_RULE.satisfy(typeFact, architecture)) {
                list.add(typeFact.createBusinessRule());
            }
        }
        return new BusinessRules(list);
    }

    public ControllerMethods createControllerMethods(Architecture architecture) {
        List<RequestHandlerMethod> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            if (ApplicationLayer.PRESENTATION.satisfy(typeFact, architecture)) {
                for (MethodFact methodFact : typeFact.instanceMethodFacts()) {
                    Method method = methodFact.createMethod();
                    RequestHandlerMethod requestHandlerMethod = new RequestHandlerMethod(method, new Annotations(typeFact.listAnnotations()));
                    if (requestHandlerMethod.valid()) {
                        list.add(requestHandlerMethod);
                    }
                }
            }
        }
        return new ControllerMethods(list);
    }

    public DatasourceMethods createDatasourceMethods(Architecture architecture) {
        List<DatasourceMethod> list = new ArrayList<>();
        List<TypeFact> datasourceFacts = list().stream()
                .filter(typeFact1 -> BuildingBlock.DATASOURCE.satisfy(typeFact1, architecture))
                .collect(toList());
        for (TypeFact typeFact : datasourceFacts) {
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
        return new DatasourceMethods(list);
    }

    public List<Method> applicationMethodsOf(Architecture architecture) {
        return list().stream()
                .filter(typeFact -> ApplicationLayer.APPLICATION.satisfy(typeFact, architecture))
                .map(TypeFact::instanceMethodFacts)
                .flatMap(List::stream)
                .map(methodFact -> methodFact.createMethod())
                .collect(toList());
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

    public FieldAnnotations annotatedFields() {
        List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            fieldAnnotations.addAll(typeFact.annotatedFields());
        }
        return new FieldAnnotations(fieldAnnotations);
    }

    public MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = new ArrayList<>();
        for (MethodFact methodFact : instanceMethodFacts()) {
            methodAnnotations.addAll(methodFact.annotatedMethods().list());
        }
        return new MethodAnnotations(methodAnnotations);
    }

    public FieldDeclarations instanceFields() {
        List<FieldDeclaration> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            FieldDeclarations fieldDeclarations = typeFact.fieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new FieldDeclarations(list);
    }

    public StaticFieldDeclarations staticFields() {
        List<StaticFieldDeclaration> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            StaticFieldDeclarations fieldDeclarations = typeFact.staticFieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new StaticFieldDeclarations(list);
    }

    public ValidationAnnotatedMembers validationAnnotatedMembers() {
        return new ValidationAnnotatedMembers(annotatedFields(), annotatedMethods());
    }

    public Optional<TypeFact> selectByTypeIdentifier(TypeIdentifier typeIdentifier) {
        return list.stream()
                .filter(typeFact -> typeIdentifier.equals(typeFact.typeIdentifier()))
                .findAny();
    }

    public void registerPackageAlias(PackageAlias packageAlias) {
        // TODO Packageを取得した際にくっつけて返せるようにする
    }

    public void registerTypeAlias(TypeAlias typeAlias) {
        for (TypeFact typeFact : list) {
            if (typeFact.typeIdentifier().equals(typeAlias.typeIdentifier())) {
                typeFact.registerTypeAlias(typeAlias);
                return;
            }
        }

        // TODO: WARN 予期しない型のAliasが登録されました。このAliasは使用されません。
    }

    public void registerMethodAlias(MethodAlias methodAlias) {
        // TODO Methodを取得した際にくっつけて返せるようにする
        for (TypeFact typeFact : list) {
            MethodIdentifier methodIdentifier = methodAlias.methodIdentifier();
            if (typeFact.typeIdentifier().equals(methodIdentifier.declaringType())) {
                typeFact.registerMethodAlias(methodAlias);
                return;
            }
        }
        // TODO: WARN 予期しない型のAliasが登録されました。このAliasは使用されません。
    }
}

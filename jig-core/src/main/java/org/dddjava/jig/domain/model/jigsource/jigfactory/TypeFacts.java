package org.dddjava.jig.domain.model.jigsource.jigfactory;

import org.dddjava.jig.domain.model.jigmodel.architecture.ArchitectureComponents;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRules;
import org.dddjava.jig.domain.model.jigmodel.controllers.ControllerMethods;
import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.RequestHandlerMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethod;
import org.dddjava.jig.domain.model.jigmodel.repositories.DatasourceMethods;
import org.dddjava.jig.domain.model.parts.alias.MethodAlias;
import org.dddjava.jig.domain.model.parts.alias.PackageAlias;
import org.dddjava.jig.domain.model.parts.alias.TypeAlias;
import org.dddjava.jig.domain.model.parts.annotation.*;
import org.dddjava.jig.domain.model.parts.class_.method.MethodIdentifier;
import org.dddjava.jig.domain.model.parts.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelation;
import org.dddjava.jig.domain.model.parts.relation.method.MethodRelations;
import org.dddjava.jig.domain.model.parts.class_.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

import java.util.*;

import static java.util.stream.Collectors.*;

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
    private Map<PackageIdentifier, List<JigType>> packageMap;

    public List<JigType> listJigTypes() {
        return list.stream().map(TypeFact::jigType).collect(toList());
    }

    public Map<PackageIdentifier, List<JigType>> mapJigTypesByPackage() {
        if (packageMap != null) return packageMap;
        packageMap = listJigTypes().stream()
                .collect(groupingBy(JigType::packageIdentifier));
        return packageMap;
    }

    public ArchitectureComponents getArchitectureComponents() {
        return new ArchitectureComponents(getArchitecturePackages());
    }

    private List<PackageIdentifier> getArchitecturePackages() {
        Map<PackageIdentifier, List<JigType>> packageIdentifierListMap = mapJigTypesByPackage();
        // depth単位にリストにする
        Map<Integer, List<PackageIdentifier>> depthMap = packageIdentifierListMap.keySet().stream()
                .flatMap(packageIdentifier -> packageIdentifier.genealogical().stream())
                .sorted(Comparator.comparing(PackageIdentifier::asText))
                .distinct()
                .collect(groupingBy(packageIdentifier -> packageIdentifier.depth().value()));

        // 最初に同じ深さに2件以上入っているものが出てきたらアーキテクチャパッケージとして扱う
        List<PackageIdentifier> packages = depthMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(Collections.emptyList());
        return packages;
    }

    public BusinessRules toBusinessRules(Architecture architecture) {
        List<BusinessRule> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            if (architecture.isBusinessRule(typeFact)) {
                list.add(typeFact.createBusinessRule());
            }
        }
        return new BusinessRules(list, toClassRelations());
    }

    public ControllerMethods createControllerMethods(Architecture architecture) {
        List<RequestHandlerMethod> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            if (architecture.isController(typeFact)) {
                for (MethodFact methodFact : typeFact.instanceMethodFacts()) {
                    JigMethod method = methodFact.createMethod();
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

    public List<JigMethod> applicationMethodsOf(Architecture architecture) {
        return list().stream()
                .filter(typeFact -> architecture.isService(typeFact))
                .map(TypeFact::instanceMethodFacts)
                .flatMap(List::stream)
                .map(methodFact -> methodFact.createMethod())
                .collect(toList());
    }

    public Map<PackageIdentifier, List<JigType>> applicationTypes(Architecture architecture) {
        Set<PackageIdentifier> servicePackages = list().stream()
                .filter(typeFact -> architecture.isService(typeFact))
                .map(TypeFact::typeIdentifier)
                .map(TypeIdentifier::packageIdentifier)
                .collect(toSet());
        HashMap<PackageIdentifier, List<JigType>> packageMap = new HashMap<>();
        for (Map.Entry<PackageIdentifier, List<JigType>> entry : mapJigTypesByPackage().entrySet()) {
            if (servicePackages.contains(entry.getKey())) {
                packageMap.put(entry.getKey(), entry.getValue());
            }
        }
        return packageMap;
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

    public AliasRegisterResult registerTypeAlias(TypeAlias typeAlias) {
        for (TypeFact typeFact : list) {
            if (typeFact.typeIdentifier().equals(typeAlias.typeIdentifier())) {
                typeFact.registerTypeAlias(typeAlias);
                return AliasRegisterResult.成功;
            }
        }

        return AliasRegisterResult.紐付け対象なし;
    }

    public AliasRegisterResult registerMethodAlias(MethodAlias methodAlias) {
        for (TypeFact typeFact : list) {
            MethodIdentifier methodIdentifier = methodAlias.methodIdentifier();
            if (typeFact.typeIdentifier().equals(methodIdentifier.declaringType())) {
                return typeFact.registerMethodAlias(methodAlias);
            }
        }

        return AliasRegisterResult.紐付け対象なし;
    }
}

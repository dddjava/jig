package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.jigtype.JigInstanceMember;
import org.dddjava.jig.domain.model.jigmodel.jigtype.JigType;
import org.dddjava.jig.domain.model.jigmodel.jigtype.JigTypeMember;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.*;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.richmethod.Methods;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること
 */
public class TypeFact {

    final ParameterizedType type;

    final ParameterizedType superType;
    final List<ParameterizedType> interfaceTypes;

    final List<Annotation> annotations = new ArrayList<>();
    final List<StaticFieldDeclaration> staticFieldDeclarations = new ArrayList<>();

    final List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
    final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    final List<MethodFact> instanceMethodFacts = new ArrayList<>();
    final List<MethodFact> staticMethodFacts = new ArrayList<>();
    final List<MethodFact> constructorFacts = new ArrayList<>();

    final Set<TypeIdentifier> useTypes = new HashSet<>();
    final TypeKind typeKind;

    final Visibility visibility;

    TypeAlias typeAlias;

    public TypeFact(ParameterizedType type,
                    ParameterizedType superType,
                    List<ParameterizedType> interfaceTypes,
                    TypeKind typeKind,
                    Visibility visibility) {
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        this.typeKind = typeKind;
        this.visibility = visibility;

        for (TypeParameter typeParameter : type.typeParameters().list()) {
            this.useTypes.add(typeParameter.typeIdentifier());
        }
        this.useTypes.add(superType.typeIdentifier());
        for (ParameterizedType interfaceType : interfaceTypes) {
            this.useTypes.add(interfaceType.typeIdentifier());
        }

        this.typeAlias = TypeAlias.empty(type.typeIdentifier());
    }

    public TypeIdentifier typeIdentifier() {
        return type.typeIdentifier();
    }

    public FieldDeclarations fieldDeclarations() {
        return new FieldDeclarations(fieldDeclarations);
    }

    public StaticFieldDeclarations staticFieldDeclarations() {
        return new StaticFieldDeclarations(staticFieldDeclarations);
    }

    public TypeIdentifiers useTypes() {
        for (MethodFact methodFact : allMethodFacts()) {
            useTypes.addAll(methodFact.methodDepend().collectUsingTypes());
        }

        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<MethodFact> instanceMethodFacts() {
        return instanceMethodFacts;
    }

    public List<Annotation> listAnnotations() {
        return annotations;
    }

    public List<FieldAnnotation> annotatedFields() {
        return fieldAnnotations;
    }

    public void registerAnnotation(Annotation annotation) {
        annotations.add(annotation);
        useTypes.add(annotation.typeIdentifier());
    }

    public void registerField(FieldDeclaration field) {
        fieldDeclarations.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void registerStaticField(StaticFieldDeclaration field) {
        staticFieldDeclarations.add(field);
        useTypes.add(field.typeIdentifier());
    }

    public void registerUseType(TypeIdentifier typeIdentifier) {
        useTypes.add(typeIdentifier);
    }

    public void registerFieldAnnotation(FieldAnnotation fieldAnnotation) {
        fieldAnnotations.add(fieldAnnotation);
    }

    public void registerInstanceMethodFacts(MethodFact methodFact) {
        instanceMethodFacts.add(methodFact);
    }

    public void registerStaticMethodFacts(MethodFact methodFact) {
        staticMethodFacts.add(methodFact);
    }

    public void registerConstructorFacts(MethodFact methodFact) {
        constructorFacts.add(methodFact);
    }

    public List<MethodFact> allMethodFacts() {
        ArrayList<MethodFact> list = new ArrayList<>();
        list.addAll(instanceMethodFacts);
        list.addAll(staticMethodFacts);
        list.addAll(constructorFacts);
        return list;
    }

    public ParameterizedType superType() {
        return superType;
    }

    public List<ParameterizedType> interfaceTypes() {
        return interfaceTypes;
    }

    void collectClassRelations(List<ClassRelation> collector) {
        TypeIdentifier form = typeIdentifier();
        for (TypeIdentifier to : useTypes().list()) {
            ClassRelation classRelation = new ClassRelation(form, to);
            if (classRelation.selfRelation()) continue;
            collector.add(classRelation);
        }
    }

    public void registerTypeAlias(TypeAlias typeAlias) {
        this.typeAlias = typeAlias;
    }

    public AliasRegisterResult registerMethodAlias(MethodAlias methodAlias) {
        boolean registered = false;
        for (MethodFact methodFact : allMethodFacts()) {
            if (methodAlias.isAliasFor(methodFact.methodIdentifier())) {
                methodFact.registerMethodAlias(methodAlias);
                registered = true;
            }
        }
        return registered ? AliasRegisterResult.成功 : AliasRegisterResult.紐付け対象なし;
    }

    public String aliasText() {
        return typeAlias.asText();
    }

    public BusinessRule createBusinessRule() {
        return new BusinessRule(
                jigType(),
                jigInstanceMember(),
                jigTypeMember());
    }

    private JigTypeMember jigTypeMember() {
        Methods constructors = new Methods(constructorFacts.stream().map(MethodFact::createMethod).collect(toList()));
        Methods staticMethods = new Methods(staticMethodFacts.stream().map(MethodFact::createMethod).collect(toList()));
        return new JigTypeMember(constructors, staticMethods, staticFieldDeclarations());
    }

    private JigInstanceMember jigInstanceMember() {
        Methods instanceMethods = new Methods(instanceMethodFacts.stream().map(MethodFact::createMethod).collect(toList()));
        return new JigInstanceMember(fieldDeclarations(), instanceMethods);
    }

    private JigType jigType() {
        TypeDeclaration typeDeclaration = new TypeDeclaration(type, superType, new ParameterizedTypes(interfaceTypes));
        return new JigType(typeDeclaration, typeAlias, typeKind, visibility);
    }
}

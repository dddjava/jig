package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.jigobject.class_.*;
import org.dddjava.jig.domain.model.models.jigobject.member.JigField;
import org.dddjava.jig.domain.model.models.jigobject.member.JigFields;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.*;
import org.dddjava.jig.domain.model.parts.classes.type.ClassRelation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること
 */
public class JigTypeBuilder {

    final ParameterizedType type;

    final ParameterizedType superType;
    final List<ParameterizedType> interfaceTypes;

    final List<Annotation> annotations;
    final List<StaticFieldDeclaration> staticFieldDeclarations;

    final List<JigField> instanceFields;

    final List<JigMethodBuilder> instanceJigMethodBuilders;
    final List<JigMethodBuilder> staticJigMethodBuilders;
    final List<JigMethodBuilder> constructorFacts;

    final List<TypeIdentifier> usingTypes;

    final Set<TypeIdentifier> useTypes = new HashSet<>();
    final TypeKind typeKind;

    final Visibility visibility;

    ClassComment classComment;

    public JigTypeBuilder(ParameterizedType type, ParameterizedType superType, List<ParameterizedType> interfaceTypes,
                          TypeKind typeKind, Visibility visibility,
                          List<Annotation> annotations,
                          List<JigMethodBuilder> instanceJigMethodBuilders,
                          List<JigMethodBuilder> staticJigMethodBuilders,
                          List<JigMethodBuilder> constructorFacts,
                          List<JigField> instanceFields,
                          List<StaticFieldDeclaration> staticFieldDeclarations,
                          List<TypeIdentifier> usingTypes) {
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        this.typeKind = typeKind;
        this.visibility = visibility;
        this.annotations = annotations;
        this.instanceJigMethodBuilders = instanceJigMethodBuilders;
        this.staticJigMethodBuilders = staticJigMethodBuilders;
        this.constructorFacts = constructorFacts;
        this.instanceFields = instanceFields;
        this.staticFieldDeclarations = staticFieldDeclarations;

        this.usingTypes = usingTypes;
        // TODO useTypes廃止したい。JigType.usingTypes()でいけるはず。
        this.useTypes.addAll(usingTypes);
        this.useTypes.addAll(type.typeParameters().list());
        this.useTypes.add(superType.typeIdentifier());
        for (ParameterizedType interfaceType : interfaceTypes) {
            this.useTypes.add(interfaceType.typeIdentifier());
        }
        this.annotations.forEach(e -> this.useTypes.add(e.typeIdentifier()));
        this.instanceFields.forEach(e -> this.useTypes.add(e.fieldDeclaration().typeIdentifier()));
        this.staticFieldDeclarations.forEach(e -> this.useTypes.add(e.typeIdentifier()));

        this.classComment = ClassComment.empty(type.typeIdentifier());
    }

    public TypeIdentifier typeIdentifier() {
        return type.typeIdentifier();
    }

    public TypeIdentifiers useTypes() {
        for (JigMethodBuilder jigMethodBuilder : allMethodFacts()) {
            useTypes.addAll(jigMethodBuilder.methodDepend().collectUsingTypes());
        }

        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<JigMethodBuilder> instanceMethodFacts() {
        return instanceJigMethodBuilders;
    }

    public List<Annotation> listAnnotations() {
        return annotations;
    }

    public List<JigMethodBuilder> allMethodFacts() {
        ArrayList<JigMethodBuilder> list = new ArrayList<>();
        list.addAll(instanceJigMethodBuilders);
        list.addAll(staticJigMethodBuilders);
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

    public void registerTypeAlias(ClassComment classComment) {
        this.classComment = classComment;
        this.jigType = null;
    }

    public AliasRegisterResult registerMethodAlias(MethodComment methodComment) {
        boolean registered = false;
        for (JigMethodBuilder jigMethodBuilder : allMethodFacts()) {
            if (methodComment.isAliasFor(jigMethodBuilder.methodIdentifier())) {
                jigMethodBuilder.registerMethodAlias(methodComment);
                registered = true;
            }
        }
        return registered ? AliasRegisterResult.成功 : AliasRegisterResult.紐付け対象なし;
    }

    JigType jigType;

    public JigType build() {
        if (jigType != null) return jigType;

        TypeDeclaration typeDeclaration = new TypeDeclaration(type, superType, new ParameterizedTypes(interfaceTypes));

        JigTypeAttribute jigTypeAttribute = new JigTypeAttribute(classComment, typeKind, visibility, annotations);

        JigMethods constructors = new JigMethods(constructorFacts.stream().map(JigMethodBuilder::build).collect(toList()));
        JigMethods staticMethods = new JigMethods(staticJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList()));
        StaticFieldDeclarations staticFieldDeclarations = new StaticFieldDeclarations(this.staticFieldDeclarations);
        JigStaticMember jigStaticMember = new JigStaticMember(constructors, staticMethods, staticFieldDeclarations);

        JigMethods instanceMethods = new JigMethods(instanceJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList()));
        JigInstanceMember jigInstanceMember = new JigInstanceMember(new JigFields(instanceFields), instanceMethods);

        jigType = new JigType(typeDeclaration, jigTypeAttribute, jigStaticMember, jigInstanceMember, usingTypes);
        return jigType;
    }
}

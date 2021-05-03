package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.jigobject.class_.*;
import org.dddjava.jig.domain.model.models.jigobject.member.JigField;
import org.dddjava.jig.domain.model.models.jigobject.member.JigFields;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.class_.field.FieldDeclarations;
import org.dddjava.jig.domain.model.parts.class_.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.parts.class_.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.parts.class_.method.MethodComment;
import org.dddjava.jig.domain.model.parts.class_.method.Visibility;
import org.dddjava.jig.domain.model.parts.class_.type.*;
import org.dddjava.jig.domain.model.parts.relation.class_.ClassRelation;

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

    final List<Annotation> annotations;
    final List<StaticFieldDeclaration> staticFieldDeclarations;

    final List<JigField> instanceFields;

    final List<MethodFact> instanceMethodFacts;
    final List<MethodFact> staticMethodFacts;
    final List<MethodFact> constructorFacts;

    final List<TypeIdentifier> usingTypes;

    final Set<TypeIdentifier> useTypes = new HashSet<>();
    final TypeKind typeKind;

    final Visibility visibility;

    ClassComment classComment;

    public TypeFact(ParameterizedType type, ParameterizedType superType, List<ParameterizedType> interfaceTypes,
                    TypeKind typeKind, Visibility visibility,
                    List<Annotation> annotations,
                    List<MethodFact> instanceMethodFacts,
                    List<MethodFact> staticMethodFacts,
                    List<MethodFact> constructorFacts,
                    List<JigField> instanceFields,
                    List<StaticFieldDeclaration> staticFieldDeclarations,
                    List<TypeIdentifier> usingTypes) {
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        this.typeKind = typeKind;
        this.visibility = visibility;
        this.annotations = annotations;
        this.instanceMethodFacts = instanceMethodFacts;
        this.staticMethodFacts = staticMethodFacts;
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

    // TODO テスト専用、削除したい
    public FieldDeclarations fieldDeclarations() {
        return new FieldDeclarations(instanceFields.stream().map(JigField::fieldDeclaration).collect(toList()));
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

    public void registerTypeAlias(ClassComment classComment) {
        this.classComment = classComment;
        this.jigType = null;
    }

    public AliasRegisterResult registerMethodAlias(MethodComment methodComment) {
        boolean registered = false;
        for (MethodFact methodFact : allMethodFacts()) {
            if (methodComment.isAliasFor(methodFact.methodIdentifier())) {
                methodFact.registerMethodAlias(methodComment);
                registered = true;
            }
        }
        return registered ? AliasRegisterResult.成功 : AliasRegisterResult.紐付け対象なし;
    }

    JigType jigType;

    public JigType jigType() {
        if (jigType != null) return jigType;

        TypeDeclaration typeDeclaration = new TypeDeclaration(type, superType, new ParameterizedTypes(interfaceTypes));

        JigTypeAttribute jigTypeAttribute = new JigTypeAttribute(classComment, typeKind, visibility, annotations);

        JigMethods constructors = new JigMethods(constructorFacts.stream().map(MethodFact::createMethod).collect(toList()));
        JigMethods staticMethods = new JigMethods(staticMethodFacts.stream().map(MethodFact::createMethod).collect(toList()));
        StaticFieldDeclarations staticFieldDeclarations = new StaticFieldDeclarations(this.staticFieldDeclarations);
        JigStaticMember jigStaticMember = new JigStaticMember(constructors, staticMethods, staticFieldDeclarations);

        JigMethods instanceMethods = new JigMethods(instanceMethodFacts.stream().map(MethodFact::createMethod).collect(toList()));
        JigInstanceMember jigInstanceMember = new JigInstanceMember(new JigFields(instanceFields), instanceMethods);

        jigType = new JigType(typeDeclaration, jigTypeAttribute, jigStaticMember, jigInstanceMember, usingTypes);
        return jigType;
    }
}

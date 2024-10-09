package org.dddjava.jig.domain.model.sources.jigfactory;

import org.dddjava.jig.domain.model.models.jigobject.class_.*;
import org.dddjava.jig.domain.model.models.jigobject.member.JigField;
import org.dddjava.jig.domain.model.models.jigobject.member.JigFields;
import org.dddjava.jig.domain.model.models.jigobject.member.JigMethods;
import org.dddjava.jig.domain.model.parts.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.field.FieldType;
import org.dddjava.jig.domain.model.parts.classes.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.parts.classes.method.MethodComment;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること
 */
public class JigTypeBuilder {

    ParameterizedType type;
    ParameterizedType superType;
    List<ParameterizedType> interfaceTypes;
    TypeKind typeKind;
    Visibility visibility;

    final List<Annotation> annotations;

    final List<StaticFieldDeclaration> staticFieldDeclarations;
    final List<JigField> instanceFields;
    final List<JigMethodBuilder> instanceJigMethodBuilders;
    final List<JigMethodBuilder> staticJigMethodBuilders;
    final List<JigMethodBuilder> constructorFacts;

    ClassComment classComment;

    public JigTypeBuilder() {
        this.type = null;
        this.superType = null;
        this.interfaceTypes = null;
        this.typeKind = null;
        this.visibility = null;
        this.annotations = new ArrayList<>();
        this.instanceJigMethodBuilders = new ArrayList<>();
        this.staticJigMethodBuilders = new ArrayList<>();
        this.constructorFacts = new ArrayList<>();
        this.instanceFields = new ArrayList<>();
        this.staticFieldDeclarations = new ArrayList<>();
    }

    public TypeIdentifier typeIdentifier() {
        return type.typeIdentifier();
    }

    public List<JigMethodBuilder> instanceJigMethodBuilders() {
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

    public void registerTypeAlias(ClassComment classComment) {
        this.classComment = classComment;
        this.jigType = null;
    }

    public boolean registerMethodAlias(MethodComment methodComment) {
        boolean registered = false;
        for (JigMethodBuilder jigMethodBuilder : allMethodFacts()) {
            if (methodComment.isAliasFor(jigMethodBuilder.methodIdentifier())) {
                jigMethodBuilder.registerMethodAlias(methodComment);
                // オーバーロードの正確な識別ができないので、同じ名前のメソッドすべてに適用するため、ここでreturnしてはいけない
                registered = true;
            }
        }
        return registered;
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

        jigType = new JigType(typeDeclaration, jigTypeAttribute, jigStaticMember, jigInstanceMember);
        return jigType;
    }

    public void applyTextSource(TextSourceModel textSourceModel) {
        for (JigMethodBuilder jigMethodBuilder : allMethodFacts()) {
            jigMethodBuilder.applyTextSource(textSourceModel);
        }
    }

    /**
     * ヘッダから取得できる情報を適用する
     */
    public void setHeaders(ParameterizedType type, ParameterizedType superType, List<ParameterizedType> interfaceTypes, Visibility visibility, TypeKind typeKind) {
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        this.visibility = visibility;
        this.typeKind = typeKind;

        // 空を準備
        this.classComment = ClassComment.empty(type.typeIdentifier());
    }

    public void addAnnotation(Annotation annotation) {
        this.annotations.add(annotation);
    }

    public FieldDeclaration addInstanceField(FieldType fieldType, String name) {
        FieldDeclaration fieldDeclaration = new FieldDeclaration(type.typeIdentifier(), fieldType, name);
        instanceFields.add(new JigField(fieldDeclaration));

        return fieldDeclaration;
    }

    // フィールドと別になっているのが微妙
    public void addFieldAnnotation(FieldAnnotation fieldAnnotation) {
        instanceFields.replaceAll(jigField -> {
            if (jigField.matches(fieldAnnotation.fieldDeclaration())) {
                return jigField.newInstanceWith(fieldAnnotation);
            }
            return jigField;
        });
    }

    public void addStaticField(String name, TypeIdentifier typeIdentifier) {
        staticFieldDeclarations.add(new StaticFieldDeclaration(type.typeIdentifier(), name, typeIdentifier));
    }

    public List<JigMethodBuilder> constructorFacts() {
        return constructorFacts;
    }

    public List<JigMethodBuilder> staticJigMethodBuilders() {
        return staticJigMethodBuilders;
    }
}

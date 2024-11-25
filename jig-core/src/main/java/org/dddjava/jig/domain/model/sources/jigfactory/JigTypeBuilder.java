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
import org.dddjava.jig.domain.model.parts.classes.method.MethodReturn;
import org.dddjava.jig.domain.model.parts.classes.method.MethodSignature;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.*;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること
 */
public class JigTypeBuilder {

    private final ParameterizedType type;
    private final ParameterizedType superType;
    private final List<ParameterizedType> interfaceTypes;
    private final TypeKind typeKind;
    private final Visibility visibility;

    final List<Annotation> annotations;

    final List<StaticFieldDeclaration> staticFieldDeclarations;
    final List<JigField> instanceFields;
    final List<JigMethodBuilder> instanceJigMethodBuilders;
    final List<JigMethodBuilder> staticJigMethodBuilders;
    final List<JigMethodBuilder> constructorFacts;

    ClassComment classComment;

    private final List<RecordComponentDefinition> recordComponentDefinitions;

    private JigTypeBuilder(ParameterizedType type, ParameterizedType superType, List<ParameterizedType> interfaceTypes, TypeKind typeKind, Visibility visibility) {
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        this.typeKind = typeKind;
        this.visibility = visibility;

        // 空を準備
        this.annotations = new ArrayList<>();
        this.instanceJigMethodBuilders = new ArrayList<>();
        this.staticJigMethodBuilders = new ArrayList<>();
        this.constructorFacts = new ArrayList<>();
        this.instanceFields = new ArrayList<>();
        this.staticFieldDeclarations = new ArrayList<>();
        this.classComment = ClassComment.empty(type.typeIdentifier());
        this.recordComponentDefinitions = new ArrayList<>();
    }

    public static JigTypeBuilder constructWithHeaders(ParameterizedType type, ParameterizedType superType, List<ParameterizedType> interfaceTypes, Visibility visibility, TypeKind typeKind) {
        return new JigTypeBuilder(type, superType, interfaceTypes, typeKind, visibility);
    }

    public TypeIdentifier typeIdentifier() {
        return type.typeIdentifier();
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

    public void registerClassComment(ClassComment classComment) {
        this.classComment = classComment;
        this.jigType = null;
    }

    public boolean registerMethodComment(MethodComment methodComment) {
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

    public JigTypeBuilder applyTextSource(TextSourceModel textSourceModel) {
        // クラスのコメントを適用
        textSourceModel.optClassComment(typeIdentifier())
                .ifPresent(this::registerClassComment);

        for (JigMethodBuilder jigMethodBuilder : allMethodFacts()) {
            jigMethodBuilder.applyTextSource(textSourceModel);
        }
        textSourceModel.streamMethodComment(typeIdentifier())
                .forEach(methodComment -> registerMethodComment(methodComment));
        return this;
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

    public void instanceJigMethodBuilders(JigMethodBuilder jigMethodBuilder) {
        instanceJigMethodBuilders.add(jigMethodBuilder);
    }

    public void constructorFacts(JigMethodBuilder jigMethodBuilder) {
        constructorFacts.add(jigMethodBuilder);
    }

    public void staticJigMethodBuilders(JigMethodBuilder jigMethodBuilder) {
        staticJigMethodBuilders.add(jigMethodBuilder);
    }

    public void addRecordComponent(String name, TypeIdentifier typeIdentifier) {
        recordComponentDefinitions.add(new RecordComponentDefinition(name, typeIdentifier));
    }

    public boolean isRecordComponent(MethodSignature methodSignature, MethodReturn methodReturn) {
        return recordComponentDefinitions.stream()
                .anyMatch(recordComponentDefinition ->
                        methodSignature.methodName().equals(recordComponentDefinition.name())
                                && methodReturn.typeIdentifier().equals(recordComponentDefinition.typeIdentifier())
                );

    }
}

package org.dddjava.jig.domain.model.sources;

import org.dddjava.jig.domain.model.data.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.data.classes.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.data.classes.field.*;
import org.dddjava.jig.domain.model.data.classes.method.JigMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.type.*;
import org.dddjava.jig.domain.model.data.types.JigTypeHeader;
import org.dddjava.jig.domain.model.sources.classsources.RecordComponentDefinition;
import org.dddjava.jig.domain.model.sources.javasources.JavaSourceModel;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること
 */
public class JigTypeBuilder {

    private final ParameterizedType type;

    final List<Annotation> annotations;

    final List<StaticFieldDeclaration> staticFieldDeclarations;
    final List<JigField> instanceFields;
    final List<JigMethodBuilder> instanceJigMethodBuilders;
    final List<JigMethodBuilder> staticJigMethodBuilders;
    final List<JigMethodBuilder> constructorBuilders;

    ClassComment classComment;

    private final List<RecordComponentDefinition> recordComponentDefinitions;

    public JigTypeBuilder(ParameterizedType type) {
        this.type = type;

        // 空を準備
        this.annotations = new ArrayList<>();
        this.instanceJigMethodBuilders = new ArrayList<>();
        this.staticJigMethodBuilders = new ArrayList<>();
        this.constructorBuilders = new ArrayList<>();
        this.instanceFields = new ArrayList<>();
        this.staticFieldDeclarations = new ArrayList<>();
        this.classComment = ClassComment.empty(type.typeIdentifier());
        this.recordComponentDefinitions = new ArrayList<>();
    }

    public TypeIdentifier typeIdentifier() {
        return type.typeIdentifier();
    }

    public List<JigMethodBuilder> allMethodFacts() {
        ArrayList<JigMethodBuilder> list = new ArrayList<>();
        list.addAll(instanceJigMethodBuilders);
        list.addAll(staticJigMethodBuilders);
        list.addAll(constructorBuilders);
        return list;
    }

    public void registerClassComment(ClassComment classComment) {
        this.classComment = classComment;
    }

    public JigType build(JigTypeHeader jigTypeHeader) {
        JigTypeAttribute jigTypeAttribute = new JigTypeAttribute(classComment, annotations);

        JigStaticMember jigStaticMember = new JigStaticMember(
                new JigMethods(constructorBuilders.stream().map(JigMethodBuilder::build).collect(toList())),
                new JigMethods(staticJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList())),
                new StaticFieldDeclarations(this.staticFieldDeclarations));

        JigInstanceMember jigInstanceMember = new JigInstanceMember(
                new JigFields(instanceFields),
                new JigMethods(instanceJigMethodBuilders.stream().map(JigMethodBuilder::build).collect(toList())));

        return new JigType(jigTypeHeader, jigTypeAttribute, jigStaticMember, jigInstanceMember);
    }

    public JigTypeBuilder applyTextSource(JavaSourceModel javaSourceModel) {
        // クラスのコメントを適用
        javaSourceModel.optClassComment(typeIdentifier())
                .ifPresent(this::registerClassComment);

        for (JigMethodBuilder jigMethodBuilder : allMethodFacts()) {
            javaSourceModel.methodImplementations.stream()
                    .filter(methodImplementation -> methodImplementation.possiblyMatches(jigMethodBuilder.methodIdentifier()))
                    .findAny()
                    .ifPresent(methodImplementation -> jigMethodBuilder.registerMethodImplementation(methodImplementation));
        }
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

    public void addInstanceMethod(JigMethodBuilder jigMethodBuilder) {
        instanceJigMethodBuilders.add(jigMethodBuilder);
    }

    public void addConstructor(JigMethodBuilder jigMethodBuilder) {
        constructorBuilders.add(jigMethodBuilder);
    }

    public void addStaticMethod(JigMethodBuilder jigMethodBuilder) {
        staticJigMethodBuilders.add(jigMethodBuilder);
    }

    public void addRecordComponent(String name, TypeIdentifier typeIdentifier) {
        recordComponentDefinitions.add(new RecordComponentDefinition(name, typeIdentifier));
    }

    public boolean isRecordComponent(MethodDeclaration methodDeclaration) {
        return recordComponentDefinitions.stream()
                .anyMatch(recordComponentDefinition ->
                        methodDeclaration.methodSignature().methodName().equals(recordComponentDefinition.name())
                                && methodDeclaration.methodReturn().typeIdentifier().equals(recordComponentDefinition.typeIdentifier())
                );

    }
}

package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.jigmodel.jigtype.class_.TypeKind;
import org.dddjava.jig.domain.model.jigsource.file.binary.ClassSource;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.MethodFact;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.TypeFact;
import org.dddjava.jig.domain.model.parts.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.parts.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.field.FieldType;
import org.dddjava.jig.domain.model.parts.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.parts.method.*;
import org.dddjava.jig.domain.model.parts.type.ParameterizedType;
import org.dddjava.jig.domain.model.parts.type.TypeIdentifier;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

/**
 * PlainClassのビルダー
 */
class PlainClassBuilder {

    ClassSource classSource;
    ParameterizedType type;
    ParameterizedType superType;
    List<ParameterizedType> interfaceTypes;
    Visibility visibility;
    TypeKind typeKind;

    List<Annotation> annotations = new ArrayList<>();

    List<MethodFact> instanceMethodFacts = new ArrayList<>();
    List<MethodFact> staticMethodFacts = new ArrayList<>();
    List<MethodFact> constructorFacts = new ArrayList<>();

    List<FieldDeclaration> fieldDeclarations = new ArrayList<>();
    List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
    List<StaticFieldDeclaration> staticFieldDeclarations = new ArrayList<>();

    List<TypeIdentifier> useTypes = new ArrayList<>();

    public PlainClassBuilder(ClassSource classSource) {
        this.classSource = classSource;
    }

    public TypeFact build() {
        return new TypeFact(type, superType, interfaceTypes, typeKind, visibility,
                annotations,
                instanceMethodFacts,
                staticMethodFacts,
                constructorFacts,
                fieldDeclarations,
                fieldAnnotations,
                staticFieldDeclarations,
                useTypes
        );
    }

    public PlainClassBuilder withType(ParameterizedType type) {
        this.type = type;
        return this;
    }

    public PlainClassBuilder withParents(ParameterizedType superType, List<ParameterizedType> interfaceTypes) {
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        return this;
    }

    public PlainClassBuilder withVisibility(Visibility visibility) {
        this.visibility = visibility;
        return this;
    }

    public PlainClassBuilder withTypeKind(TypeKind typeKind) {
        this.typeKind = typeKind;
        return this;
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    /**
     * 「使っている」という曖昧な関連。無くしたい。
     */
    public void addUsingType(TypeIdentifier typeIdentifier) {
        useTypes.add(typeIdentifier);
    }

    public FieldDeclaration addInstanceField(FieldType fieldType, String name) {
        FieldDeclaration fieldDeclaration = new FieldDeclaration(type.typeIdentifier(), fieldType, name);
        fieldDeclarations.add(fieldDeclaration);
        return fieldDeclaration;
    }

    /**
     * FieldについているアノテーションをFieldと別に管理するのは違和感。。。
     */
    public void addFieldAnnotation(FieldAnnotation fieldAnnotation) {
        fieldAnnotations.add(fieldAnnotation);
    }

    public void addStaticField(String name, TypeIdentifier typeIdentifier) {
        staticFieldDeclarations.add(new StaticFieldDeclaration(type.typeIdentifier(), name, typeIdentifier));
    }

    public PlainMethodBuilder createPlainMethodBuilder(MethodSignature methodSignature,
                                                       MethodReturn methodReturn,
                                                       int access,
                                                       Visibility visibility,
                                                       List<TypeIdentifier> useTypes,
                                                       List<TypeIdentifier> throwsTypes) {
        MethodDeclaration methodDeclaration = new MethodDeclaration(type.typeIdentifier(), methodSignature, methodReturn);

        // 追加先のコレクションを判別
        List<MethodFact> methodFactCollector = instanceMethodFacts;
        if (methodDeclaration.isConstructor()) {
            methodFactCollector = constructorFacts;
        } else if ((access & Opcodes.ACC_STATIC) != 0) {
            methodFactCollector = staticMethodFacts;
        }

        MethodDerivation methodDerivation = resolveMethodDerivation(methodSignature, access);
        return new PlainMethodBuilder(methodDeclaration, useTypes, visibility, methodFactCollector, throwsTypes, methodDerivation);
    }

    private MethodDerivation resolveMethodDerivation(MethodSignature methodSignature, int access) {
        String name = methodSignature.methodName();
        if ("<init>".equals(name) || "<clinit>".equals(name)) {
            return MethodDerivation.CONSTRUCTOR;
        }

        if ((access & Opcodes.ACC_BRIDGE) != 0 || (access & Opcodes.ACC_SYNTHETIC) != 0) {
            return MethodDerivation.COMPILER_GENERATED;
        }

        if (superType.typeIdentifier().isEnum() && (access & Opcodes.ACC_STATIC) != 0) {
            // enumで生成されるstaticメソッド2つをコンパイラ生成として扱う
            if (methodSignature.isSame(new MethodSignature("values"))) {
                return MethodDerivation.COMPILER_GENERATED;
            } else if (methodSignature.isSame(new MethodSignature("valueOf", TypeIdentifier.of(String.class)))) {
                return MethodDerivation.COMPILER_GENERATED;
            }
        }

        return MethodDerivation.PROGRAMMER;
    }
}

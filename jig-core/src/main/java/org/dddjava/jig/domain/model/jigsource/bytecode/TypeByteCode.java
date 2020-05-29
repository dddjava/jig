package org.dddjava.jig.domain.model.jigsource.bytecode;

import org.dddjava.jig.domain.model.declaration.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.declaration.type.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * バイトコード
 */
public class TypeByteCode {


    final TypeIdentifier typeIdentifier;

    final boolean canExtend;

    final ParameterizedType parameterizedSuperType;
    final ParameterizedTypes parameterizedInterfaceTypes;

    final List<TypeAnnotation> typeAnnotations = new ArrayList<>();
    final List<StaticFieldDeclaration> staticFieldDeclarations = new ArrayList<>();

    final List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
    final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    final List<MethodByteCode> instanceMethodByteCodes = new ArrayList<>();
    final List<MethodByteCode> staticMethodByteCodes = new ArrayList<>();
    final List<MethodByteCode> constructorByteCodes = new ArrayList<>();

    final Set<TypeIdentifier> useTypes = new HashSet<>();

    public TypeByteCode(TypeIdentifier typeIdentifier,
                        ParameterizedType parameterizedSuperType,
                        ParameterizedTypes parameterizedInterfaceTypes,
                        List<TypeIdentifier> useTypes,
                        boolean canExtend) {
        this.typeIdentifier = typeIdentifier;
        this.parameterizedSuperType = parameterizedSuperType;
        this.parameterizedInterfaceTypes = parameterizedInterfaceTypes;
        this.canExtend = canExtend;

        this.useTypes.addAll(useTypes);
        this.useTypes.add(parameterizedSuperType.typeIdentifier());
        this.useTypes.addAll(parameterizedInterfaceTypes.identifiers().list());
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public boolean canExtend() {
        return canExtend;
    }

    public boolean isEnum() {
        return parameterizedSuperType.typeIdentifier().equals(new TypeIdentifier(Enum.class));
    }

    public boolean hasInstanceMethod() {
        return !instanceMethodByteCodes().isEmpty();
    }

    public boolean hasField() {
        return !fieldDeclarations.isEmpty();
    }

    public FieldDeclarations fieldDeclarations() {
        return new FieldDeclarations(fieldDeclarations);
    }

    public StaticFieldDeclarations staticFieldDeclarations() {
        return new StaticFieldDeclarations(staticFieldDeclarations);
    }

    public TypeIdentifiers useTypes() {
        for (MethodByteCode methodByteCode : methodByteCodes()) {
            useTypes.addAll(methodByteCode.methodDepend().useTypes());
        }

        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<MethodByteCode> instanceMethodByteCodes() {
        return instanceMethodByteCodes;
    }

    public List<TypeAnnotation> typeAnnotations() {
        return typeAnnotations;
    }

    public List<FieldAnnotation> annotatedFields() {
        return fieldAnnotations;
    }

    public void registerTypeAnnotation(TypeAnnotation typeAnnotation) {
        typeAnnotations.add(typeAnnotation);
        useTypes.add(typeAnnotation.type());
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

    public void registerInstanceMethodByteCodes(MethodByteCode methodByteCode) {
        instanceMethodByteCodes.add(methodByteCode);
    }

    public void registerStaticMethodByteCodes(MethodByteCode methodByteCode) {
        staticMethodByteCodes.add(methodByteCode);
    }

    public void registerConstructorByteCodes(MethodByteCode methodByteCode) {
        constructorByteCodes.add(methodByteCode);
    }

    public List<MethodByteCode> methodByteCodes() {
        ArrayList<MethodByteCode> list = new ArrayList<>();
        list.addAll(instanceMethodByteCodes);
        list.addAll(staticMethodByteCodes);
        list.addAll(constructorByteCodes);
        return list;
    }

    public ParameterizedType parameterizedSuperType() {
        return parameterizedSuperType;
    }

    public Type type() {
        return new Type(typeIdentifier, parameterizedSuperType, parameterizedInterfaceTypes);
    }

    public ParameterizedTypes parameterizedInterfaceTypes() {
        return parameterizedInterfaceTypes;
    }

    public MethodDeclarations methodDeclarations() {
        return methodByteCodes().stream()
                .map(MethodByteCode::methodDeclaration)
                .collect(MethodDeclarations.collector());
    }
}

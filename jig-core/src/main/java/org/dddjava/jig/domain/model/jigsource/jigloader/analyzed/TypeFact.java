package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.TypeAnnotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 型の実装から読み取れること
 */
public class TypeFact {

    final TypeIdentifier typeIdentifier;

    final boolean canExtend;

    final ParameterizedType parameterizedSuperType;
    final ParameterizedTypes parameterizedInterfaceTypes;

    final List<TypeAnnotation> typeAnnotations = new ArrayList<>();
    final List<StaticFieldDeclaration> staticFieldDeclarations = new ArrayList<>();

    final List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
    final List<FieldDeclaration> fieldDeclarations = new ArrayList<>();

    final List<MethodFact> instanceMethodFacts = new ArrayList<>();
    final List<MethodFact> staticMethodFacts = new ArrayList<>();
    final List<MethodFact> constructorFacts = new ArrayList<>();

    final Set<TypeIdentifier> useTypes = new HashSet<>();

    public TypeFact(TypeIdentifier typeIdentifier,
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
        return !instanceMethodFacts().isEmpty();
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
        for (MethodFact methodFact : allMethodFacts()) {
            useTypes.addAll(methodFact.methodDepend().collectUsingTypes());
        }

        return new TypeIdentifiers(new ArrayList<>(useTypes));
    }

    public List<MethodFact> instanceMethodFacts() {
        return instanceMethodFacts;
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
        return allMethodFacts().stream()
                .map(MethodFact::methodDeclaration)
                .collect(MethodDeclarations.collector());
    }
}

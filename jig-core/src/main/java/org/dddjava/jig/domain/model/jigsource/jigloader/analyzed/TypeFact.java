package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.businessrules.BusinessRule;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.TypeKind;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.TypeAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.FieldAnnotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.*;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    TypeAlias typeAlias;

    public TypeFact(ParameterizedType type,
                    ParameterizedType superType,
                    List<ParameterizedType> interfaceTypes,
                    TypeKind typeKind) {
        this.type = type;
        this.superType = superType;
        this.interfaceTypes = interfaceTypes;
        this.typeKind = typeKind;

        for (TypeParameter typeParameter : type.typeParameters().list()) {
            this.useTypes.add(typeParameter.typeIdentifier());
        }
        this.useTypes.add(superType.typeIdentifier());
        for (ParameterizedType interfaceType : interfaceTypes) {
            this.useTypes.add(interfaceType.typeIdentifier());
        }

        this.typeAlias = TypeAlias.empty(type.typeIdentifier());
    }

    public BusinessRule createBusinessRule() {
        return new BusinessRule(
                typeKind(),
                fieldDeclarations(),
                typeDeclaration(),
                methodDeclarations(),
                hasInstanceMethod()
        );
    }

    public TypeIdentifier typeIdentifier() {
        return type.typeIdentifier();
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

    public TypeDeclaration typeDeclaration() {
        return new TypeDeclaration(type, superType, new ParameterizedTypes(interfaceTypes));
    }

    public List<ParameterizedType> interfaceTypes() {
        return interfaceTypes;
    }

    public MethodDeclarations methodDeclarations() {
        return allMethodFacts().stream()
                .map(MethodFact::methodDeclaration)
                .collect(MethodDeclarations.collector());
    }

    void collectClassRelations(List<ClassRelation> collector) {
        TypeIdentifier form = typeIdentifier();
        for (TypeIdentifier to : useTypes().list()) {
            ClassRelation classRelation = new ClassRelation(form, to);
            if (classRelation.selfRelation()) continue;
            collector.add(classRelation);
        }
    }

    public TypeKind typeKind() {
        return typeKind;
    }

    public void registerTypeAlias(TypeAlias typeAlias) {
        this.typeAlias = typeAlias;
    }

    public void registerMethodAlias(MethodAlias methodAlias) {
        for (MethodFact methodFact : allMethodFacts()) {
            if (methodFact.methodIdentifier().equals(methodAlias.methodIdentifier())) {
                methodFact.registerMethodAlias(methodAlias);
            }
        }
    }
}

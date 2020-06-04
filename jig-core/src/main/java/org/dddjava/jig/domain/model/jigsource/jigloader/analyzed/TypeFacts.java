package org.dddjava.jig.domain.model.jigsource.jigloader.analyzed;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.*;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.StaticFieldDeclarations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.class_.ClassRelations;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.relation.method.MethodRelations;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 型の実装から読み取れること一覧
 */
public class TypeFacts {
    private final List<TypeFact> list;

    public TypeFacts(List<TypeFact> list) {
        this.list = list;
    }

    private ClassRelations classRelations;
    private MethodRelations methodRelations;

    public synchronized MethodRelations toMethodRelations() {
        if (methodRelations != null) {
            return methodRelations;
        }
        List<MethodRelation> collector = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            for (MethodFact methodFact : typeFact.allMethodFacts()) {
                methodFact.collectUsingMethodRelations(collector);
            }
        }
        methodRelations = new MethodRelations(collector);
        return methodRelations;
    }

    public synchronized ClassRelations toClassRelations() {
        if (classRelations != null) {
            return classRelations;
        }
        List<ClassRelation> collector = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            typeFact.collectClassRelations(collector);
        }
        classRelations = new ClassRelations(collector);
        return classRelations;
    }

    public List<TypeFact> list() {
        return list;
    }

    public List<MethodFact> instanceMethodFacts() {
        return list.stream()
                .map(TypeFact::instanceMethodFacts)
                .flatMap(List::stream)
                .collect(toList());
    }

    public FieldAnnotations annotatedFields() {
        List<FieldAnnotation> fieldAnnotations = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            fieldAnnotations.addAll(typeFact.annotatedFields());
        }
        return new FieldAnnotations(fieldAnnotations);
    }

    public MethodAnnotations annotatedMethods() {
        List<MethodAnnotation> methodAnnotations = new ArrayList<>();
        for (MethodFact methodFact : instanceMethodFacts()) {
            methodAnnotations.addAll(methodFact.annotatedMethods().list());
        }
        return new MethodAnnotations(methodAnnotations);
    }

    public FieldDeclarations instanceFields() {
        List<FieldDeclaration> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            FieldDeclarations fieldDeclarations = typeFact.fieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new FieldDeclarations(list);
    }

    public StaticFieldDeclarations staticFields() {
        List<StaticFieldDeclaration> list = new ArrayList<>();
        for (TypeFact typeFact : list()) {
            StaticFieldDeclarations fieldDeclarations = typeFact.staticFieldDeclarations();
            list.addAll(fieldDeclarations.list());
        }
        return new StaticFieldDeclarations(list);
    }

    public ValidationAnnotatedMembers validationAnnotatedMembers() {
        return new ValidationAnnotatedMembers(annotatedFields(), annotatedMethods());
    }
}

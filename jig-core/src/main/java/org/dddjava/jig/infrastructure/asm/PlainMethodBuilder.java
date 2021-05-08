package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.parts.classes.annotation.Annotation;
import org.dddjava.jig.domain.model.parts.classes.field.FieldDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDerivation;
import org.dddjava.jig.domain.model.parts.classes.method.Visibility;
import org.dddjava.jig.domain.model.parts.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.sources.jigfactory.JigMethodBuilder;

import java.util.ArrayList;
import java.util.List;

public class PlainMethodBuilder {
    MethodDeclaration methodDeclaration;
    List<TypeIdentifier> useTypes;
    Visibility visibility;
    List<TypeIdentifier> throwsTypes;
    MethodDerivation methodDerivation;

    List<JigMethodBuilder> jigMethodBuilderCollector;

    List<Annotation> annotations = new ArrayList<>();
    List<FieldDeclaration> fieldInstructions = new ArrayList<>();
    List<MethodDeclaration> methodInstructions = new ArrayList<>();
    List<TypeIdentifier> classReferenceCalls = new ArrayList<>();
    List<TypeIdentifier> invokeDynamicTypes = new ArrayList<>();

    private int lookupSwitchInstructionNumber = 0;
    private int jumpInstructionNumber = 0;

    boolean hasJudgeNull = false;
    boolean hasReferenceNull = false;

    public PlainMethodBuilder(MethodDeclaration methodDeclaration, List<TypeIdentifier> useTypes, Visibility visibility, List<JigMethodBuilder> jigMethodBuilderCollector, List<TypeIdentifier> throwsTypes, MethodDerivation methodDerivation) {
        this.methodDeclaration = methodDeclaration;
        this.useTypes = useTypes;
        this.visibility = visibility;
        this.jigMethodBuilderCollector = jigMethodBuilderCollector;
        this.throwsTypes = throwsTypes;
        this.methodDerivation = methodDerivation;
    }

    public void buildAndCollect() {
        JigMethodBuilder jigMethodBuilder = new JigMethodBuilder(
                methodDeclaration, useTypes, visibility, methodDerivation,
                annotations,
                throwsTypes,
                fieldInstructions,
                methodInstructions,
                classReferenceCalls,
                invokeDynamicTypes,
                lookupSwitchInstructionNumber,
                jumpInstructionNumber,
                hasJudgeNull,
                hasReferenceNull
        );

        jigMethodBuilderCollector.add(jigMethodBuilder);
    }

    public void addAnnotation(Annotation annotation) {
        annotations.add(annotation);
    }

    public void addFieldInstruction(FieldDeclaration fieldDeclaration) {
        fieldInstructions.add(fieldDeclaration);
    }

    public void addMethodInstruction(MethodDeclaration methodDeclaration) {
        methodInstructions.add(methodDeclaration);
    }

    public void addClassReferenceCall(TypeIdentifier typeIdentifier) {
        classReferenceCalls.add(typeIdentifier);
    }

    public void addInvokeDynamicType(TypeIdentifier typeIdentifier) {
        invokeDynamicTypes.add(typeIdentifier);
    }

    public void addLookupSwitch() {
        lookupSwitchInstructionNumber++;
    }

    public void addJump() {
        jumpInstructionNumber++;
    }

    public void markJudgeNull() {
        hasJudgeNull = true;
    }

    public void markReferenceNull() {
        hasReferenceNull = true;
    }
}

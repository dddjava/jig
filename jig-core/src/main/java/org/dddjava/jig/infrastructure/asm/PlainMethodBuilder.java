package org.dddjava.jig.infrastructure.asm;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.annotation.Annotation;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.field.FieldDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.GeneratedBy;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.Visibility;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigsource.jigloader.analyzed.MethodFact;

import java.util.ArrayList;
import java.util.List;

public class PlainMethodBuilder {
    MethodDeclaration methodDeclaration;
    List<TypeIdentifier> useTypes;
    Visibility visibility;
    List<TypeIdentifier> throwsTypes;
    GeneratedBy generatedBy;

    List<MethodFact> methodFactCollector;

    List<Annotation> annotations = new ArrayList<>();
    List<FieldDeclaration> fieldInstructions = new ArrayList<>();
    List<MethodDeclaration> methodInstructions = new ArrayList<>();
    List<TypeIdentifier> classReferenceCalls = new ArrayList<>();
    List<TypeIdentifier> invokeDynamicTypes = new ArrayList<>();

    private int lookupSwitchInstructionNumber = 0;
    private int jumpInstructionNumber = 0;

    boolean hasJudgeNull = false;
    boolean hasReferenceNull = false;

    public PlainMethodBuilder(MethodDeclaration methodDeclaration, List<TypeIdentifier> useTypes, Visibility visibility, List<MethodFact> methodFactCollector, List<TypeIdentifier> throwsTypes, GeneratedBy generatedBy) {
        this.methodDeclaration = methodDeclaration;
        this.useTypes = useTypes;
        this.visibility = visibility;
        this.methodFactCollector = methodFactCollector;
        this.throwsTypes = throwsTypes;
        this.generatedBy = generatedBy;
    }

    public void buildAndCollect() {
        MethodFact methodFact = new MethodFact(
                methodDeclaration, useTypes, visibility, generatedBy,
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

        methodFactCollector.add(methodFact);
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

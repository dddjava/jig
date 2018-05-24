package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.characteristic.Characteristics;
import org.dddjava.jig.domain.model.characteristic.CharacterizedTypes;
import org.dddjava.jig.domain.model.characteristic.Layer;
import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.method.MethodSignature;
import org.dddjava.jig.domain.model.identifier.type.TypeIdentifier;

public class DecisionAngle {

    MethodDeclaration methodDeclaration;
    Characteristics typeCharacteristics;

    public DecisionAngle(MethodDeclaration methodDeclaration, Characteristics typeCharacteristics) {
        this.methodDeclaration = methodDeclaration;
        this.typeCharacteristics = typeCharacteristics;
    }

    public static DecisionAngle of(CharacterizedTypes characterizedTypes, MethodDeclaration methodDeclaration) {
        return new DecisionAngle(methodDeclaration, characterizedTypes.stream().pickup(methodDeclaration.declaringType()).characteristics());
    }

    public TypeIdentifier declaringType() {
        return methodDeclaration.declaringType();
    }

    public MethodSignature methodSignature() {
        return methodDeclaration.methodSignature();
    }

    public Layer typeLayer() {
        return typeCharacteristics.toLayer();
    }
}

package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.characteristic.Characteristics;
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

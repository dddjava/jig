package org.dddjava.jig.domain.model.angle;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;

public class DecisionAngle {

    private final MethodDeclarations methods;

    public DecisionAngle(MethodDeclarations methods) {
        this.methods = methods;
    }

    public MethodDeclarations methods() {
        return methods;
    }
}

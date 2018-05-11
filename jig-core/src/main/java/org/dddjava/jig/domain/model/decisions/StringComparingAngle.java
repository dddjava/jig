package org.dddjava.jig.domain.model.decisions;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclarations;

public class StringComparingAngle {

    private final MethodDeclarations stringComparingMethods;

    public StringComparingAngle(MethodDeclarations stringComparingMethods) {
        this.stringComparingMethods = stringComparingMethods;
    }

    public MethodDeclarations stringComparingMethods() {
        return stringComparingMethods;
    }
}

package org.dddjava.jig.domain.model.japanese;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;

public class MethodJapaneseName {
    MethodDeclaration methodDeclaration;
    JapaneseName japaneseName;

    public MethodJapaneseName(MethodDeclaration methodDeclaration, JapaneseName japaneseName) {
        this.methodDeclaration = methodDeclaration;
        this.japaneseName = japaneseName;
    }

    public MethodDeclaration methodDeclaration() {
        return methodDeclaration;
    }

    public JapaneseName japaneseName() {
        return japaneseName;
    }
}

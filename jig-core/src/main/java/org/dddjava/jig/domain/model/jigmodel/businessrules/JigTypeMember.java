package org.dddjava.jig.domain.model.jigmodel.businessrules;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;

public class JigTypeMember {
    private final MethodDeclarations constructorDeclarations;
    private final MethodDeclarations staticMethodDeclarations;

    public JigTypeMember(MethodDeclarations constructorDeclarations, MethodDeclarations staticMethodDeclarations) {
        this.constructorDeclarations = constructorDeclarations;
        this.staticMethodDeclarations = staticMethodDeclarations;
    }

    public MethodDeclarations getConstructorDeclarations() {
        return constructorDeclarations;
    }

    public MethodDeclarations getStaticMethodDeclarations() {
        return staticMethodDeclarations;
    }
}

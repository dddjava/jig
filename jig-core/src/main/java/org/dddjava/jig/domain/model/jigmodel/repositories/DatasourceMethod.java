package org.dddjava.jig.domain.model.jigmodel.repositories;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.method.MethodDeclarations;

/**
 * データソースの構造
 */
public class DatasourceMethod {
    JigMethod repositoryMethod;
    JigMethod concreteMethod;
    MethodDeclarations usingMethods;

    public DatasourceMethod(JigMethod repositoryMethod, JigMethod concreteMethod, MethodDeclarations usingMethods) {
        this.repositoryMethod = repositoryMethod;
        this.concreteMethod = concreteMethod;
        this.usingMethods = usingMethods;
    }

    public JigMethod repositoryMethod() {
        return repositoryMethod;
    }

    public JigMethod concreteMethod() {
        return concreteMethod;
    }

    public MethodDeclarations usingMethods() {
        return usingMethods;
    }
}

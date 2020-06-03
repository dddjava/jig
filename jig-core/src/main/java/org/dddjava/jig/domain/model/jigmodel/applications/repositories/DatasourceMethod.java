package org.dddjava.jig.domain.model.jigmodel.applications.repositories;

import org.dddjava.jig.domain.model.jigmodel.declaration.method.MethodDeclarations;
import org.dddjava.jig.domain.model.jigmodel.richmethod.Method;

/**
 * データソースの構造
 */
public class DatasourceMethod {
    Method repositoryMethod;
    Method concreteMethod;
    MethodDeclarations usingMethods;

    public DatasourceMethod(Method repositoryMethod, Method concreteMethod, MethodDeclarations usingMethods) {
        this.repositoryMethod = repositoryMethod;
        this.concreteMethod = concreteMethod;
        this.usingMethods = usingMethods;
    }

    public Method repositoryMethod() {
        return repositoryMethod;
    }

    public Method concreteMethod() {
        return concreteMethod;
    }

    public MethodDeclarations usingMethods() {
        return usingMethods;
    }
}

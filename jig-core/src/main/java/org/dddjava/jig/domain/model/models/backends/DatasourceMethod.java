package org.dddjava.jig.domain.model.models.backends;

import org.dddjava.jig.domain.model.models.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclarations;

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

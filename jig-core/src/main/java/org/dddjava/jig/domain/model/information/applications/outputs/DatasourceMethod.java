package org.dddjava.jig.domain.model.information.applications.outputs;

import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;
import org.dddjava.jig.domain.model.parts.classes.method.MethodDeclarations;

/**
 * データソースの構造
 */
public class DatasourceMethod {
    JigMethod repositoryMethod;
    JigMethod concreteMethod;

    public DatasourceMethod(JigMethod repositoryMethod, JigMethod concreteMethod) {
        this.repositoryMethod = repositoryMethod;
        this.concreteMethod = concreteMethod;
    }

    public JigMethod repositoryMethod() {
        return repositoryMethod;
    }

    public JigMethod concreteMethod() {
        return concreteMethod;
    }

    public MethodDeclarations usingMethods() {
        return concreteMethod().usingMethods().methodDeclarations();
    }
}

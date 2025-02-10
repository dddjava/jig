package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclarations;
import org.dddjava.jig.domain.model.information.method.JigMethod;

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

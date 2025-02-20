package org.dddjava.jig.domain.model.information.outputs;

import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.types.JigType;

/**
 * データソースの構造
 */
public record DatasourceMethod(JigMethod repositoryMethod, JigMethod concreteMethod, JigType interfaceJigType) {

    public UsingMethods usingMethods() {
        return concreteMethod().usingMethods();
    }
}

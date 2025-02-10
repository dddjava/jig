package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.information.method.CallerMethods;

public interface CallerMethodsFactory {
    CallerMethods callerMethodsOf(MethodDeclaration methodDeclaration);
}

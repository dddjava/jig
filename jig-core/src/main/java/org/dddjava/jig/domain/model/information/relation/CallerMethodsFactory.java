package org.dddjava.jig.domain.model.information.relation;

import org.dddjava.jig.domain.model.data.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;

public interface CallerMethodsFactory {
    CallerMethods callerMethodsOf(MethodDeclaration methodDeclaration);
}

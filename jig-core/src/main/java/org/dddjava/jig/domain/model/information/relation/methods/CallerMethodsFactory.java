package org.dddjava.jig.domain.model.information.relation.methods;

import org.dddjava.jig.domain.model.data.members.methods.JigMethodId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;

public interface CallerMethodsFactory {
    CallerMethods callerMethodsOf(JigMethodId jigMethodId);
}

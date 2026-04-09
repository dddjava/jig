package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

/**
 * サービスメソッド
 */
public record ServiceMethod(JigMethod method, CallerMethods callerMethods) {

    public static ServiceMethod from(JigMethod jigMethod, CallerMethodsFactory callerMethodsFactory) {
        return new ServiceMethod(
                jigMethod,
                callerMethodsFactory.callerMethodsOf(jigMethod.jigMethodId())
        );
    }

    public UsingFields methodUsingFields() {
        return method.usingFields();
    }

    public UsingMethods usingMethods() {
        return method.usingMethods();
    }

    public JigMethod method() {
        return method;
    }

    public TypeId declaringType() {
        return method.declaringType();
    }
}

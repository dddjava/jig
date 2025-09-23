package org.dddjava.jig.domain.model.knowledge.usecases;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;

/**
 * ユースケース
 */
public record Usecase(ServiceMethod serviceMethod, UsecaseCategory usecaseCategory) {

    public static Usecase from(ServiceAngle serviceAngle) {
        return new Usecase(serviceAngle.serviceMethod(), UsecaseCategory.resolver(serviceAngle));
    }

    public String usecaseIdentifier() {
        return serviceMethod.method().fqn();
    }

    public String usecaseLabel() {
        return serviceMethod.method().aliasText();
    }

    public String simpleTextWithDeclaringType() {
        return serviceMethod.method().simpleText();
    }

    public boolean isHandler() {
        return usecaseCategory.handler();
    }

    public TypeId declaringType() {
        return serviceMethod.declaringType();
    }
}

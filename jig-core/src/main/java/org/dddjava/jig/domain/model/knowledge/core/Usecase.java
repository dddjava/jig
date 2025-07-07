package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.types.TypeId;
import org.dddjava.jig.domain.model.information.applications.ServiceMethod;

import java.util.List;
import java.util.Optional;

/**
 * ユースケース
 */
public class Usecase {

    ServiceMethod serviceMethod;
    UsecaseCategory usecaseCategory;

    public Usecase(ServiceAngle serviceAngle) {
        this.serviceMethod = serviceAngle.serviceMethod();
        this.usecaseCategory = UsecaseCategory.resolver(serviceAngle);
    }

    public List<TypeId> internalUsingTypes() {
        return serviceMethod.internalUsingTypes();
    }

    public Optional<TypeId> primaryType() {
        return serviceMethod.primaryType();
    }

    public List<TypeId> requireTypes() {
        return serviceMethod.requireTypes();
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

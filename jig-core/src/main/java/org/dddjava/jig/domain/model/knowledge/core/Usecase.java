package org.dddjava.jig.domain.model.knowledge.core;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
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

    public List<TypeIdentifier> internalUsingTypes() {
        return serviceMethod.internalUsingTypes();
    }

    public Optional<TypeIdentifier> primaryType() {
        return serviceMethod.primaryType();
    }

    public List<TypeIdentifier> requireTypes() {
        return serviceMethod.requireTypes();
    }

    public String usecaseIdentifier() {
        return serviceMethod.methodDeclaration().asFullNameText();
    }

    public String usecaseLabel() {
        return serviceMethod.method().aliasText();
    }

    public String simpleTextWithDeclaringType() {
        return serviceMethod.methodDeclaration().asSimpleTextWithDeclaringType();
    }

    public boolean isHandler() {
        return usecaseCategory.handler();
    }

    public TypeIdentifier declaringType() {
        return serviceMethod.declaringType();
    }
}

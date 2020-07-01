package org.dddjava.jig.domain.model.jigmodel.services;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.package_.PackageIdentifier;
import org.dddjava.jig.domain.model.jigmodel.lowmodel.declaration.type.TypeIdentifier;

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

    public String usecaseLabel(AliasFinder aliasFinder) {
        MethodAlias methodAlias = aliasFinder.find(serviceMethod.methodDeclaration().identifier());
        return methodAlias.asTextOrDefault(serviceMethod.methodDeclaration().declaringType().asSimpleText() + "\\n" + serviceMethod.methodDeclaration().methodSignature().methodName());
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

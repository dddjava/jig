package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethod;

import java.util.List;
import java.util.stream.Collectors;

/**
 * ユースケース
 */
public class UseCase {
    ServiceMethod serviceMethod;

    public UseCase(ServiceMethod serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    List<TypeIdentifier> internalUsingTypes() {
        List<TypeIdentifier> list = serviceMethod.usingMethods().methodDeclarations().list().stream()
                .flatMap(methodDeclaration -> methodDeclaration.relateTypes().list().stream())
                .filter(typeIdentifier -> !typeIdentifier.isPrimitive())
                .filter(typeIdentifier -> !typeIdentifier.isVoid())
                .distinct()
                .collect(Collectors.toList());
        return list;
    }

    TypeIdentifier returnType() {
        return serviceMethod.methodDeclaration().methodReturn().typeIdentifier();
    }

    List<TypeIdentifier> requireTypes() {
        return serviceMethod.methodDeclaration().methodSignature().arguments();
    }

    public String useCaseIdentifier() {
        return serviceMethod.methodDeclaration().asFullNameText();
    }

    public String useCaseLabel(AliasFinder aliasFinder) {
        MethodAlias methodAlias = aliasFinder.find(serviceMethod.methodDeclaration().identifier());
        return methodAlias.asTextOrDefault(serviceMethod.methodDeclaration().declaringType().asSimpleText() + "\\n" + serviceMethod.methodDeclaration().methodSignature().methodName());
    }
}

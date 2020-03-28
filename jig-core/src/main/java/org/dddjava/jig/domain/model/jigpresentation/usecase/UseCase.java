package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.MethodAlias;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethod;

import java.util.List;
import java.util.Optional;
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
                .filter(typeIdentifier -> !primaryType().filter(primaryType -> primaryType.equals(typeIdentifier)).isPresent())
                .filter(typeIdentifier -> !requireTypes().contains(typeIdentifier))
                .distinct()
                .collect(Collectors.toList());
        return list;
    }

    Optional<TypeIdentifier> primaryType() {
        TypeIdentifier typeIdentifier = serviceMethod.methodDeclaration().methodReturn().typeIdentifier();
        if (typeIdentifier.isVoid()) return Optional.empty();
        return Optional.of(typeIdentifier);
    }

    List<TypeIdentifier> requireTypes() {
        List<TypeIdentifier> arguments = serviceMethod.methodDeclaration().methodSignature().arguments();
        // primaryTypeは除く
        primaryType().ifPresent(arguments::remove);
        return arguments;
    }

    public String useCaseIdentifier() {
        return serviceMethod.methodDeclaration().asFullNameText();
    }

    public String useCaseLabel(AliasFinder aliasFinder) {
        MethodAlias methodAlias = aliasFinder.find(serviceMethod.methodDeclaration().identifier());
        return methodAlias.asTextOrDefault(serviceMethod.methodDeclaration().declaringType().asSimpleText() + "\\n" + serviceMethod.methodDeclaration().methodSignature().methodName());
    }
}

package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.richmethod.UsingMethods;
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

    public List<String> listRelationTexts() {
        UsingMethods usingMethods = serviceMethod.usingMethods();
        List<TypeIdentifier> list2 = usingMethods.methodDeclarations().list().stream()
                .flatMap(methodDeclaration -> methodDeclaration.relateTypes().list().stream())
                .collect(Collectors.toList());

        return list2.stream()
                .filter(typeIdentifier -> !typeIdentifier.isPrimitive())
                .filter(typeIdentifier -> !typeIdentifier.isVoid())
                .map(typeIdentifier -> typeIdentifier.asSimpleText())
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    public String useCaseIdentifier() {
        return serviceMethod.methodDeclaration().asFullNameText();
    }

    public String useCaseLabel() {
        return serviceMethod.methodDeclaration().declaringType().asSimpleText() + "\\n" + serviceMethod.methodDeclaration().methodSignature().methodName();
    }
}

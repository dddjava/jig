package org.dddjava.jig.domain.model.jigpresentation.usecase;

import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.richmethod.UsingFields;
import org.dddjava.jig.domain.model.jigloaded.richmethod.UsingMethods;
import org.dddjava.jig.domain.model.jigmodel.applications.services.ServiceMethod;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ユースケース
 */
public class UseCase {
    ServiceMethod serviceMethod;

    public UseCase(ServiceMethod serviceMethod) {
        this.serviceMethod = serviceMethod;
    }

    public List<String> listRelationTexts() {
        UsingFields usingFields = serviceMethod.methodUsingFields();
        List<TypeIdentifier> list1 = usingFields.typeIdentifiers().list();

        UsingMethods usingMethods = serviceMethod.usingMethods();
        List<TypeIdentifier> list2 = usingMethods.methodDeclarations().list().stream()
                .map(methodDeclaration -> methodDeclaration.methodReturn().typeIdentifier())
                .collect(Collectors.toList());

        return Stream.concat(list1.stream(), list2.stream())
                .filter(typeIdentifier -> !typeIdentifier.isPrimitive())
                .map(typeIdentifier -> typeIdentifier.asSimpleText())
                .sorted()
                .distinct()
                .collect(Collectors.toList());
    }

    public String useCaseName() {
        return serviceMethod.declaringType().asSimpleText();
    }
}

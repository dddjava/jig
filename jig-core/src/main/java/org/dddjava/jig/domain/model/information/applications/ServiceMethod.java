package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.types.JigTypeReference;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.members.CallerMethods;
import org.dddjava.jig.domain.model.information.members.JigMethod;
import org.dddjava.jig.domain.model.information.members.UsingFields;
import org.dddjava.jig.domain.model.information.members.UsingMethods;
import org.dddjava.jig.domain.model.information.relation.methods.CallerMethodsFactory;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * サービスメソッド
 */
public record ServiceMethod(JigMethod method, CallerMethods callerMethods) {

    public static ServiceMethod from(JigMethod jigMethod, CallerMethodsFactory callerMethodsFactory) {
        return new ServiceMethod(
                jigMethod,
                callerMethodsFactory.callerMethodsOf(jigMethod.jigMethodIdentifier())
        );
    }

    public MethodDeclaration methodDeclaration() {
        return method.declaration();
    }

    public boolean isPublic() {
        return method.isPublic();
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

    public TypeIdentifier declaringType() {
        return methodDeclaration().declaringType();
    }

    public List<TypeIdentifier> internalUsingTypes() {
        return usingMethods().invokedMethodStream()
                .flatMap(invokedMethod -> invokedMethod.extractTypeIdentifiers().stream())
                .filter(typeIdentifier -> !typeIdentifier.isJavaLanguageType())
                .filter(typeIdentifier -> primaryType().filter(primaryType -> primaryType.equals(typeIdentifier)).isEmpty())
                .filter(typeIdentifier -> !requireTypes().contains(typeIdentifier))
                .distinct()
                .collect(Collectors.toList());
    }

    public Optional<TypeIdentifier> primaryType() {
        // 戻り値型が主要な関心
        TypeIdentifier typeIdentifier = method().methodReturnTypeReference().id();
        if (typeIdentifier.isVoid()) return Optional.empty();
        return Optional.of(typeIdentifier);
    }

    public List<TypeIdentifier> requireTypes() {
        return method.jigMethodDeclaration().argumentStream()
                .map(JigTypeReference::id)
                // primaryTypeは除く
                .filter(argumentType -> primaryType().filter(primaryType -> primaryType.equals(argumentType)).isEmpty())
                .toList();
    }

    public TypeIdentifiers usingTypes() {
        return method().usingTypes();
    }
}

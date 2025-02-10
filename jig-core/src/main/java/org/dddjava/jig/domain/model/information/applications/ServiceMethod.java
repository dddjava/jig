package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.classes.method.CallerMethods;
import org.dddjava.jig.domain.model.data.classes.method.MethodDeclaration;
import org.dddjava.jig.domain.model.data.classes.method.UsingMethods;
import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.method.JigMethod;
import org.dddjava.jig.domain.model.information.method.UsingFields;
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
                callerMethodsFactory.callerMethodsOf(jigMethod.declaration())
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

    public boolean isCall(MethodDeclaration methodDeclaration) {
        return method.usingMethods().methodDeclarations().contains(methodDeclaration);
    }

    public TypeIdentifier declaringType() {
        return methodDeclaration().declaringType();
    }

    public List<TypeIdentifier> internalUsingTypes() {
        return usingMethods().methodDeclarations().list().stream()
                .flatMap(methodDeclaration -> methodDeclaration.relateTypes().stream())
                .filter(typeIdentifier -> !typeIdentifier.isJavaLanguageType())
                .filter(typeIdentifier -> primaryType().filter(primaryType -> primaryType.equals(typeIdentifier)).isEmpty())
                .filter(typeIdentifier -> !requireTypes().contains(typeIdentifier))
                .distinct()
                .collect(Collectors.toList());
    }

    public Optional<TypeIdentifier> primaryType() {
        // 戻り値型が主要な関心
        TypeIdentifier typeIdentifier = methodDeclaration().methodReturn().typeIdentifier();
        if (typeIdentifier.isVoid()) return Optional.empty();
        return Optional.of(typeIdentifier);
    }

    public List<TypeIdentifier> requireTypes() {
        return methodDeclaration().methodSignature().arguments().stream()
                .map(ParameterizedType::typeIdentifier)
                // primaryTypeは除く
                .filter(argumentType -> primaryType().filter(primaryType -> primaryType.equals(argumentType)).isEmpty())
                .toList();
    }

    public TypeIdentifiers usingTypes() {
        return method().usingTypes();
    }

    public boolean sameIdentifier(MethodDeclaration methodDeclaration) {
        return methodDeclaration().sameIdentifier(methodDeclaration);
    }
}

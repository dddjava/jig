package org.dddjava.jig.domain.model.information.applications;

import org.dddjava.jig.domain.model.data.classes.method.*;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.information.jigobject.member.JigMethod;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * サービスメソッド
 */
public record ServiceMethod(JigMethod method, CallerMethods callerMethods) {

    public static ServiceMethod from(JigMethod jigMethod, MethodRelations methodRelations) {
        return new ServiceMethod(
                jigMethod,
                methodRelations.callerMethodsOf(jigMethod.declaration())
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

    // TODO type
    public List<TypeIdentifier> internalUsingTypes() {
        return usingMethods().methodDeclarations().list().stream()
                .flatMap(methodDeclaration -> methodDeclaration.relateTypes().stream())
                .filter(typeIdentifier -> !typeIdentifier.isJavaLanguageType())
                .filter(typeIdentifier -> !primaryType().filter(primaryType -> primaryType.equals(typeIdentifier)).isPresent())
                .filter(typeIdentifier -> !requireTypes().contains(typeIdentifier))
                .distinct()
                .collect(Collectors.toList());
    }

    // TODO type
    public Optional<TypeIdentifier> primaryType() {
        // 戻り値型が主要な関心
        TypeIdentifier typeIdentifier = methodDeclaration().methodReturn().typeIdentifier();
        if (typeIdentifier.isVoid()) return Optional.empty();
        return Optional.of(typeIdentifier);
    }

    // TODO type
    public List<TypeIdentifier> requireTypes() {
        List<TypeIdentifier> arguments = new ArrayList<>(methodDeclaration().methodSignature().listArgumentTypeIdentifiers());
        // primaryTypeは除く
        primaryType().ifPresent(arguments::remove);
        return arguments;
    }

    public TypeIdentifiers usingTypes() {
        return method().usingTypes();
    }

    public boolean sameIdentifier(MethodDeclaration methodDeclaration) {
        return methodDeclaration().sameIdentifier(methodDeclaration);
    }
}

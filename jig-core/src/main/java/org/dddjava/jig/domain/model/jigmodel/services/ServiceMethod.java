package org.dddjava.jig.domain.model.jigmodel.services;

import org.dddjava.jig.domain.model.jigmodel.jigtype.member.JigMethod;
import org.dddjava.jig.domain.model.jigmodel.jigtype.member.MethodWorries;
import org.dddjava.jig.domain.model.parts.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifiers;
import org.dddjava.jig.domain.model.parts.relation.method.UsingFields;
import org.dddjava.jig.domain.model.parts.relation.method.UsingMethods;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * サービスメソッド
 */
public class ServiceMethod {
    private final JigMethod method;

    public ServiceMethod(JigMethod method) {
        this.method = method;
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

    public MethodWorries methodWorries() {
        return method.methodWorries();
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
        List<TypeIdentifier> list = usingMethods().methodDeclarations().list().stream()
                .flatMap(methodDeclaration -> methodDeclaration.relateTypes().list().stream())
                .filter(typeIdentifier -> !typeIdentifier.isJavaLanguageType())
                .filter(typeIdentifier -> !primaryType().filter(primaryType -> primaryType.equals(typeIdentifier)).isPresent())
                .filter(typeIdentifier -> !requireTypes().contains(typeIdentifier))
                .distinct()
                .collect(Collectors.toList());
        return list;
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
        List<TypeIdentifier> arguments = methodDeclaration().methodSignature().arguments();
        // primaryTypeは除く
        primaryType().ifPresent(arguments::remove);
        return arguments;
    }

    public TypeIdentifiers usingTypes() {
        return method().usingTypes();
    }
}

package org.dddjava.jig.domain.model.jigmodel.applications.services;

import org.dddjava.jig.domain.model.declaration.method.MethodDeclaration;
import org.dddjava.jig.domain.model.declaration.type.TypeIdentifier;
import org.dddjava.jig.domain.model.jigloaded.alias.AliasFinder;
import org.dddjava.jig.domain.model.jigloaded.alias.TypeAlias;

import java.util.function.Function;

import static java.util.stream.Collectors.joining;

public enum MethodNodeLabelStyle {
    /** method(ArgumentTypes) : ReturnType */
    SIMPLE,
    /** method(引数型) : 戻り値型 */
    JAPANESE;

    public String apply(MethodDeclaration method, AliasFinder aliasFinder) {
        if (this == JAPANESE) {
            Function<TypeIdentifier, String> func = typeIdentifier -> {
                TypeAlias typeAlias = aliasFinder.find(typeIdentifier);
                if (typeAlias.exists()) {
                    return typeAlias.asText();
                }
                return typeIdentifier.asSimpleText();
            };

            return method.methodSignature().methodName()
                    + "("
                    + method.methodSignature().arguments().stream().map(func).collect(joining(", "))
                    + ")"
                    + " : "
                    + func.apply(method.methodReturn().typeIdentifier());
        }

        return method.asSignatureSimpleText() + " : " + method.methodReturn().typeIdentifier().asSimpleText();
    }

    public String typeNameAndMethodName(MethodDeclaration methodDeclaration, AliasFinder aliasFinder) {
        return methodDeclaration.declaringType().asSimpleText() + "." + apply(methodDeclaration, aliasFinder);
    }
}

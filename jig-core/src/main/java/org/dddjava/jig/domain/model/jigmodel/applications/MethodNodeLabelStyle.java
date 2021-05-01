package org.dddjava.jig.domain.model.jigmodel.applications;

import org.dddjava.jig.domain.model.parts.alias.AliasFinder;
import org.dddjava.jig.domain.model.parts.class_.type.ClassComment;
import org.dddjava.jig.domain.model.parts.class_.method.MethodDeclaration;
import org.dddjava.jig.domain.model.parts.class_.type.TypeIdentifier;

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
                ClassComment classComment = aliasFinder.find(typeIdentifier);
                if (classComment.exists()) {
                    return classComment.asText();
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

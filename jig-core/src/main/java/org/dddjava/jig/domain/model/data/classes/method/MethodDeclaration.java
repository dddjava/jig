package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.members.JigMethodIdentifier;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * メソッド定義
 */
public class MethodDeclaration {

    private final JigMethodIdentifier jigMethodIdentifier;
    private final TypeIdentifier declaringType;

    public MethodDeclaration(TypeIdentifier declaringType, MethodSignature methodSignature) {
        this.jigMethodIdentifier = JigMethodIdentifier.from(declaringType, methodSignature.methodName(),
                methodSignature.arguments().stream().map(ParameterizedType::typeIdentifier).toList());
        this.declaringType = declaringType;
    }

    /**
     * @return "org.dddjava.jig.domain.model.parts.classes.method.MethodDeclaration#asFullNameText()"
     */
    public String asFullNameText() {
        return jigMethodIdentifier.value();
    }

    public TypeIdentifier declaringType() {
        return declaringType;
    }

    public JigMethodIdentifier jigMethodIdentifier() {
        return jigMethodIdentifier;
    }
}

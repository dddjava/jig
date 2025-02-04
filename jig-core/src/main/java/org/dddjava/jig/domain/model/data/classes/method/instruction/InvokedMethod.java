package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

import java.util.ArrayList;
import java.util.List;

/**
 * 呼び出すメソッド
 *
 * MethodDeclarationやMethodSignatureはジェネリクスを含む場合などもあるため、呼び出しでは使用できない。
 */
public record InvokedMethod(TypeIdentifier methodOwner, String methodName,
                            List<TypeIdentifier> argumentTypes,
                            TypeIdentifier returnType) {

    public List<TypeIdentifier> extractTypeIdentifiers() {
        List<TypeIdentifier> extractedTypes = new ArrayList<>(argumentTypes);
        extractedTypes.add(methodOwner);
        extractedTypes.add(returnType);
        return extractedTypes;
    }
}

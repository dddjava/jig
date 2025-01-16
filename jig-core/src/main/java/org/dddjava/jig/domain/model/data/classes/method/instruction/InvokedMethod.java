package org.dddjava.jig.domain.model.data.classes.method.instruction;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.List;

/**
 * 呼び出すメソッド
 *
 * MethodDeclarationやMethodSignatureはジェネリクスを含む場合などもあるため、呼び出しでは使用できない。
 */
public record InvokedMethod(TypeIdentifier methodOwner, String methodName,
                            List<TypeIdentifier> argumentTypes,
                            TypeIdentifier returnType) {
}

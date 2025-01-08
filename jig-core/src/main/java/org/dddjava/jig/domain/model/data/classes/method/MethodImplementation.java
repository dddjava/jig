package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

import java.util.Optional;

/**
 * 実装されたメソッド
 *
 * テキストソース由来の情報
 */
public class MethodImplementation {
    MethodIdentifier methodIdentifier;
    MethodComment methodComment;

    public MethodImplementation(MethodIdentifier methodIdentifier, MethodComment methodComment) {
        this.methodIdentifier = methodIdentifier;
        this.methodComment = methodComment;
    }

    public MethodImplementation(MethodIdentifier methodIdentifier) {
        this(methodIdentifier, null);
    }

    /**
     * ソースコードなしの場合
     */
    public static MethodImplementation unknown(MethodIdentifier methodIdentifier) {
        return new MethodImplementation(methodIdentifier);
    }

    public Optional<MethodComment> comment() {
        return Optional.ofNullable(methodComment);
    }

    public boolean matches(MethodIdentifier methodIdentifier) {
        // テキストソース由来では引数型が確定しないのでクラスと名前で当てる
        return this.methodIdentifier.matches(methodIdentifier.declaringType(), methodIdentifier.methodSignature().methodName());
    }

    public boolean declaringTypeMatches(TypeIdentifier typeIdentifier) {
        return methodIdentifier.declaringType().equals(typeIdentifier);
    }
}

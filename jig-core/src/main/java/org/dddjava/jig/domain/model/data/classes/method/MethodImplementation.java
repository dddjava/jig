package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;

/**
 * 実装されたメソッド
 *
 * テキストソース由来の情報
 */
public record MethodImplementation(
        TypeIdentifier typeIdentifier,
        MethodImplementationDeclarator methodDeclarator,
        MethodComment methodComment
) {

    /**
     * ソースコードなしの場合
     *
     * これ作らなくてもいいようにできるはず
     */
    public static MethodImplementation unknown(MethodIdentifier methodIdentifier) {
        return new MethodImplementation(
                methodIdentifier.declaringType(),
                new MethodImplementationDeclarator(
                        methodIdentifier.methodSignature().methodName(),
                        methodIdentifier.methodSignature().listArgumentTypeIdentifiers().stream().map(TypeIdentifier::fullQualifiedName).toList()
                ),
                MethodComment.empty()
        );
    }

    public MethodComment comment() {
        return methodComment;
    }

    public boolean possiblyMatches(MethodIdentifier methodIdentifier) {
        // テキストソース由来では引数型が確定しないのでクラスと名前で当てる
        return methodIdentifier.declaringType().equals(typeIdentifier) && methodDeclarator.possiblyMatches(methodIdentifier.methodSignature());
    }

    public boolean hasComment() {
        return methodComment.exists();
    }

    public String methodIdentifierText() {
        return typeIdentifier().fullQualifiedName() + "#" + methodDeclarator().asText();
    }
}

package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.classes.type.ParameterizedType;
import org.dddjava.jig.domain.model.data.classes.type.TypeIdentifier;
import org.dddjava.jig.domain.model.data.comment.Comment;

/**
 * 実装されたメソッド
 *
 * テキストソース由来の情報
 */
public record MethodImplementation(
        TypeIdentifier typeIdentifier,
        JavaMethodDeclarator methodDeclarator,
        Comment comment
) {

    /**
     * ソースコードなしの場合
     *
     * これ作らなくてもいいようにできるはず
     */
    public static MethodImplementation unknown(MethodIdentifier methodIdentifier) {
        return new MethodImplementation(
                methodIdentifier.declaringType(),
                new JavaMethodDeclarator(
                        methodIdentifier.methodSignature().methodName(),
                        methodIdentifier.methodSignature().arguments().stream()
                                .map(ParameterizedType::typeIdentifier)
                                .map(TypeIdentifier::fullQualifiedName).toList()
                ),
                Comment.empty()
        );
    }

    public boolean possiblyMatches(MethodIdentifier methodIdentifier) {
        return methodIdentifier.declaringType().equals(typeIdentifier)
                && methodDeclarator.possiblyMatches(methodIdentifier.methodSignature());
    }

    public boolean hasComment() {
        return comment.exists();
    }

    public String methodIdentifierText() {
        return typeIdentifier().fullQualifiedName() + "#" + methodDeclarator().asText();
    }
}

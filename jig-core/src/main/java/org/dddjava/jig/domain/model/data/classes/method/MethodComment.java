package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.comment.Comment;

/**
 * メソッド別名
 */
public class MethodComment {
    MethodIdentifier methodIdentifier;
    Comment comment;

    public MethodComment(MethodIdentifier methodIdentifier, Comment comment) {
        this.methodIdentifier = methodIdentifier;
        this.comment = comment;
    }

    public static MethodComment empty(MethodIdentifier methodIdentifier) {
        return new MethodComment(methodIdentifier, Comment.empty());
    }

    public MethodIdentifier methodIdentifier() {
        return methodIdentifier;
    }

    public String asText() {
        return comment.summaryText();
    }

    public String asTextOrDefault(String defaultText) {
        if (comment.exists()) {
            return asText();
        }
        return defaultText;
    }

    public boolean isAliasFor(MethodIdentifier methodIdentifier) {
        // TODO オーバーロードに対応するときはここでやる。
        // MethodAliasのフィールドを硬いmethodIdentifierを持つのではなく、引数も含めてある程度マッチする感じのやつを作る。
        return methodIdentifier.matchTypeAndMethodName(this.methodIdentifier);
    }

    public Comment documentationComment() {
        return comment;
    }

    public boolean exists() {
        return comment.exists();
    }
}

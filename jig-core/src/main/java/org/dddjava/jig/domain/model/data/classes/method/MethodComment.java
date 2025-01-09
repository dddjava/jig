package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.comment.Comment;

/**
 * メソッド別名
 */
public class MethodComment {
    Comment comment;

    public MethodComment(Comment comment) {
        this.comment = comment;
    }

    public static MethodComment empty() {
        return new MethodComment(Comment.empty());
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

    public Comment documentationComment() {
        return comment;
    }

    public boolean exists() {
        return comment.exists();
    }
}

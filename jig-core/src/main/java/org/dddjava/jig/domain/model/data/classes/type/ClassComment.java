package org.dddjava.jig.domain.model.data.classes.type;

import org.dddjava.jig.domain.model.data.comment.Comment;
import org.dddjava.jig.domain.model.data.types.TypeIdentifier;

/**
 * 型別名
 */
public class ClassComment {
    TypeIdentifier typeIdentifier;
    Comment comment;

    public ClassComment(TypeIdentifier typeIdentifier, Comment comment) {
        this.typeIdentifier = typeIdentifier;
        this.comment = comment;
    }

    public static ClassComment empty(TypeIdentifier typeIdentifier) {
        return new ClassComment(typeIdentifier, Comment.empty());
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public boolean exists() {
        return comment.exists();
    }

    public String asText() {
        return comment.summaryText();
    }

    public String asTextOrDefault(String defaultText) {
        if (exists()) {
            return asText();
        }
        return defaultText;
    }

    public String nodeLabel() {
        return nodeLabel("\\n");
    }

    public String nodeLabel(String delimiter) {
        String aliasLine = "";
        if (exists()) {
            aliasLine = asText() + delimiter;
        }
        return aliasLine + typeIdentifier().asSimpleText();
    }

    public String asTextOrIdentifierSimpleText() {
        return asTextOrDefault(typeIdentifier.asSimpleText());
    }

    public Comment documentationComment() {
        return comment;
    }
}

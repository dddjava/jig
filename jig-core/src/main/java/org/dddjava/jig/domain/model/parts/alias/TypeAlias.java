package org.dddjava.jig.domain.model.parts.alias;

import org.dddjava.jig.domain.model.parts.declaration.type.TypeIdentifier;

/**
 * 型別名
 */
public class TypeAlias {
    TypeIdentifier typeIdentifier;
    DocumentationComment documentationComment;

    public TypeAlias(TypeIdentifier typeIdentifier, DocumentationComment documentationComment) {
        this.typeIdentifier = typeIdentifier;
        this.documentationComment = documentationComment;
    }

    public static TypeAlias empty(TypeIdentifier typeIdentifier) {
        return new TypeAlias(typeIdentifier, DocumentationComment.empty());
    }

    public TypeIdentifier typeIdentifier() {
        return typeIdentifier;
    }

    public boolean exists() {
        return documentationComment.exists();
    }

    public String asText() {
        return documentationComment.summaryText();
    }

    public String asTextOrDefault(String defaultText) {
        if (exists()) {
            return asText();
        }
        return defaultText;
    }

    public boolean markedCore() {
        return documentationComment.markedCore();
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

    public DocumentationComment documentationComment() {
        return documentationComment;
    }
}

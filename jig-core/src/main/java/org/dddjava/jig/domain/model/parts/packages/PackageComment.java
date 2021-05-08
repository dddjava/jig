package org.dddjava.jig.domain.model.parts.packages;

import org.dddjava.jig.domain.model.parts.comment.Comment;

/**
 * パッケージ別名
 */
public class PackageComment {
    PackageIdentifier packageIdentifier;
    Comment comment;

    public PackageComment(PackageIdentifier packageIdentifier, Comment comment) {
        this.packageIdentifier = packageIdentifier;
        this.comment = comment;
    }

    public static PackageComment empty(PackageIdentifier packageIdentifier) {
        return new PackageComment(packageIdentifier, Comment.empty());
    }

    public PackageIdentifier packageIdentifier() {
        return packageIdentifier;
    }

    public boolean exists() {
        return comment.exists();
    }

    public String asText() {
        return comment.summaryText();
    }

    public String summaryOrSimpleName() {
        if (comment.exists()) {
            return comment.summaryText();
        }
        return packageIdentifier.simpleName();
    }

    public Comment descriptionComment() {
        return comment;
    }
}

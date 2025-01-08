package org.dddjava.jig.domain.model.information.jigobject.package_;

import org.dddjava.jig.domain.model.data.comment.Comment;

public class JigPackageDescription {

    String subject;
    String content;

    private JigPackageDescription(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public static JigPackageDescription from(Comment comment) {
        String subject = comment.summaryText();
        String content = comment.bodyText();
        return new JigPackageDescription(subject, content);
    }

    public String subject() {
        return subject;
    }

    public boolean hasContent() {
        return !content.isEmpty();
    }

    public String content() {
        return content;
    }
}

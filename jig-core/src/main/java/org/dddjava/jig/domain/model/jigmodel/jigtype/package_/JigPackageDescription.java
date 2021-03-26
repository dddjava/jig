package org.dddjava.jig.domain.model.jigmodel.jigtype.package_;

import org.dddjava.jig.domain.model.jigmodel.lowmodel.alias.DocumentationComment;

public class JigPackageDescription {

    String subject;
    String content;

    private JigPackageDescription(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public static JigPackageDescription from(DocumentationComment documentationComment) {
        String subject = documentationComment.summaryText();
        String content = documentationComment.bodyText();
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

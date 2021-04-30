package org.dddjava.jig.domain.model.jigmodel.jigtype.member;

import org.dddjava.jig.domain.model.parts.alias.DocumentationComment;

public class JigMethodDescription {

    String subject;
    String content;

    private JigMethodDescription(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public static JigMethodDescription from(DocumentationComment documentationComment) {
        String subject = documentationComment.summaryText();
        String content = documentationComment.bodyText();
        return new JigMethodDescription(subject, content);
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

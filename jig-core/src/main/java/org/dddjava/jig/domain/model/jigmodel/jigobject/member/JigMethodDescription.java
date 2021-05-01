package org.dddjava.jig.domain.model.jigmodel.jigobject.member;

import org.dddjava.jig.domain.model.parts.comment.Comment;

public class JigMethodDescription {

    String subject;
    String content;

    private JigMethodDescription(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public static JigMethodDescription from(Comment comment) {
        String subject = comment.summaryText();
        String content = comment.bodyText();
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

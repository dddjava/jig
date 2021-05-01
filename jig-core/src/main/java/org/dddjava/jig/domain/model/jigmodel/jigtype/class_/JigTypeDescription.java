package org.dddjava.jig.domain.model.jigmodel.jigtype.class_;

import org.dddjava.jig.domain.model.parts.comment.Comment;

public class JigTypeDescription {

    String subject;
    String content;

    private JigTypeDescription(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public static JigTypeDescription from(Comment comment) {
        String subject = comment.summaryText();
        String content = comment.bodyText();
        return new JigTypeDescription(subject, content);
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

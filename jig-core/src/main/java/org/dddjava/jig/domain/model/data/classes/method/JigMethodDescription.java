package org.dddjava.jig.domain.model.data.classes.method;

import org.dddjava.jig.domain.model.data.term.Term;

public class JigMethodDescription {

    String subject;
    String content;

    private JigMethodDescription(String subject, String content) {
        this.subject = subject;
        this.content = content;
    }

    public static JigMethodDescription from(Term term) {
        return new JigMethodDescription(term.title(), term.description());
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

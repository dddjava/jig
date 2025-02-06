package org.dddjava.jig.adapter.html.dialect;

// htmlで使用しているので移動したい
public class JigTypeDescription {

    private final String subject;
    private final String content;

    public JigTypeDescription(String subject, String content) {
        this.subject = subject;
        this.content = content;
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

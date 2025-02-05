package org.dddjava.jig.domain.model.data.packages;

// htmlで使用しているので移動したい
public class JigPackageDescription {

    String subject;
    String content;

    public JigPackageDescription(String subject, String content) {
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

package org.dddjava.jig.domain.model.documents.documentformat;

public class JigDocumentLabel {
    String japanese;
    String english;

    private JigDocumentLabel(String japanese, String english) {
        this.japanese = japanese;
        this.english = english;
    }

    public static JigDocumentLabel of(String japanese, String english) {
        return new JigDocumentLabel(japanese, english);
    }
}

package org.dddjava.jig.domain.model.jigdocument.documentformat;

import java.util.Locale;

public enum JigDiagramFormat {
    SVG,
    PNG,
    DOT;

    public String extension() {
        return '.' + lowerCase();
    }

    private String lowerCase() {
        return name().toLowerCase(Locale.ENGLISH);
    }

    public String dotOption() {
        return "-T" + lowerCase();
    }
}

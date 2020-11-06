package org.dddjava.jig.presentation.view.graphviz;

import java.util.Locale;

public enum DiagramFormat {
    SVG,
    PNG,
    ;

    public String extension() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}

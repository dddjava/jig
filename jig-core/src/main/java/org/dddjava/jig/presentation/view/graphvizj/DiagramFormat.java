package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;

import java.util.Locale;

public enum DiagramFormat {
    SVG {
        @Override
        public Format graphvizjFormat() {
            return Format.SVG;
        }
    },
    PNG {
        @Override
        public Format graphvizjFormat() {
            return Format.PNG;
        }
    };

    public abstract Format graphvizjFormat();

    public String extension() {
        return name().toLowerCase(Locale.ENGLISH);
    }
}

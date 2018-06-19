package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;

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
}

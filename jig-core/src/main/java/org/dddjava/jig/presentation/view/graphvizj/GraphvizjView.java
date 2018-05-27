package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.presentation.view.JigView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;

public class GraphvizjView<T> implements JigView<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphvizjView.class);

    DotTextEditor<T> editor;

    public GraphvizjView(DotTextEditor<T> editor) {
        this.editor = editor;
    }

    public void render(T model, OutputStream outputStream) throws IOException {
        String graphText = editor.edit(model);

        LOGGER.debug(graphText);

        Graphviz.fromString(graphText)
                .render(Format.PNG)
                .toOutputStream(outputStream);
    }
}

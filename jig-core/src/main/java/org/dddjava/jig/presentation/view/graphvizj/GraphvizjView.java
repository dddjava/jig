package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.presentation.view.JigDocumentLocation;
import org.dddjava.jig.presentation.view.JigView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class GraphvizjView<T> implements JigView<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphvizjView.class);

    DotTextEditor<T> editor;

    public GraphvizjView(DotTextEditor<T> editor) {
        this.editor = editor;
    }

    @Override
    public void render(T model, JigDocumentLocation jigDocumentLocation) throws IOException {
        String graphText = editor.edit(model);

        LOGGER.debug(graphText);

        jigDocumentLocation.writeDocument(outputStream ->
                Graphviz.fromString(graphText)
                        .render(Format.PNG)
                        .toOutputStream(outputStream));
    }
}

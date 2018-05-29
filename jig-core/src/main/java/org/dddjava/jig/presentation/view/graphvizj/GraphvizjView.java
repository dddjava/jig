package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.presentation.view.JigDocumentLocation;
import org.dddjava.jig.presentation.view.JigView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GraphvizjView<T> implements JigView<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphvizjView.class);

    DotTextEditor<T> editor;

    public GraphvizjView(DotTextEditor<T> editor) {
        this.editor = editor;
    }

    @Override
    public void render(T model, JigDocumentLocation jigDocumentLocation) {
        String graphText = editor.edit(model);

        jigDocumentLocation.writeDocument(outputStream ->
                Graphviz.fromString(graphText)
                        .render(Format.PNG)
                        .toOutputStream(outputStream));
        jigDocumentLocation.writeDebugText(graphText);
    }
}

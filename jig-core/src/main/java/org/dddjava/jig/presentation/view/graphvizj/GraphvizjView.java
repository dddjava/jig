package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Graphviz;
import org.dddjava.jig.presentation.view.JigDocumentLocation;
import org.dddjava.jig.presentation.view.JigView;

public class GraphvizjView<T> implements JigView<T> {

    DotTextEditor<T> editor;
    DiagramFormat diagramFormat;

    public GraphvizjView(DotTextEditor<T> editor, DiagramFormat diagramFormat) {
        this.editor = editor;
        this.diagramFormat = diagramFormat;
    }

    @Override
    public void render(T model, JigDocumentLocation jigDocumentLocation) {
        String graphText = editor.edit(model);

        jigDocumentLocation.writeDiagram(
                outputStream ->
                        Graphviz.fromString(graphText)
                                .render(diagramFormat.graphvizjFormat())
                                .toOutputStream(outputStream),
                diagramFormat);
        jigDocumentLocation.writeDebugText(graphText);
    }
}

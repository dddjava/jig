package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.engine.GraphvizException;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GraphvizjView<T> implements JigView<T> {
    DotTextEditor<T> editor;
    DiagramFormat diagramFormat;

    public GraphvizjView(DotTextEditor<T> editor, DiagramFormat diagramFormat) {
        this.editor = editor;
        this.diagramFormat = diagramFormat;
    }

    @Override
    public void render(T model, JigDocumentWriter jigDocumentWriter) {
        DotTexts dotTexts = editor.edit(model);

        if (dotTexts.isEmpty()) {
            jigDocumentWriter.skip();
            return;
        }

        // コマンドラインのみにする
        GraphvizCmdLineEngine graphvizCmdLineEngine = new GraphvizCmdLineEngine();

        try {
            Method doInit = GraphvizCmdLineEngine.class.getDeclaredMethod("doInit");
            doInit.setAccessible(true);
            doInit.invoke(graphvizCmdLineEngine);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // バージョンアップなどで内部メソッドが変更された場合に起こる
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof GraphvizException) {
                throw new IllegalStateException(e.getTargetException().getMessage());
            }
            throw new IllegalStateException(e);
        }

        Graphviz.useEngine(graphvizCmdLineEngine);

        for (DotText dot : dotTexts.list()) {
            jigDocumentWriter.writeDiagram(
                    outputStream ->
                            Graphviz.fromString(dot.text())
                                    .render(diagramFormat.graphvizjFormat())
                                    .toOutputStream(outputStream),
                    diagramFormat,
                    dot.documentSuffix());
        }
    }
}

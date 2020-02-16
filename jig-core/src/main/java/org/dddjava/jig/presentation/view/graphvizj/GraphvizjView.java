package org.dddjava.jig.presentation.view.graphvizj;

import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.engine.GraphvizException;
import org.dddjava.jig.domain.model.jigdocument.AdditionalText;
import org.dddjava.jig.domain.model.jigdocument.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.DocumentName;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;

import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class GraphvizjView<T> implements JigView<T> {
    DiagramSourceEditor<T> editor;
    DiagramFormat diagramFormat;

    public GraphvizjView(DiagramSourceEditor<T> editor, DiagramFormat diagramFormat) {
        this.editor = editor;
        this.diagramFormat = diagramFormat;
    }

    @Override
    public void render(T model, JigDocumentWriter jigDocumentWriter) {
        DiagramSource diagramSource = editor.edit(model);

        if (diagramSource.noValue()) {
            jigDocumentWriter.skip();
            return;
        }

        // コマンドラインのみにする
        GraphvizCmdLineEngine graphvizCmdLineEngine = new GraphvizCmdLineEngine();

        confirmInstalledGraphviz(graphvizCmdLineEngine);

        Graphviz.useEngine(graphvizCmdLineEngine);

        diagramSource.each(element -> writeDocument(jigDocumentWriter, element));
    }

    private void writeDocument(JigDocumentWriter jigDocumentWriter, DiagramSource diagramSource) {
        DocumentName documentName = diagramSource.documentName();

        jigDocumentWriter.write(
                outputStream ->
                        Graphviz.fromString(diagramSource.text())
                                .render(diagramFormat.graphvizjFormat())
                                .toOutputStream(outputStream),
                documentName.withExtension(diagramFormat.extension()));

        AdditionalText additionalText = diagramSource.additionalText();
        if (additionalText.enable()) {
            jigDocumentWriter.write(
                    outputStream -> {
                        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                            writer.write(additionalText.value());
                        }
                    },
                    documentName.withExtension("additional.txt")
            );
        }
    }

    public static void confirmInstalledGraphviz(GraphvizCmdLineEngine graphvizCmdLineEngine) {
        // graphvizがインストールされていることを GraphvizCmdLineEngine#doInit で確認する
        try {
            Method doInit = GraphvizCmdLineEngine.class.getDeclaredMethod("doInit");
            doInit.setAccessible(true);
            doInit.invoke(graphvizCmdLineEngine);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            // バージョンアップなどで doInit メソッドのシグネチャが変更された場合に起こる
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            if (e.getTargetException() instanceof GraphvizException) {
                // 実行可能な dot が見つからなかった
                // TODO メッセージ
                throw (GraphvizException) e.getTargetException();
            }
            // 想定しない例外
            throw new IllegalStateException(e);
        }
    }
}

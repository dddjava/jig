package org.dddjava.jig.presentation.view.graphviz.graphvizj;

import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.GraphvizCmdLineEngine;
import guru.nidi.graphviz.engine.GraphvizException;
import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.stationery.AdditionalText;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSource;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.dddjava.jig.presentation.view.graphviz.DiagramFormat;
import org.dddjava.jig.presentation.view.graphviz.DiagramSourceEditor;

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
        DiagramSources diagramSources = editor.edit(model);

        if (diagramSources.noEntity()) {
            jigDocumentWriter.skip();
            return;
        }

        // コマンドラインのみにする
        GraphvizCmdLineEngine graphvizCmdLineEngine = new GraphvizCmdLineEngine();

        confirmInstalledGraphviz(graphvizCmdLineEngine);

        Graphviz.useEngine(graphvizCmdLineEngine);

        diagramSources.each(element -> writeDocument(jigDocumentWriter, element));
    }

    private void writeDocument(JigDocumentWriter jigDocumentWriter, DiagramSource diagramSource) {
        DocumentName documentName = diagramSource.documentName();

        jigDocumentWriter.write(
                outputStream ->
                        Graphviz.fromString(diagramSource.text())
                                .render(toFormat())
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

    private Format toFormat() {
        // 対応しないフォーマットがあるとここで例外が発生する
        return Format.valueOf(diagramFormat.name());
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

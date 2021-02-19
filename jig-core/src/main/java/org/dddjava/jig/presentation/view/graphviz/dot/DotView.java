package org.dddjava.jig.presentation.view.graphviz.dot;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.stationery.AdditionalText;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;
import org.dddjava.jig.presentation.view.graphviz.DiagramSourceEditor;

import java.io.OutputStreamWriter;
import java.nio.file.Path;

public class DotView<T> implements JigView<T> {

    DiagramSourceEditor<T> editor;
    JigDiagramFormat diagramFormat;
    DotCommandRunner dotCommandRunner;

    public DotView(DiagramSourceEditor<T> editor, JigDiagramFormat diagramFormat, DotCommandRunner dotCommandRunner) {
        this.editor = editor;
        this.diagramFormat = diagramFormat;
        this.dotCommandRunner = dotCommandRunner;
    }

    @Override
    public void render(T model, JigDocumentWriter jigDocumentWriter) {
        DiagramSources diagramSources = editor.edit(model);

        if (diagramSources.noEntity()) {
            jigDocumentWriter.skip();
            return;
        }

        diagramSources.each(element -> {
            DocumentName documentName = element.documentName();
            Path sourcePath = dotCommandRunner.writeSource(element);

            jigDocumentWriter.writePath((directory, outputPaths) -> {
                String fileName = documentName.withExtension(diagramFormat);
                Path resultPath = dotCommandRunner.run(diagramFormat, sourcePath, directory.resolve(fileName));
                outputPaths.add(resultPath);
            });

            // 追加のテキストファイル
            AdditionalText additionalText = element.additionalText();
            if (additionalText.enable()) {
                jigDocumentWriter.write(
                        outputStream -> {
                            try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                                writer.write(additionalText.value());
                            }
                        },
                        documentName.fileName() + ".additional.txt"
                );
            }
        });
    }
}

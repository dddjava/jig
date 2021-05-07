package org.dddjava.jig.presentation.view.graphviz.dot;

import org.dddjava.jig.domain.model.jigdocument.documentformat.DocumentName;
import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.stationery.AdditionalText;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSources;
import org.dddjava.jig.domain.model.jigdocument.stationery.JigDocumentContext;
import org.dddjava.jig.presentation.view.JigDocumentWriter;
import org.dddjava.jig.presentation.view.JigView;

import java.io.OutputStreamWriter;
import java.nio.file.Path;

public class DotView implements JigView {

    JigDiagramFormat diagramFormat;
    DotCommandRunner dotCommandRunner;
    JigDocumentContext jigDocumentContext;

    public DotView(JigDiagramFormat diagramFormat, DotCommandRunner dotCommandRunner, JigDocumentContext jigDocumentContext) {
        this.diagramFormat = diagramFormat;
        this.dotCommandRunner = dotCommandRunner;
        this.jigDocumentContext = jigDocumentContext;
    }

    @Override
    public void render(Object model, JigDocumentWriter jigDocumentWriter) {
        DiagramSourceWriter sourceWriter = (DiagramSourceWriter) model;

        DiagramSources diagramSources = sourceWriter.sources(jigDocumentContext);
        // editor.edit(model);

        if (diagramSources.noEntity()) {
            jigDocumentWriter.markSkip();
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

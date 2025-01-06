package org.dddjava.jig.infrastructure.view.graphviz.dot;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.application.JigView;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.AdditionalText;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSourceWriter;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSources;
import org.dddjava.jig.domain.model.documents.stationery.JigDocumentContext;

import java.io.OutputStreamWriter;
import java.nio.file.Path;

public class DotView implements JigView {

    private final JigDocument jigDocument;
    JigDiagramFormat diagramFormat;
    DotCommandRunner dotCommandRunner;
    JigDocumentContext jigDocumentContext;

    public DotView(JigDocument jigDocument, JigDiagramFormat diagramFormat, DotCommandRunner dotCommandRunner, JigDocumentContext jigDocumentContext) {
        this.jigDocument = jigDocument;
        this.diagramFormat = diagramFormat;
        this.dotCommandRunner = dotCommandRunner;
        this.jigDocumentContext = jigDocumentContext;
    }

    @Override
    public JigDocument jigDocument() {
        return jigDocument;
    }

    @Override
    public void render(Object model, JigDocumentWriter jigDocumentWriter) {
        DiagramSourceWriter sourceWriter = (DiagramSourceWriter) model;

        DiagramSources diagramSources = sourceWriter.sources(jigDocumentContext);

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

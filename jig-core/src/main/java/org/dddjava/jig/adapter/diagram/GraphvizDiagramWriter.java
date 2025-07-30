package org.dddjava.jig.adapter.diagram;

import org.dddjava.jig.application.JigDocumentWriter;
import org.dddjava.jig.domain.model.documents.documentformat.DocumentName;
import org.dddjava.jig.domain.model.documents.documentformat.JigDocument;
import org.dddjava.jig.domain.model.documents.stationery.*;

import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.List;

public class GraphvizDiagramWriter {

    JigDiagramOption diagramOption;
    DotCommandRunner dotCommandRunner;
    JigDocumentContext jigDocumentContext;

    public GraphvizDiagramWriter(JigDocumentContext jigDocumentContext) {
        this.diagramOption = jigDocumentContext.diagramOption();
        this.dotCommandRunner = new DotCommandRunner(diagramOption);
        this.jigDocumentContext = jigDocumentContext;
    }

    public List<Path> write(DiagramSourceWriter model, JigDocument jigDocument) {
        JigDocumentWriter jigDocumentWriter = new JigDocumentWriter(jigDocument, jigDocumentContext.outputDirectory());
        DiagramSources diagramSources = model.sources(jigDocumentContext);

        if (diagramSources.noEntity()) {
            jigDocumentWriter.markSkip();
        } else {
            diagramSources.each(diagramSource -> {
                DocumentName documentName = diagramSource.documentName();

                jigDocumentWriter.writePath((directory, outputPaths) -> {
                    Path resultPath = dotCommandRunner.run(diagramSource, directory);
                    outputPaths.add(resultPath);
                });

                // 追加のテキストファイル
                AdditionalText additionalText = diagramSource.additionalText();
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

        return jigDocumentWriter.outputFilePaths();
    }
}

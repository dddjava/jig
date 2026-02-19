package org.dddjava.jig.adapter.graphviz;

import org.dddjava.jig.adapter.JigDocumentWriter;
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

        int count = model.write(diagramOption, diagramSource -> writeDiagram(diagramSource, jigDocumentWriter));

        if (count == 0) {
            jigDocumentWriter.markSkip();
        } else if (count == -1) {
            // -1: 未実装なので従来の処理を行う
            DiagramSources diagramSources = model.sources(jigDocumentContext);
            if (diagramSources.noEntity()) {
                jigDocumentWriter.markSkip();
            }

            diagramSources.each(diagramSource -> writeDiagram(diagramSource, jigDocumentWriter));
        }

        return jigDocumentWriter.outputFilePaths();
    }

    private void writeDiagram(DiagramSource diagramSource, JigDocumentWriter jigDocumentWriter) {
        DocumentName documentName = diagramSource.documentName();

        jigDocumentWriter.writePath((directory, outputPaths) -> {
            Path resultPath = dotCommandRunner.run(diagramSource, directory);
            outputPaths.add(resultPath);
        });

        // 追加のテキストファイルが存在する場合は内容を書き出す
        diagramSource.additionalText().ifPresent(value -> {
            jigDocumentWriter.write(
                    outputStream -> {
                        try (OutputStreamWriter writer = new OutputStreamWriter(outputStream)) {
                            writer.write(value);
                        }
                    },
                    documentName.fileName() + ".additional.txt"
            );
        });
    }
}

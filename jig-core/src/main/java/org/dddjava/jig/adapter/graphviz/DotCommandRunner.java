package org.dddjava.jig.adapter.graphviz;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSource;
import org.dddjava.jig.domain.model.documents.stationery.JigDiagramOption;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class DotCommandRunner {
    private static final Logger logger = Logger.getLogger(DotCommandRunner.class.getName());

    private final JigDiagramOption diagramOption;
    private final ProcessExecutor processExecutor;
    private final ThreadLocal<Path> workDirectory;

    public DotCommandRunner(JigDiagramOption diagramOption) {
        this.diagramOption = diagramOption;
        this.processExecutor = new ProcessExecutor(diagramOption.graphvizTimeout());
        this.workDirectory = ThreadLocal.withInitial(() -> {
            try {
                Path tempDirectory = Files.createTempDirectory("jig");
                tempDirectory.toFile().deleteOnExit();
                return tempDirectory;
            } catch (IOException e) {
                throw new UncheckedIOException("テンポラリディレクトリの作成に失敗しました。", e);
            }
        });
    }

    public Path run(DiagramSource diagramSource, Path outputDirectory) {
        var sourcePath = writeSourceToTemporaryDirectory(diagramSource);
        return executeDot(diagramSource, sourcePath, outputDirectory);
    }

    private Path executeDot(DiagramSource diagramSource, Path inputPath, Path outputDirectory) {
        var documentName = diagramSource.documentName();
        var outputPath = outputDirectory.resolve(documentName.withExtension(diagramOption.graphvizOutputFormat()));
        var documentFormat = diagramOption.graphvizOutputFormat();
        try {
            if (documentFormat == JigDiagramFormat.DOT) {
                Files.move(inputPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
                logger.fine("dot text file: " + outputPath);
                Files.deleteIfExists(inputPath);
                return outputPath;
            }

            ProcessResult result = processExecutor.execute(dotCommand(processExecutor),
                    documentFormat.dotOption(),
                    "-o" + outputPath,
                    inputPath.toString());

            if (result.failed()) {
                Path dotFilePath = outputDirectory.resolve(inputPath.getFileName());
                Files.copy(inputPath, dotFilePath, StandardCopyOption.REPLACE_EXISTING);
                logger.warning("dot command failed. write DOT file: " + dotFilePath);
                return dotFilePath;
            }

            logger.fine("diagram path: " + outputPath.toAbsolutePath());
            Files.deleteIfExists(inputPath);
            return outputPath;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String dotCommand(ProcessExecutor processExecutor) {
        // TODO DOTコマンドを設定可能にする（暫定対応）
        if (System.getenv().containsKey("JIG_DOT_COMMAND")) {
            return System.getenv("JIG_DOT_COMMAND");
        }
        return processExecutor.isWin() ? "dot.exe" : "dot";
    }

    private Path writeSourceToTemporaryDirectory(DiagramSource diagramSource) {
        try {
            String fileName = diagramSource.documentName().withExtension(JigDiagramFormat.DOT);
            Path sourcePath = workDirectory.get().resolve(fileName);
            Files.writeString(sourcePath, diagramSource.text());
            logger.fine("temporary DOT file: " + sourcePath.toAbsolutePath());
            return sourcePath;
        } catch (IOException e) {
            throw new UncheckedIOException("テンポラリファイルの出力に失敗しました。", e);
        }
    }
}

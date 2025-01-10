package org.dddjava.jig.adapter.diagram;

import org.dddjava.jig.domain.model.documents.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.documents.stationery.DiagramSource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class DotCommandRunner {
    Logger logger = Logger.getLogger(DotCommandRunner.class.getName());

    ProcessExecutor processExecutor = new ProcessExecutor();
    ThreadLocal<Path> workDirectory = ThreadLocal.withInitial(() -> {
        try {
            Path tempDirectory = Files.createTempDirectory("jig");
            tempDirectory.toFile().deleteOnExit();
            return tempDirectory;
        } catch (IOException e) {
            throw new UncheckedIOException("テンポラリディレクトリの作成に失敗しました。", e);
        }
    });

    public Path run(JigDiagramFormat documentFormat, Path inputPath, Path outputPath) {
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
                Path dotFilePath = outputPath.getParent().resolve(inputPath.getFileName());
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

    public ProcessResult runVersion() {
        ProcessResult result = processExecutor.execute(dotCommand(processExecutor), "-V");
        if (result.failed()) {
            return result.withMessage("Graphvizのバージョン取得に失敗しました。Graphvizがインストールできていない可能性があります。");
        }
        return result;
    }

    private String dotCommand(ProcessExecutor processExecutor) {
        // TODO DOTコマンドを設定可能にする（暫定対応）
        if (System.getenv().containsKey("JIG_DOT_COMMAND")) {
            return System.getenv("JIG_DOT_COMMAND");
        }
        return processExecutor.isWin() ? "dot.exe" : "dot";
    }

    public void verify() {
        ProcessResult processResult = runVersion();
        if (processResult.failed()) {
            throw new IllegalStateException(processResult.message());
        }
    }

    public Path writeSource(DiagramSource element) {
        try {
            String fileName = element.documentName().withExtension(JigDiagramFormat.DOT);
            Path sourcePath = workDirectory.get().resolve(fileName);
            Files.writeString(sourcePath, element.text());
            logger.fine("temporary DOT file: " + sourcePath.toAbsolutePath());
            return sourcePath;
        } catch (IOException e) {
            throw new UncheckedIOException("テンポラリファイルの出力に失敗しました。", e);
        }
    }
}

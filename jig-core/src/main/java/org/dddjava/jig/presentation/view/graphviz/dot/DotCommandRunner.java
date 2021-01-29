package org.dddjava.jig.presentation.view.graphviz.dot;

import org.dddjava.jig.domain.model.jigdocument.documentformat.JigDiagramFormat;
import org.dddjava.jig.domain.model.jigdocument.stationery.DiagramSource;
import org.dddjava.jig.presentation.view.graphviz.process.DotProcessExecutor;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

public class DotCommandRunner {
    Logger logger = Logger.getLogger(DotCommandRunner.class.getName());

    DotProcessExecutor dotProcessExecutor = new DotProcessExecutor();
    Path workDirectory;

    public DotCommandRunner() {
        try {
            workDirectory = Files.createTempDirectory("jig");
            workDirectory.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new UncheckedIOException("テンポラリディレクトリの作成に失敗しました。", e);
        }
    }

    public DotCommandResult run(JigDiagramFormat documentFormat, Path inputPath, Path outputPath) {
        try {
            if (documentFormat == JigDiagramFormat.DOT) {
                Files.move(inputPath, outputPath, StandardCopyOption.REPLACE_EXISTING);
                logger.info("dot text file: " + outputPath);
                Files.deleteIfExists(inputPath);
                return DotCommandResult.success();
            }

            String[] options = {documentFormat.dotOption(), "-o" + outputPath, inputPath.toString()};
            DotCommandResult result = dotProcessExecutor.execute(options);

            if (result.succeed()) {
                logger.info("diagram path: " + outputPath.toAbsolutePath());
                Files.deleteIfExists(inputPath);
            }

            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DotCommandResult runVersion() {
        DotCommandResult result = dotProcessExecutor.execute("-V");
        if (result.failed()) {
            return result.withMessage("Graphvizのバージョン取得に失敗しました。インストール状況を確認してください。");
        }
        return result;
    }

    public void verify() {
        DotCommandResult dotCommandResult = runVersion();
        if (dotCommandResult.failed()) {
            throw new IllegalStateException(dotCommandResult.message());
        }
    }

    public Path writeSource(DiagramSource element) {
        try {
            String fileName = element.documentName().withExtension(JigDiagramFormat.DOT);
            Path sourcePath = workDirectory.resolve(fileName);
            Files.write(sourcePath, element.text().getBytes(StandardCharsets.UTF_8));
            logger.info("temporary DOT file: " + sourcePath.toAbsolutePath());
            return sourcePath;
        } catch (IOException e) {
            throw new UncheckedIOException("テンポラリファイルの出力に失敗しました。", e);
        }
    }
}

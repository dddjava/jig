package org.dddjava.jig.presentation.view;

import org.dddjava.jig.domain.basic.FileWriteFailureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class AbstractLocalView implements LocalView {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLocalView.class);
    private final String fileName;

    public AbstractLocalView(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void write(Path outputDirectory) {
        try {
            if (Files.notExists(outputDirectory)) {
                Files.createDirectories(outputDirectory);
                LOGGER.info("{} を作成しました。", outputDirectory.toAbsolutePath());
            }

            Path outputFilePath = outputDirectory.resolve(fileName);
            try (OutputStream outputStream = new BufferedOutputStream(Files.newOutputStream(outputFilePath))) {
                write(outputStream);
                LOGGER.info("{} を出力しました。", outputFilePath.toAbsolutePath());
            }
        } catch (IOException e) {
            throw new FileWriteFailureException(e);
        }
    }

    protected abstract void write(OutputStream outputStream) throws IOException;
}

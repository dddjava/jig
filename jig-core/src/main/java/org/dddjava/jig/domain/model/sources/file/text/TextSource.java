package org.dddjava.jig.domain.model.sources.file.text;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TextSource {

    private Path location;

    public TextSource(Path location) {
        this.location = location;
    }

    public Path location() {
        return location;
    }

    public TextSourceType textSourceType() {
        String fileName = location().getFileName().toString();
        return TextSourceType.from(fileName);
    }

    public byte[] readAllBytes() throws IOException {
        return Files.readAllBytes(location());
    }

    public ReadableTextSource toReadableTextSource() throws IOException {
        return new ReadableTextSource(this, readAllBytes());
    }
}

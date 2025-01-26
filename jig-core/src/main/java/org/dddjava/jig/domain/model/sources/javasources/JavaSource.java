package org.dddjava.jig.domain.model.sources.javasources;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JavaSource {

    private final Path location;

    public JavaSource(Path location) {
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
